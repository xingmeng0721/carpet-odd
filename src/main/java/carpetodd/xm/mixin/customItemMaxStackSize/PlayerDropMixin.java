package carpetodd.xm.mixin.customItemMaxStackSize;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDropMixin {
    
    /**
     * 统一的自定义堆叠物品掉落处理方法（两参数版本）
     */
    @Unique
    private ItemEntity carpetOdd$splitAndDrop(ItemStack stack, boolean throwRandomly) {
        Player player = (Player) (Object) this;
        int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getCustomStackSize(stack);
        // Item's own limit, not stack.getMaxStackSize(): that now returns the custom value on both sides
        int vanillaMax = stack.getItem().getDefaultMaxStackSize();

        if (custom <= 1 || stack.getCount() <= vanillaMax) {
            return null;
        }

        ItemEntity firstEntity = null;
        
        while (stack.getCount() > vanillaMax) {
            ItemStack toDrop = stack.split(vanillaMax);
            ItemEntity entity = player.drop(toDrop, throwRandomly);
            if (firstEntity == null) {
                firstEntity = entity;
            }
        }
        
        if (!stack.isEmpty()) {
            ItemEntity entity = player.drop(stack.copy(), throwRandomly);
            stack.setCount(0);
            if (firstEntity == null) {
                firstEntity = entity;
            }
        }
        
        return firstEntity;
    }
    
    /**
     * 统一的自定义堆叠物品掉落处理方法（三参数版本，死亡掉落用）
     */
    @Unique
    private ItemEntity carpetOdd$splitAndDrop(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        Player player = (Player) (Object) this;
        int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getCustomStackSize(stack);
        int vanillaMax = stack.getItem().getDefaultMaxStackSize();

        if (custom <= 1 || stack.getCount() <= vanillaMax) {
            return null;
        }
        ItemEntity firstEntity = null;
        
        while (stack.getCount() > vanillaMax) {
            ItemStack toDrop = stack.split(vanillaMax);
            ItemEntity entity = ((net.minecraft.world.entity.LivingEntity)(Object)player).drop(toDrop, throwRandomly, retainOwnership);
            if (firstEntity == null) {
                firstEntity = entity;
            }
        }
        
        if (!stack.isEmpty()) {
            ItemEntity entity = ((net.minecraft.world.entity.LivingEntity)(Object)player).drop(stack.copy(), throwRandomly, retainOwnership);
            stack.setCount(0);
            if (firstEntity == null) {
                firstEntity = entity;
            }
        }
        
        return firstEntity;
    }
    
    /**
     * 拦截 Player.drop(ItemStack, boolean) - Q键和Ctrl+Q
     */
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"), cancellable = true)
    private void onPlayerDrop(ItemStack stack, boolean throwRandomly, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity result = carpetOdd$splitAndDrop(stack, throwRandomly);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
