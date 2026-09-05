package carpetodd.xm.network;

import carpetodd.xm.CarpetOddSettings;
import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public final class OddNetwork {

    private static MinecraftServer server;

    private OddNetwork() {}

    /** Called from mod init on both physical sides; S2C types must be registered before any send/receive. */
    public static void register() {
        //#if MC >= 26_01_00
        //$$ PayloadTypeRegistry.clientboundPlay().register(SyncCustomStackSizePayload.TYPE, SyncCustomStackSizePayload.CODEC);
        //#else
        PayloadTypeRegistry.playS2C().register(SyncCustomStackSizePayload.TYPE, SyncCustomStackSizePayload.CODEC);
        //#endif
    }

    /** Server-side: remembers the server and pushes the current rules to each player as they join. */
    public static void registerServerHooks() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, joinedServer) -> {
            server = joinedServer;
            sendTo(handler.getPlayer());
        });
    }

    public static void sendTo(ServerPlayer player) {
        if (player == null) return;
        if (!ServerPlayNetworking.canSend(player, SyncCustomStackSizePayload.TYPE)) return;
        Map<String, Integer> data = CustomItemMaxStackSizeDataManager.INSTANCE.getCurrentData();
        ServerPlayNetworking.send(player,
                new SyncCustomStackSizePayload(CarpetOddSettings.playerInventoryStack, data));
    }

    public static void broadcast() {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTo(player);
        }
    }
}
