package carpetodd.xm;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpetodd.xm.command.AutoDropCommand;
import carpetodd.xm.command.BatchPlayerCommand;
import carpetodd.xm.command.CustomItemMaxStackSizeCommand;
import carpet.helpers.EntityPlayerActionPack.ActionType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Direction;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class CarpetOddExtension implements CarpetExtension {

    private static final CarpetOddExtension INSTANCE = new CarpetOddExtension();
    private static final Gson GSON = new Gson();

    private CarpetOddExtension() {}

    public static void init() {
        CarpetServer.manageExtension(INSTANCE);
    }

    public static CarpetOddExtension getInstance() {
        return INSTANCE;
    }

    @Override
    public void onGameStarted() {
        CarpetServer.settingsManager.parseSettingsClass(CarpetOddSettings.class);
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                 CommandBuildContext commandBuildContext) {
        CustomItemMaxStackSizeCommand.register(dispatcher, commandBuildContext);
        dispatcher.register(
                Commands.literal("player")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .then(Commands.literal("autodrop")
                                        .executes(AutoDropCommand::execute))));

        // /playerManager batch <prefix> <start> <end> <action>
        dispatcher.register(
                Commands.literal("playerManager")
                        .then(Commands.literal("batch")
                                .then(Commands.argument("prefix", StringArgumentType.word())
                                        .then(Commands.argument("start", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("end", IntegerArgumentType.integer(1))
                                                        // spawn
                                                        .then(Commands.literal("spawn")
                                                                .then(Commands.argument("at", Vec3Argument.vec3())
                                                                        .executes(BatchPlayerCommand::batchSpawn)))
                                                        // kill
                                                        .then(Commands.literal("kill")
                                                                .executes(BatchPlayerCommand::batchKill))
                                                        // ActionType actions with mode sub-nodes
                                                        .then(addActionModes(Commands.literal("use"), ActionType.USE))
                                                        .then(addActionModes(Commands.literal("attack"), ActionType.ATTACK))
                                                        .then(addActionModes(Commands.literal("jump"), ActionType.JUMP))
                                                        // One-shot actions
                                                        .then(Commands.literal("drop")
                                                                .executes(ctx -> BatchPlayerCommand.batchStartAction(ctx, ActionType.DROP_ITEM)))
                                                        .then(Commands.literal("drop_stack")
                                                                .executes(ctx -> BatchPlayerCommand.batchStartAction(ctx, ActionType.DROP_STACK)))
                                                        .then(Commands.literal("swap_hands")
                                                                .executes(ctx -> BatchPlayerCommand.batchStartAction(ctx, ActionType.SWAP_HANDS)))
                                                        // move
                                                        .then(Commands.literal("move")
                                                                .then(Commands.literal("forward")
                                                                        .executes(ctx -> BatchPlayerCommand.batchMove(ctx, 1.0f, 0.0f)))
                                                                .then(Commands.literal("backward")
                                                                        .executes(ctx -> BatchPlayerCommand.batchMove(ctx, -1.0f, 0.0f)))
                                                                .then(Commands.literal("left")
                                                                        .executes(ctx -> BatchPlayerCommand.batchMove(ctx, 0.0f, -1.0f)))
                                                                .then(Commands.literal("right")
                                                                        .executes(ctx -> BatchPlayerCommand.batchMove(ctx, 0.0f, 1.0f))))
                                                        // sneak / unsneak
                                                        .then(Commands.literal("sneak")
                                                                .executes(ctx -> BatchPlayerCommand.batchSneak(ctx, true)))
                                                        .then(Commands.literal("unsneak")
                                                                .executes(ctx -> BatchPlayerCommand.batchSneak(ctx, false)))
                                                        // sprint / unsprint
                                                        .then(Commands.literal("sprint")
                                                                .executes(ctx -> BatchPlayerCommand.batchSprint(ctx, true)))
                                                        .then(Commands.literal("unsprint")
                                                                .executes(ctx -> BatchPlayerCommand.batchSprint(ctx, false)))
                                                        // look
                                                        .then(Commands.literal("look")
                                                                .then(Commands.literal("north").executes(ctx -> BatchPlayerCommand.batchLookDirection(ctx, Direction.NORTH)))
                                                                .then(Commands.literal("south").executes(ctx -> BatchPlayerCommand.batchLookDirection(ctx, Direction.SOUTH)))
                                                                .then(Commands.literal("east").executes(ctx -> BatchPlayerCommand.batchLookDirection(ctx, Direction.EAST)))
                                                                .then(Commands.literal("west").executes(ctx -> BatchPlayerCommand.batchLookDirection(ctx, Direction.WEST)))
                                                                .then(Commands.literal("up").executes(ctx -> BatchPlayerCommand.batchLookDirection(ctx, Direction.UP)))
                                                                .then(Commands.literal("down").executes(ctx -> BatchPlayerCommand.batchLookDirection(ctx, Direction.DOWN)))
                                                                .then(Commands.literal("at")
                                                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                                                .executes(BatchPlayerCommand::batchLookAt))))
                                                        // turn
                                                        .then(Commands.literal("turn")
                                                                .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                                                                        .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                                                                                .executes(BatchPlayerCommand::batchTurn))))
                                                        // hotbar
                                                        .then(Commands.literal("hotbar")
                                                                .then(Commands.argument("slot", IntegerArgumentType.integer(1, 9))
                                                                        .executes(BatchPlayerCommand::batchHotbar)))
                                                        // mount / dismount
                                                        .then(Commands.literal("mount")
                                                                .executes(BatchPlayerCommand::batchMount))
                                                        .then(Commands.literal("dismount")
                                                                .executes(BatchPlayerCommand::batchDismount))
                                                        // stop
                                                        .then(Commands.literal("stop")
                                                                .executes(BatchPlayerCommand::batchStop)))))));
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        String path = String.format("assets/%s/lang/%s.json", CarpetOddMod.MOD_ID, lang);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return Collections.emptyMap();
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return GSON.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception e) {
            CarpetOddMod.LOGGER.warn("Failed to load translations for lang: {}", lang, e);
            return Collections.emptyMap();
        }
    }

    @Override
    public String version() {
        return CarpetOddMod.MOD_ID;
    }

    /**
     * Adds mode sub-nodes (once, continuous, after, interval, perTick, randomly) as siblings to an action node.
     * perTick and randomly require Carpet TIS Addition.
     */
    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> addActionModes(
            com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> actionNode, ActionType type) {
        actionNode
            .then(Commands.literal("once")
                    .executes(ctx -> BatchPlayerCommand.batchStartAction(ctx, type)))
            .then(Commands.literal("continuous")
                    .executes(ctx -> BatchPlayerCommand.batchStartActionContinuous(ctx, type)))
            .then(Commands.literal("after")
                    .then(Commands.argument("ticks", IntegerArgumentType.integer(1))
                            .executes(ctx -> BatchPlayerCommand.batchStartActionAfter(ctx, type,
                                    IntegerArgumentType.getInteger(ctx, "ticks")))))
            .then(Commands.literal("interval")
                    .then(Commands.argument("ticks", IntegerArgumentType.integer(1))
                            .executes(ctx -> BatchPlayerCommand.batchStartActionInterval(ctx, type,
                                    IntegerArgumentType.getInteger(ctx, "ticks")))))
            .then(Commands.literal("perTick")
                    .then(Commands.argument("times", IntegerArgumentType.integer(1, 64))
                            .executes(ctx -> BatchPlayerCommand.batchStartActionPerTick(ctx, type,
                                    IntegerArgumentType.getInteger(ctx, "times")))))
            .then(Commands.literal("randomly")
                    .then(Commands.argument("min", IntegerArgumentType.integer(1))
                            .then(Commands.argument("max", IntegerArgumentType.integer(1))
                                    .executes(ctx -> BatchPlayerCommand.batchStartActionRandomly(ctx, type,
                                            IntegerArgumentType.getInteger(ctx, "min"),
                                            IntegerArgumentType.getInteger(ctx, "max"))))));
        return actionNode;
    }
}
