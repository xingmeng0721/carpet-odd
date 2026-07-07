package carpetodd.xm.command;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.Messenger;
import carpetodd.xm.CarpetOddSettings;
import carpetodd.xm.helper.AutoThrowService;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class AutoDropCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!CarpetOddSettings.autoDrop) {
            Messenger.m(source, "r autoDrop rule is not enabled, use /carpet autoDrop true");
            return 0;
        }

        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (target == null) {
            Messenger.m(source, "r Player not found: " + playerName);
            return 0;
        }

        if (!(target instanceof EntityPlayerMPFake)) {
            Messenger.m(source, "r " + playerName + " is not a fake player");
            return 0;
        }

        int count = AutoThrowService.processFakePlayer(target);

        if (count > 0) {
            Messenger.m(source, "g Dropped " + count + " shulker box(es)");
        } else {
            Messenger.m(source, "c No qualifying shulker boxes found");
        }

        return count;
    }
}
