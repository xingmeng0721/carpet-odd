package carpetodd.xm.mixin.customItemMaxStackSize;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    // Removed global isStackable/getMaxStackSize injection
    // to keep vanilla behavior in chests, ground, etc.
}
