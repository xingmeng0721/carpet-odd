package carpetodd.xm.mixin.client;

import carpetodd.xm.utils.compat.DummyClass;
import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 26_01_00
//$$ import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
//$$ import net.minecraft.world.item.ItemInstance;
//$$ import net.minecraft.world.item.ItemStack;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

/**
 * Client-side only: 26.1 moved getMaxStackSize() onto the ItemInstance interface as a default method,
 * so ItemStack no longer declares it and the override has to target the interface.
 * Below 26.1 this file is a no-op dummy (see ItemStackMaxStackSizeMixin instead).
 */
//#if MC >= 26_01_00
//$$ @Mixin(value = ItemInstance.class, priority = 999)
//#else
@Mixin(DummyClass.class)
//#endif
public interface ItemInstanceMaxStackSizeMixin {

    //#if MC >= 26_01_00
    //$$ @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    //$$ private void carpetOdd$applyCustomLimit(CallbackInfoReturnable<Integer> cir) {
    //$$     if (!(((Object) this) instanceof ItemStack stack)) return;
    //$$     if (cir.getReturnValue() != stack.getItem().getDefaultMaxStackSize()) return;
    //$$     int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getClientCustomStackSize(stack);
    //$$     if (custom != -1) {
    //$$         cir.setReturnValue(custom);
    //$$     }
    //$$ }
    //#endif
}
