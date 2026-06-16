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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;

/**
 * Endgame sponge: absorbs water like a vanilla sponge and, thanks to its
 * upgrade tier, lava as well (at a smaller radius).
 *
 * Behaves like a vanilla sponge: it sits dry until liquid first touches it,
 * then absorbs once and turns wet (a texture change via the SATURATED state).
 * A wet sponge never absorbs again. Unlike vanilla, breaking it always drops
 * the dry item (saturation lives only on the placed block), so it is reusable
 * — re-place it and it absorbs again on the next liquid contact.
 *
 * Absorb logic adapted from vanilla SpongeBlock, parametrized by fluid tag,
 * search depth and block budget.
 */
public class AbsorbingSpongeBlock extends Block implements EntityBlock {
	public static final BooleanProperty SATURATED = BooleanProperty.create("saturated");

	private static final Direction[] ALL_DIRECTIONS = Direction.values();

	public AbsorbingSpongeBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(SATURATED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SATURATED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AbsorbingSpongeBlockEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		// Covers placing the sponge directly into liquid. In dry air this
		// absorbs nothing and leaves the sponge dry, waiting for liquid.
		// Read the tier from the stack since the block entity was just populated.
		this.tryAbsorb(level, pos, state, Math.max(1, UpgradeHelper.getLevel(stack)));
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			@Nullable Orientation orientation, boolean movedByPiston) {
		// First contact with liquid that flows in after placement. Once wet, stop.
		if (!state.getValue(SATURATED) && level.getBlockEntity(pos) instanceof AbsorbingSpongeBlockEntity sponge) {
			this.tryAbsorb(level, pos, state, sponge.getTier());
		}
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
	}

	/** Absorbs surrounding fluid once; if anything was absorbed, turns the sponge wet. */
	private void tryAbsorb(Level level, BlockPos pos, BlockState state, int tier) {
		if (level.isClientSide() || state.getValue(SATURATED)) {
			return;
		}
		int waterDepth = UpgradeHelper.spongeWaterDepth(tier);
		int lavaDepth = UpgradeHelper.spongeLavaDepth(tier);
		boolean absorbedWater = removeFluid(level, pos, FluidTags.WATER, waterDepth, 2 * waterDepth * waterDepth);
		boolean absorbedLava = removeFluid(level, pos, FluidTags.LAVA, lavaDepth, 2 * lavaDepth * lavaDepth);
		if (absorbedWater || absorbedLava) {
			// Same-block setBlock keeps the block entity (and its tier) intact.
			level.setBlock(pos, state.setValue(SATURATED, true), Block.UPDATE_CLIENTS);
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
