package carpetodd.xm.mixin.bonemealSporeBlossom;

import carpetodd.xm.CarpetOddSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void odd$dropSporeBlossomOnBonemeal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!CarpetOddSettings.bonemealSporeBlossom) return;
        if (!(context.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SporeBlossomBlock)) return;

        Block.popResource(level, pos, new ItemStack(state.getBlock()));

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        level.levelEvent(1505, pos, 0);

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}