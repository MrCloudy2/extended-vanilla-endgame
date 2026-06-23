package dev.rok.eve;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The +1 shulker box block: a static-model container (renders like a normal
 * block, no special renderer) that opens a generic chest menu, so it can store
 * shulker boxes and any other item. Keeps its contents in the dropped item like
 * a vanilla shulker box.
 */
public class UpgradedShulkerBoxBlock extends BaseEntityBlock {
	public static final MapCodec<UpgradedShulkerBoxBlock> CODEC = simpleCodec(UpgradedShulkerBoxBlock::new);

	public UpgradedShulkerBoxBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UpgradedShulkerBoxBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof UpgradedShulkerBoxBlockEntity box) {
			player.openMenu(box);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		// Update comparators only — do NOT spill contents; they travel with the item drop.
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// In creative, hand back the filled box instead of losing the contents.
		if (level.getBlockEntity(pos) instanceof UpgradedShulkerBoxBlockEntity box
				&& !level.isClientSide() && player.preventsBlockDrops() && !box.isEmpty()) {
			ItemStack stack = new ItemStack(this);
			stack.applyComponents(box.collectComponents());
			ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
			drop.setDefaultPickUpDelay();
			level.addFreshEntity(drop);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
