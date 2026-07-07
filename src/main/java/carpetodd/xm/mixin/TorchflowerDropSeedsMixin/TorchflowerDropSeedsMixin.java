package carpetodd.xm.mixin.TorchflowerDropSeedsMixin;

import carpetodd.xm.CarpetOddSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class TorchflowerDropSeedsMixin {

    @Inject(
            method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private static void carpetOdd$dropExtraSeeds(
            BlockState state,
            Level level,
            BlockPos pos,
            @Nullable BlockEntity blockEntity,
            @Nullable Entity entity,
            ItemStack tool,
            CallbackInfo ci
    ) {
        if (!CarpetOddSettings.torchflowerDropSeeds || level.isClientSide()) {
            return;
        }

        if (!state.is(Blocks.TORCHFLOWER)) {
            return;
        }

        Block.popResource(
                level,
                pos,
                new ItemStack(
                        Items.TORCHFLOWER_SEEDS,
                        level.random.nextInt(3) + 1
                )
        );
    }
}