package carpetodd.xm.manager;

import carpetodd.xm.CarpetOddMod;
import carpetodd.xm.CarpetOddSettings;
import carpetodd.xm.network.OddNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class CustomItemMaxStackSizeDataManager {
    public static final CustomItemMaxStackSizeDataManager INSTANCE = new CustomItemMaxStackSizeDataManager();
    public static final String FILLED_SHULKER_BOX = "filled_shulker_box";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STORED_TYPE = new TypeToken<LinkedHashMap<String, Integer>>() {}.getType();
    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 99;

    private final Map<String, Integer> configuredStacks = new LinkedHashMap<>();
    private final List<StackRule> runtimeRules = new ArrayList<>();

    /** Client-side only: rules synced from the server; drives the ItemStack.getMaxStackSize mixin. */
    private volatile boolean clientRulesActive = false;
    private final List<StackRule> clientRules = new ArrayList<>();

    /**
     * Client-side only: whether the player is currently viewing their own inventory (no container GUI open).
     * The ItemStack.getMaxStackSize override is global and ItemStack can't tell which container it sits in,
     * so we gate it to the inventory screen; otherwise chests/other containers would wrongly stack too.
     */
    private volatile java.util.function.BooleanSupplier clientInventoryView = () -> false;

    public void setClientInventoryView(java.util.function.BooleanSupplier supplier) {
        this.clientInventoryView = supplier;
    }

    private CustomItemMaxStackSizeDataManager() {}

    public void set(String pattern, int count, CommandBuildContext context) throws CommandSyntaxException {
        StackRule rule = buildRule(pattern, count, context); // parse first, commit only on success
        configuredStacks.put(pattern, count);
        runtimeRules.removeIf(existing -> existing.pattern.equals(pattern));
        runtimeRules.add(rule);
        save();
        OddNetwork.broadcast();
    }

    public void setFilledShulkerBox(int count) {
        configuredStacks.put(FILLED_SHULKER_BOX, count);
        runtimeRules.removeIf(rule -> rule.pattern.equals(FILLED_SHULKER_BOX));
        runtimeRules.add(new StackRule(FILLED_SHULKER_BOX, CustomItemMaxStackSizeDataManager::isFilledShulkerBox, count));
        save();
        OddNetwork.broadcast();
    }

    public boolean remove(String pattern) {
        boolean removed = configuredStacks.remove(pattern) != null;
        runtimeRules.removeIf(rule -> rule.pattern.equals(pattern));
        if (removed) {
            save();
            OddNetwork.broadcast();
        }
        return removed;
    }

    public void clear() {
        configuredStacks.clear();
        runtimeRules.clear();
        save();
        OddNetwork.broadcast();
    }

    public Map<String, Integer> getCurrentData() {
        return new LinkedHashMap<>(configuredStacks);
    }

    public int getCustomStackSize(ItemStack stack) {
        if (!CarpetOddSettings.playerInventoryStack) return -1;
        for (StackRule rule : runtimeRules) {
            if (rule.predicate.test(stack)) return rule.size;
        }
        return -1;
    }

    /**
     * Slot-scoped lookup used by the {@code Slot} / {@code Container} / {@code AbstractContainerMenu} mixins.
     * Unlike {@link #getClientCustomStackSize(ItemStack)} it is deliberately NOT gated on the open screen,
     * because callers already restrict the override to player-inventory containers.
     *
     * <p>The screen gate has to stay off here: quick-move (shift-click, and itemscroller's "move everything")
     * is predicted locally on the client through {@code moveItemStackTo}, which reads the slot limit. If the
     * client predicted with the vanilla limit while the server settled with the custom one, the two disagree
     * on the slot contents, the container state id desyncs, and every click after the first is dropped --
     * which is what made "move everything" only ever fill a single inventory slot.
     */
    public int getInventorySlotStackSize(ItemStack stack) {
        if (CarpetOddSettings.playerInventoryStack) {
            for (StackRule rule : runtimeRules) {
                if (rule.predicate.test(stack)) return rule.size;
            }
        }
        if (clientRulesActive) {
            for (StackRule rule : clientRules) {
                if (rule.predicate.test(stack)) return rule.size;
            }
        }
        return -1;
    }

    /**
     * Client-side lookup used by the {@code ItemStack.getMaxStackSize} mixin so that client sorting
     * mods (IPN) see the custom limit. Independent of the server-side {@link CarpetOddSettings} value;
     * activation is driven by the synced {@code enabled} flag instead.
     */
    public int getClientCustomStackSize(ItemStack stack) {
        if (!clientRulesActive || !clientInventoryView.getAsBoolean()) return -1;
        for (StackRule rule : clientRules) {
            if (rule.predicate.test(stack)) return rule.size;
        }
        return -1;
    }

    /** Client-side: rebuilds synced rules. {@code context} is supplied by the caller to avoid a Minecraft-class dependency here. */
    public void applyClientRules(boolean enabled, Map<String, Integer> data, CommandBuildContext context) {
        clientRulesActive = enabled;
        List<StackRule> rebuilt = new ArrayList<>();
        if (enabled) {
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                Integer count = entry.getValue();
                if (count == null || count < MIN_COUNT || count > MAX_COUNT) continue;
                try {
                    rebuilt.add(buildRule(entry.getKey(), count, context));
                } catch (Exception exception) {
                    CarpetOddMod.LOGGER.warn("Failed to parse synced player inventory stack rule '{}'", entry.getKey());
                }
            }
        }
        clientRules.clear();
        clientRules.addAll(rebuilt);
    }

    /**
     * Rebuilds all rules from the on-disk file. Called once when the server builds its command tree,
     * which is the earliest point a {@link CommandBuildContext} is available to parse item predicates.
     * Rules whose predicate can no longer be parsed (e.g. removed items after a version change) are skipped.
     */
    public void load(CommandBuildContext context) {
        Path path = configPath();
        if (!Files.exists(path)) return;

        Map<String, Integer> stored;
        try (Reader reader = Files.newBufferedReader(path)) {
            stored = GSON.fromJson(reader, STORED_TYPE);
        } catch (Exception exception) {
            CarpetOddMod.LOGGER.error("Failed to read player inventory stack rules from {}", path, exception);
            return;
        }
        if (stored == null) return;

        configuredStacks.clear();
        runtimeRules.clear();
        for (Map.Entry<String, Integer> entry : stored.entrySet()) {
            String pattern = entry.getKey();
            Integer count = entry.getValue();
            if (count == null || count < MIN_COUNT || count > MAX_COUNT) {
                CarpetOddMod.LOGGER.warn("Skipped invalid player inventory stack rule '{}'", pattern);
                continue;
            }
            try {
                StackRule rule = buildRule(pattern, count, context);
                configuredStacks.put(pattern, count);
                runtimeRules.add(rule);
            } catch (Exception exception) {
                CarpetOddMod.LOGGER.warn("Failed to parse player inventory stack rule '{}', skipped", pattern);
            }
        }
    }

    private void save() {
        Path path = configPath();
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Path temp = Files.createTempFile(path.getParent(), path.getFileName().toString(), ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp)) {
                GSON.toJson(new LinkedHashMap<>(configuredStacks), writer);
            }
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            CarpetOddMod.LOGGER.error("Failed to save player inventory stack rules to {}", path, exception);
        }
    }

    private static StackRule buildRule(String pattern, int count, CommandBuildContext context) throws CommandSyntaxException {
        if (FILLED_SHULKER_BOX.equals(pattern)) {
            return new StackRule(pattern, CustomItemMaxStackSizeDataManager::isFilledShulkerBox, count);
        }
        ItemPredicateArgument.Result predicate = ItemPredicateArgument.itemPredicate(context)
                .parse(new StringReader(pattern));
        return new StackRule(pattern, predicate, count);
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir()
                .resolve(CarpetOddMod.MOD_ID)
                .resolve("player_inventory_stack.json");
    }

    private static boolean isFilledShulkerBox(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock)) {
            return false;
        }
        ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        int slots = 0;
        //#if MC >= 26_01_00
        //$$ for (ItemStack item : contents.nonEmptyItemCopyStream().toList()) {
        //#else
        for (ItemStack item : contents.nonEmptyItems()) {
        //#endif
            // A full slot uses the item's vanilla stack limit: 1 for minecarts, 16 for pearls, 64 for sand, etc.
            if (item.getCount() != item.getMaxStackSize()) return false;
            slots++;
        }
        return slots == 27;
    }

    private record StackRule(String pattern, Predicate<ItemStack> predicate, int size) {}
}
