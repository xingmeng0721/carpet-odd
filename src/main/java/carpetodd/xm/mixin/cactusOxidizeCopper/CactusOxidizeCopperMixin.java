package carpetodd.xm.mixin.cactusOxidizeCopper;

import carpetodd.xm.CarpetOddSettings;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockItem.class)
public abstract class CactusOxidizeCopperMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void carpetOdd$oxidizeCopper(
            UseOnContext context,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!CarpetOddSettings.cactusOxidizeCopper) return;

        ItemStack tool = context.getItemInHand();
        if (!tool.is(Items.CACTUS)) return;

        Player player = context.getPlayer();
        if (player == null) return;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof WeatheringCopper copper)) return;

        Optional<BlockState> next = copper.getNext(state);

        BlockState newState = next.get();

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, tool);
        }

        level.setBlock(pos, newState, 11);

        level.gameEvent(
                GameEvent.BLOCK_CHANGE,
                pos,
                GameEvent.Context.of(player, newState)
        );

        level.playSound(
                null,
                pos,
                SoundEvents.AXE_SCRAPE,
                SoundSource.BLOCKS,
                0.7F,
                0.35F
        );

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
