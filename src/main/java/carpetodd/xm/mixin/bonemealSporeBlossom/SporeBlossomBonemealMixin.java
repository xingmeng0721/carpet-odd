package carpetodd.xm.mixin.bonemealSporeBlossom;

import carpetodd.xm.CarpetOddSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SporeBlossomBlock.class)
public abstract class SporeBlossomBonemealMixin extends Block implements BonemealableBlock {

    protected SporeBlossomBonemealMixin(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (!CarpetOddSettings.bonemealSporeBlossom) {
            return false;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(dir);
            if (canPlaceSporeBlossom(level, neighbor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int placed = 0;
        for (int attempt = 0; attempt < 15; attempt++) {
            BlockPos target = pos.offset(
                    random.nextInt(5) - 2,
                    random.nextInt(3) - 1,
                    random.nextInt(5) - 2
            );
            if (target.equals(pos)) continue;
            if (canPlaceSporeBlossom(level, target)) {
                level.setBlock(target, state, 3);
                placed++;
                if (placed >= 3) break;
            }
        }
    }

    @Override
    public BonemealableBlock.Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }

    private static boolean canPlaceSporeBlossom(LevelReader level, BlockPos pos) {
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && !existing.canBeReplaced()) {
            return false;
        }
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        VoxelShape shape = aboveState.getCollisionShape(level, above);
        return Block.isFaceFull(shape, Direction.DOWN);
    }
}
