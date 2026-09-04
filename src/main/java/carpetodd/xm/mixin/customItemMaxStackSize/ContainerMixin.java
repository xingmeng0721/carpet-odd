package carpetodd.xm.mixin.customItemMaxStackSize;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import net.minecraft.world.Container;
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
        if ((Object) this instanceof Inventory) {
            int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getInventorySlotStackSize(stack);
            if (custom != -1) cir.setReturnValue(custom);
        }
    }
}
