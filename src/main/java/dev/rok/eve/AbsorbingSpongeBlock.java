package dev.rok.eve;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

/**
 * Endgame sponge: absorbs water like a vanilla sponge and, thanks to its
 * upgrade tier, lava as well (at a smaller radius). Unlike the vanilla
 * sponge it never gets wet — it is infinitely reusable.
 *
 * Absorb logic adapted from vanilla SpongeBlock, parametrized by fluid tag,
 * search depth and block budget.
 */
public class AbsorbingSpongeBlock extends Block implements EntityBlock {
	private static final Direction[] ALL_DIRECTIONS = Direction.values();

	public AbsorbingSpongeBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AbsorbingSpongeBlockEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		// One absorb per placement: components were just applied to the block
		// entity, but read the tier straight from the stack to be safe. To
		// absorb again, break and re-place the sponge.
		this.absorb(level, pos, Math.max(1, UpgradeHelper.getLevel(stack)));
	}

	private void absorb(Level level, BlockPos pos, int tier) {
		if (level.isClientSide()) {
			return;
		}
		int waterDepth = UpgradeHelper.spongeWaterDepth(tier);
		int lavaDepth = UpgradeHelper.spongeLavaDepth(tier);
		boolean absorbedWater = removeFluid(level, pos, FluidTags.WATER, waterDepth, 2 * waterDepth * waterDepth);
		boolean absorbedLava = removeFluid(level, pos, FluidTags.LAVA, lavaDepth, 2 * lavaDepth * lavaDepth);
		if (absorbedWater || absorbedLava) {
			level.playSound(null, pos, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	private static boolean removeFluid(Level level, BlockPos startPos, TagKey<Fluid> fluidTag, int maxDepth, int maxCount) {
		return BlockPos.breadthFirstTraversal(startPos, maxDepth, maxCount, (pos, consumer) -> {
			for (Direction direction : ALL_DIRECTIONS) {
				consumer.accept(pos.relative(direction));
			}
		}, pos -> {
			if (pos.equals(startPos)) {
				return BlockPos.TraversalNodeStatus.ACCEPT;
			} else {
				BlockState state = level.getBlockState(pos);
				FluidState fluidState = level.getFluidState(pos);
				if (!fluidState.is(fluidTag)) {
					return BlockPos.TraversalNodeStatus.SKIP;
				} else if (state.getBlock() instanceof BucketPickup bucketPickup
						&& !bucketPickup.pickupBlock(null, level, pos, state).isEmpty()) {
					return BlockPos.TraversalNodeStatus.ACCEPT;
				} else {
					if (state.getBlock() instanceof LiquidBlock) {
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					} else {
						if (!state.is(Blocks.KELP) && !state.is(Blocks.KELP_PLANT)
								&& !state.is(Blocks.SEAGRASS) && !state.is(Blocks.TALL_SEAGRASS)) {
							return BlockPos.TraversalNodeStatus.SKIP;
						}

						BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
						dropResources(state, level, pos, blockEntity);
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					}

					return BlockPos.TraversalNodeStatus.ACCEPT;
				}
			}
		}) > 1;
	}
}
