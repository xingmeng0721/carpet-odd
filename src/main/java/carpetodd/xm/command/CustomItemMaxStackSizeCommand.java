package carpetodd.xm.command;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;

import java.util.Map;

public final class CustomItemMaxStackSizeCommand {
    private CustomItemMaxStackSizeCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("playerInventoryStack")
                .then(Commands.literal("set")
                        .then(Commands.literal(CustomItemMaxStackSizeDataManager.FILLED_SHULKER_BOX)
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 99))
                                        .executes(context -> setFilledShulkerBox(context))))
                        .then(Commands.argument("predicate", ItemPredicateArgument.itemPredicate(commandBuildContext))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 99))
                                        .executes(context -> set(context, commandBuildContext)))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("predicate", StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        CustomItemMaxStackSizeDataManager.INSTANCE.getCurrentData().keySet(), builder))
                                .executes(context -> {
                                    String predicate = StringArgumentType.getString(context, "predicate");
                                    boolean removed = CustomItemMaxStackSizeDataManager.INSTANCE.remove(predicate);
                    context.getSource().sendSuccess(() -> Component.translatable(
                            removed ? "carpetodd.command.playerInventoryStack.removed"
                                    : "carpetodd.command.playerInventoryStack.not_found", predicate), true);
                                    return removed ? 1 : 0;
                                })))
                .then(Commands.literal("clear")
                        .executes(context -> {
                    CustomItemMaxStackSizeDataManager.INSTANCE.clear();
                    context.getSource().sendSuccess(() -> Component.translatable(
                            "carpetodd.command.playerInventoryStack.cleared"), true);
                            return 1;
                        }))
                .then(Commands.literal("list")
                        .executes(context -> {
                            Map<String, Integer> rules = CustomItemMaxStackSizeDataManager.INSTANCE.getCurrentData();
                            if (rules.isEmpty()) {
                                context.getSource().sendFailure(Component.translatable(
                                        "carpetodd.command.playerInventoryStack.empty"));
                            } else {
                                rules.forEach((predicate, count) -> context.getSource().sendSuccess(
                                        () -> Component.literal(predicate + " = " + count), false));
                            }
                            return 1;
                        })));
    }

    private static int set(CommandContext<CommandSourceStack> context, CommandBuildContext commandBuildContext) {
        String predicate = getRawArgument(context, "predicate");
        int count = IntegerArgumentType.getInteger(context, "count");
        try {
            CustomItemMaxStackSizeDataManager.INSTANCE.set(predicate, count, commandBuildContext);
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.translatable(
                    "carpetodd.command.playerInventoryStack.invalid", predicate));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                "carpetodd.command.playerInventoryStack.set", predicate, count), true);
        return 1;
    }

    private static int setFilledShulkerBox(CommandContext<CommandSourceStack> context) {
        int count = IntegerArgumentType.getInteger(context, "count");
        CustomItemMaxStackSizeDataManager.INSTANCE.setFilledShulkerBox(count);
        context.getSource().sendSuccess(() -> Component.translatable(
                "carpetodd.command.playerInventoryStack.set", CustomItemMaxStackSizeDataManager.FILLED_SHULKER_BOX, count), true);
        return 1;
    }

    private static String getRawArgument(CommandContext<CommandSourceStack> context, String name) {
        return context.getNodes().stream()
                .filter(node -> node.getNode().getName().equals(name))
                .findFirst()
                .map(node -> context.getInput().substring(node.getRange().getStart(), node.getRange().getEnd()))
                .orElseThrow();
    }
}
