package carpetodd.xm.mixin.customItemMaxStackSize;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public interface ContainerMixin {
    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
    private void customPlayerInventoryStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // Every slot of these containers is real player storage; third-party wrappers are handled
        // per-slot in SlotMixin instead (this method has no slot index to validate against).
        if ((Object) this instanceof Inventory || (Object) this instanceof PlayerEnderChestContainer) {
            int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getInventorySlotStackSize(stack);
            if (custom != -1) cir.setReturnValue(custom);
            return;
        }
        // Client-side stand-in: remote / fake-player inventories and the ender chest are backed by a plain
        // SimpleContainer here, whose setItem() clamps each synced stack to getMaxStackSize(stack). The server
        // only ever sends a stack larger than the vanilla limit into player storage, so an oversized incoming
        // stack that matches a rule is one of ours and must survive the sync clamp instead of being cut back.
        if (stack.getCount() > cir.getReturnValue()) {
            int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getInventorySlotStackSize(stack);
            if (custom != -1 && custom >= stack.getCount()) cir.setReturnValue(custom);
        }
    }
}
