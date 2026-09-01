package carpetodd.xm.mixin.customItemMaxStackSize;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDropMixin {
    
    /**
     * 拦截 LivingEntity.drop(ItemStack, boolean, boolean) - 死亡掉落用
     */
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"), cancellable = true)
    private void onLivingEntityDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, 
                                    CallbackInfoReturnable<ItemEntity> cir) {
        int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getCustomStackSize(stack);
        
        if (custom <= 1 || stack.getCount() <= stack.getMaxStackSize()) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) (Object) this;
        int vanillaMax = stack.getMaxStackSize();
        ItemEntity firstEntity = null;
        
        while (stack.getCount() > vanillaMax) {
            ItemStack toDrop = stack.split(vanillaMax);
            ItemEntity dropped = entity.drop(toDrop, throwRandomly, retainOwnership);
            if (firstEntity == null) {
                firstEntity = dropped;
            }
        }
        
        if (!stack.isEmpty()) {
            ItemEntity dropped = entity.drop(stack.copy(), throwRandomly, retainOwnership);
            stack.setCount(0);
            if (firstEntity == null) {
                firstEntity = dropped;
            }
        }
        
        cir.setReturnValue(firstEntity);
    }
}
