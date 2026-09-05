package carpetodd.xm.mixin.client;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import carpetodd.xm.utils.compat.DummyClass;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
//#if MC < 26_01_00
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

//#if MC >= 26_01_00
//$$ @Mixin(DummyClass.class)
//#else
@Mixin(value = ItemStack.class, priority = 999)
//#endif
public abstract class ItemStackMaxStackSizeMixin {

    //#if MC < 26_01_00
    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void carpetOdd$applyCustomLimit(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (cir.getReturnValue() != self.getItem().getDefaultMaxStackSize()) return;
        int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getClientCustomStackSize(self);
        if (custom != -1) {
            cir.setReturnValue(custom);
        }
    }
    //#endif
}
