package carpetodd.xm.manager;

import carpetodd.xm.CarpetOddSettings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class CustomItemMaxStackSizeDataManager {
    public static final CustomItemMaxStackSizeDataManager INSTANCE = new CustomItemMaxStackSizeDataManager();
    public static final String FILLED_SHULKER_BOX = "filled_shulker_box";

    private final Map<String, Integer> configuredStacks = new LinkedHashMap<>();
    private final List<StackRule> runtimeRules = new ArrayList<>();

    private CustomItemMaxStackSizeDataManager() {}

    public void set(String pattern, int count, CommandBuildContext context) throws CommandSyntaxException {
        ItemPredicateArgument.Result predicate = ItemPredicateArgument.itemPredicate(context)
                .parse(new StringReader(pattern));
        configuredStacks.put(pattern, count);
        runtimeRules.removeIf(rule -> rule.pattern.equals(pattern));
        runtimeRules.add(new StackRule(pattern, predicate, count));
    }

    public void setFilledShulkerBox(int count) {
        configuredStacks.put(FILLED_SHULKER_BOX, count);
        runtimeRules.removeIf(rule -> rule.pattern.equals(FILLED_SHULKER_BOX));
        runtimeRules.add(new StackRule(FILLED_SHULKER_BOX, CustomItemMaxStackSizeDataManager::isFilledShulkerBox, count));
    }

    public boolean remove(String pattern) {
        boolean removed = configuredStacks.remove(pattern) != null;
        runtimeRules.removeIf(rule -> rule.pattern.equals(pattern));
        return removed;
    }

    public void clear() {
        configuredStacks.clear();
        runtimeRules.clear();
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
