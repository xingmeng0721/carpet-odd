package carpetodd.xm.mixin.customItemMaxStackSize;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
//#if MC < 1_21_05
//$$ import net.minecraft.world.entity.player.Player;
//#else
import net.minecraft.world.entity.LivingEntity;
//#endif
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC < 1_21_05
//$$ @Mixin(Player.class)
//#else
@Mixin(LivingEntity.class)
//#endif
public abstract class LivingEntityDropMixin {

    /**
     * 拦截 drop(ItemStack, boolean, boolean) - 死亡掉落用
     * 1.21.5+ 该方法在 LivingEntity 上；1.21.1~1.21.4 只在 Player 上
     */
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"), cancellable = true)
    private void onLivingEntityDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership, 
                                    CallbackInfoReturnable<ItemEntity> cir) {
        int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getCustomStackSize(stack);
        // Item's own limit, not stack.getMaxStackSize(): that now returns the custom value on both sides
        int vanillaMax = stack.getItem().getDefaultMaxStackSize();

        if (custom <= 1 || stack.getCount() <= vanillaMax) {
            return;
        }

        //#if MC < 1_21_05
        //$$ Player entity = (Player) (Object) this;
        //#else
        LivingEntity entity = (LivingEntity) (Object) this;
        //#endif
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
