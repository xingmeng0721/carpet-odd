package carpetodd.xm.mixin.customItemMaxStackSize;

import carpetodd.xm.manager.CustomItemMaxStackSizeDataManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow public abstract ItemStack getItem(int slot);
    @Shadow public abstract void setItem(int slot, ItemStack stack);
    @Shadow public abstract int getFreeSlot();
    @Shadow public int selected;

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void customAdd(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        int custom = CustomItemMaxStackSizeDataManager.INSTANCE.getCustomStackSize(stack);
        if (custom <= 1) return; // Let vanilla handle


        int remaining = stack.getCount();


        if (slot >= 0 && slot < 36) {
            ItemStack existing = this.getItem(slot);
            if (existing.isEmpty()) {
                int placed = Math.min(remaining, custom);
                this.setItem(slot, stack.copyWithCount(placed));
                remaining -= placed;
            } else if (ItemStack.isSameItemSameComponents(stack, existing)) {
                int space = custom - existing.getCount();
                int merged = Math.min(remaining, space);
                existing.grow(merged);
                remaining -= merged;
            }
        }


        if (remaining > 0) {
            ItemStack existing = this.getItem(this.selected);
            if (ItemStack.isSameItemSameComponents(stack, existing) && existing.getCount() < custom) {
                int merged = Math.min(remaining, custom - existing.getCount());
                existing.grow(merged);
                remaining -= merged;
            }
        }


        if (remaining > 0) {
            for (int i = 0; i < 36; i++) {
                ItemStack existing = this.getItem(i);
                if (existing.isEmpty()) {
                    int placed = Math.min(remaining, custom);
                    this.setItem(i, stack.copyWithCount(placed));
                    remaining -= placed;
                    if (remaining <= 0) break;
                } else if (ItemStack.isSameItemSameComponents(stack, existing) && existing.getCount() < custom) {
                    int merged = Math.min(remaining, custom - existing.getCount());
                    existing.grow(merged);
                    remaining -= merged;
                    if (remaining <= 0) break;
                }
            }
        }

        stack.setCount(remaining);
        cir.setReturnValue(remaining == 0 || remaining < stack.getCount());
    }
}

