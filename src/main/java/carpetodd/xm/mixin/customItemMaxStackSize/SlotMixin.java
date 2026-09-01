package carpetodd.xm.mixin.customItemMaxStackSize;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow @Final public Container container;
    @Shadow public abstract ItemStack getItem();
    @Shadow public abstract void setChanged();

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", 
            at = @At("RETURN"), cancellable = true)
    private void customSlotStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (this.container instanceof Inventory) {
            int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getCustomStackSize(stack);
            if (custom > 1) {
                cir.setReturnValue(custom);
            }
        }
    }

    @Inject(method = "safeInsert(Lnet/minecraft/world/item/ItemStack;I)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"), cancellable = true)
    private void customSafeInsert(ItemStack stack, int count, CallbackInfoReturnable<ItemStack> cir) {
        if (!(this.container instanceof Inventory)) return;
        
        int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getCustomStackSize(stack);
        if (custom <= 1) return;

        ItemStack existing = this.getItem();
        int toInsert = Math.min(count, stack.getCount());

        if (existing.isEmpty()) {
            // Empty slot - place items
            int placed = Math.min(toInsert, custom);
            this.container.setItem(((Slot)(Object)this).getContainerSlot(), stack.split(placed));
            this.setChanged();
            cir.setReturnValue(stack);
        } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
            // Merge with existing
            int space = custom - existing.getCount();
            int merged = Math.min(toInsert, space);
            if (merged > 0) {
                existing.grow(merged);
                stack.shrink(merged);
                this.setChanged();
            }
            cir.setReturnValue(stack);
        }
    }
}
