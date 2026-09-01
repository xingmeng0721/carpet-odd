package carpetodd.xm.command;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.helpers.EntityPlayerActionPack.Action;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import carpet.patches.EntityPlayerMPFake;
import carpet.utils.Messenger;
import carpetodd.xm.CarpetOddSettings;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.lang.reflect.Method;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Direction;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class BatchPlayerCommand {

    private static final int MAX_BATCH_SIZE = 256;
    private static final int MAX_NAME_LENGTH = 16;

    private static String buildName(String prefix, int index) {
        String base = prefix.endsWith("_") ? prefix : prefix + "_";
        String name = base + index;
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

    private static boolean checkRule(CommandContext<CommandSourceStack> context) {
        if (!CarpetOddSettings.batchPlayerCommand) {
            Messenger.m(context.getSource(), "r batchPlayerCommand rule is not enabled");
            return false;
        }
        return true;
    }

    private static int[] normalizeRange(int start, int end) {
        int min = Math.min(start, end);
        int max = Math.max(start, end);
        return new int[]{min, max};
    }

    // ==================== spawn ====================

    public static int batchSpawn(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int start = IntegerArgumentType.getInteger(context, "start");
        int end = IntegerArgumentType.getInteger(context, "end");
        int[] range = normalizeRange(start, end);
        start = range[0];
        end = range[1];

        if (end - start + 1 > MAX_BATCH_SIZE) {
            Messenger.m(context.getSource(), "r Batch size exceeds limit of " + MAX_BATCH_SIZE);
            return 0;
        }

        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        Vec3 pos = Vec3Argument.getVec3(context, "at");
        float yaw = source.getRotation().y;
        float pitch = source.getRotation().x;

        int count = 0;
        for (int i = start; i <= end; i++) {
            String name = buildName(prefix, i);
            if (server.getPlayerList().getPlayerByName(name) != null) continue;
            boolean created = EntityPlayerMPFake.createFake(
                    name, server, pos,
                    yaw, pitch,
                    source.getLevel().dimension(),
                    GameType.SURVIVAL, false
            );
            if (created) count++;
        }

        Messenger.m(source, "g Batch spawned " + count + " player(s)");
        return count;
    }

    // ==================== kill ====================

    public static int batchKill(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int start = IntegerArgumentType.getInteger(context, "start");
        int end = IntegerArgumentType.getInteger(context, "end");
        int[] range = normalizeRange(start, end);
        start = range[0];
        end = range[1];

        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = start; i <= end; i++) {
            String name = buildName(prefix, i);
            ServerPlayer player = server.getPlayerList().getPlayerByName(name);
            if (!(player instanceof EntityPlayerMPFake)) continue;
            player.connection.onDisconnect(
                    new DisconnectionDetails(Component.literal("Batch killed"))
            );
            count++;
        }

        Messenger.m(source, "g Batch killed " + count + " player(s)");
        return count;
    }

    // ==================== ActionType actions ====================
    // jump, attack, use, drop, drop_stack, swap_hands
    // Modes: once, continuous, after <ticks>, interval <ticks>, perTick <times>, randomly <min> <max>

    public static int batchStartAction(CommandContext<CommandSourceStack> context, ActionType actionType) {
        return applyActionFactory(context, actionType, Action::once);
    }

    public static int batchStartActionContinuous(CommandContext<CommandSourceStack> context, ActionType actionType) {
        return applyActionFactory(context, actionType, Action::continuous);
    }

    public static int batchStartActionAfter(CommandContext<CommandSourceStack> context,
                                             ActionType actionType, int ticks) {
        return applyActionFactory(context, actionType, () -> Action.interval(1, ticks));
    }

    public static int batchStartActionInterval(CommandContext<CommandSourceStack> context,
                                                ActionType actionType, int ticks) {
        return applyActionFactory(context, actionType, () -> Action.interval(ticks));
    }

    // ---- perTick (requires TIS-Addition) ----

    public static int batchStartActionPerTick(CommandContext<CommandSourceStack> context,
                                               ActionType actionType, int perTick) {
        if (!checkTis(context)) return 0;
        try {
            Class<?> helper = Class.forName("carpettisaddition.helpers.carpet.playerActionEnhanced.PlayerActionPackHelper");
            Method perTickMethod = helper.getMethod("perTick", int.class);
            return applyActionFactory(context, actionType, () -> {
                try {
                    return (Action) perTickMethod.invoke(null, perTick);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            Messenger.m(context.getSource(), "r Failed to create perTick action: " + e.getMessage());
            return 0;
        }
    }

    // ---- randomly (requires TIS-Addition) ----

    public static int batchStartActionRandomly(CommandContext<CommandSourceStack> context,
                                                ActionType actionType, int min, int max) {
        if (!checkTis(context)) return 0;
        if (min < 1) min = 1;
        if (max < min) { int tmp = min; min = max; max = tmp; }
        final int fMin = min;
        final int fMax = max;
        try {
            Class<?> uniformGenClass = Class.forName("carpettisaddition.helpers.carpet.playerActionEnhanced.randomly.gen.UniformGen");
            Class<?> randomGenClass = Class.forName("carpettisaddition.helpers.carpet.playerActionEnhanced.randomly.gen.RandomGen");
            Method genInterval = uniformGenClass.getMethod("generateRandomInterval");
            Method setRandom = EntityPlayerActionPack.Action.class.getMethod("setIntervalRandomGenerator$TISCM", randomGenClass);

            return applyActionFactory(context, actionType, () -> {
                try {
                    Object gen = uniformGenClass.getConstructor(int.class, int.class).newInstance(fMin, fMax);
                    int initialInterval = (int) genInterval.invoke(gen);
                    Action action = Action.interval(initialInterval);
                    setRandom.invoke(action, gen);
                    return action;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            Messenger.m(context.getSource(), "r Failed to create randomly action: " + e.getMessage());
            return 0;
        }
    }

    private static int applyActionFactory(CommandContext<CommandSourceStack> context, ActionType actionType,
                                           java.util.function.Supplier<Action> actionFactory) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.start(actionType, actionFactory.get());
            count++;
        }

        Messenger.m(source, "g Applied " + actionType.name().toLowerCase() + " to " + count + " player(s)");
        return count;
    }

    // ==================== move ====================
    // move forward/backward → setForward(±1)
    // move left/right → setStrafing(±1)

    public static int batchMove(CommandContext<CommandSourceStack> context, float forward, float strafe) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.setForward(forward);
            ap.setStrafing(strafe);
            count++;
        }

        String dir = forward > 0 ? "forward" : forward < 0 ? "backward" : strafe > 0 ? "right" : "left";
        Messenger.m(source, "g Set " + count + " player(s) moving " + dir);
        return count;
    }

    // ==================== sneak / unsneak ====================

    public static int batchSneak(CommandContext<CommandSourceStack> context, boolean sneak) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.setSneaking(sneak);
            count++;
        }

        Messenger.m(source, "g " + (sneak ? "Sneaking" : "Stopped sneaking") + " for " + count + " player(s)");
        return count;
    }

    // ==================== sprint / unsprint ====================

    public static int batchSprint(CommandContext<CommandSourceStack> context, boolean sprint) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.setSprinting(sprint);
            count++;
        }

        Messenger.m(source, "g " + (sprint ? "Sprinting" : "Stopped sprinting") + " for " + count + " player(s)");
        return count;
    }

    // ==================== look (direction) ====================

    public static int batchLookDirection(CommandContext<CommandSourceStack> context, Direction direction) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.look(direction);
            count++;
        }

        Messenger.m(source, "g Looked " + direction.getName() + " for " + count + " player(s)");
        return count;
    }

    // ==================== look at (position) ====================

    public static int batchLookAt(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        Vec3 pos = Vec3Argument.getVec3(context, "pos");

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.lookAt(pos);
            count++;
        }

        Messenger.m(source, "g Looked at position for " + count + " player(s)");
        return count;
    }

    // ==================== turn ====================

    public static int batchTurn(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        float yaw = FloatArgumentType.getFloat(context, "yaw");
        float pitch = FloatArgumentType.getFloat(context, "pitch");

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.turn(yaw, pitch);
            count++;
        }

        Messenger.m(source, "g Turned " + count + " player(s)");
        return count;
    }

    // ==================== hotbar ====================

    public static int batchHotbar(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        int slot = IntegerArgumentType.getInteger(context, "slot");

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.setSlot(slot);
            count++;
        }

        Messenger.m(source, "g Set hotbar slot " + slot + " for " + count + " player(s)");
        return count;
    }

    // ==================== mount ====================

    public static int batchMount(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.mount(false);
            count++;
        }

        Messenger.m(source, "g Mounted " + count + " player(s)");
        return count;
    }

    // ==================== dismount ====================

    public static int batchDismount(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.dismount();
            count++;
        }

        Messenger.m(source, "g Dismounted " + count + " player(s)");
        return count;
    }

    // ==================== stop ====================

    public static int batchStop(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return 0;

        int[] range = getRange(context);
        String prefix = StringArgumentType.getString(context, "prefix");
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        int count = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            ServerPlayer player = getFakePlayer(server, prefix, i);
            if (player == null) continue;
            EntityPlayerActionPack ap = ((ServerPlayerInterface) player).getActionPack();
            ap.stopAll();
            count++;
        }

        Messenger.m(source, "g Stopped all actions for " + count + " player(s)");
        return count;
    }

    // ==================== helpers ====================

    private static boolean checkTis(CommandContext<CommandSourceStack> context) {
        if (!checkRule(context)) return false;
        try {
            Class.forName("carpettisaddition.helpers.carpet.playerActionEnhanced.PlayerActionPackHelper");
            return true;
        } catch (ClassNotFoundException e) {
            Messenger.m(context.getSource(), "r perTick/randomly requires Carpet TIS Addition to be installed");
            return false;
        }
    }

    private static int[] getRange(CommandContext<CommandSourceStack> context) {
        int start = IntegerArgumentType.getInteger(context, "start");
        int end = IntegerArgumentType.getInteger(context, "end");
        return normalizeRange(start, end);
    }

    private static ServerPlayer getFakePlayer(MinecraftServer server, String prefix, int index) {
        String name = buildName(prefix, index);
        ServerPlayer player = server.getPlayerList().getPlayerByName(name);
        if (!(player instanceof EntityPlayerMPFake)) return null;
        return player;
    }
}
