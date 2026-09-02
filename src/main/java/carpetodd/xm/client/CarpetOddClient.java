package carpetodd.xm.client;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import carpetodd.xm.network.SyncCustomStackSizePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.commands.CommandBuildContext;

public final class CarpetOddClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Only apply the custom limit on the client render thread while the inventory screen is open.
        // - Not on other threads: in single-player/LAN the integrated server shares this JVM, so the
        //   mixin also fires on server threads (ground ItemEntity merging, entity logic) otherwise.
        // - Not with no screen: walking around must keep vanilla stacking for world entities.
        CustomItemMaxStackSizeDataManager.INSTANCE.setClientInventoryView(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (!mc.isSameThread()) return false;
            //#if MC >= 26_02_00
            //$$ return mc.gui.screen() instanceof InventoryScreen;
            //#else
            return mc.screen instanceof InventoryScreen;
            //#endif
        });

        // Payload type registration happens in CarpetOddMod.onInitialize on both sides.
        ClientPlayNetworking.registerGlobalReceiver(SyncCustomStackSizePayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft.getConnection() == null) return;
                    CommandBuildContext buildContext = CommandBuildContext.simple(
                            minecraft.getConnection().registryAccess(),
                            minecraft.getConnection().enabledFeatures());
                    CustomItemMaxStackSizeDataManager.INSTANCE.applyClientRules(
                            payload.enabled(), payload.customStacks(), buildContext);
                }));
    }
}
