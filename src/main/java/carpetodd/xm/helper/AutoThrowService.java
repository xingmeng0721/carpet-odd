package carpetodd.xm.helper;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.HashSet;
import java.util.Set;

public class AutoThrowService {

    public static int processFakePlayer(ServerPlayer fakePlayer) {
        if (!(fakePlayer instanceof EntityPlayerMPFake)) {
            return 0;
        }

        var inventory = fakePlayer.getInventory();
        int droppedCount = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty() || !isShulkerBoxItem(stack)) {
                continue;
            }

            if (countUniqueItemTypes(stack) == 1) {
                ItemStack toDrop = stack.copy();
                inventory.setItem(i, ItemStack.EMPTY);

                ItemEntity droppedEntity = fakePlayer.drop(toDrop, false, true);
                if (droppedEntity != null) {
                    droppedEntity.setDefaultPickUpDelay();
                    droppedCount++;
                }
            }
        }

        return droppedCount;
    }

    private static int countUniqueItemTypes(ItemStack shulkerBox) {
        ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
        if (contents == null || contents == ItemContainerContents.EMPTY) {
            return 0;
        }
        Set<Item> uniqueTypes = new HashSet<>();
        //#if MC >= 26_01_00
        //$$ contents.nonEmptyItemCopyStream().forEach(item -> uniqueTypes.add(item.getItem()));
        //#else
        contents.nonEmptyStream().forEach(item -> uniqueTypes.add(item.getItem()));
        //#endif
        return uniqueTypes.size();
    }

    public static boolean isShulkerBoxItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(Items.SHULKER_BOX)) return true;
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof ShulkerBoxBlock;
        }
        return false;
    }
}
