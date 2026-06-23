package dev.rok.eve;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The +1 shulker box block. Extends the vanilla shulker box so it inherits all
 * of its behaviour — lid opening, collision, contents-preserving drops, the
 * entity renderer — but uses its own block entity (which relaxes the slot rule
 * to allow normal shulker boxes inside, but not +1 boxes).
 */
public class UpgradedShulkerBoxBlock extends ShulkerBoxBlock {
	public static final MapCodec<UpgradedShulkerBoxBlock> CODEC = simpleCodec(UpgradedShulkerBoxBlock::new);

	public UpgradedShulkerBoxBlock(Properties properties) {
		super(null, properties); // null colour => default (purple) shulker texture
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public MapCodec<ShulkerBoxBlock> codec() {
		return (MapCodec) CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UpgradedShulkerBoxBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, EVE.UPGRADED_SHULKER_BOX_BLOCK_ENTITY, ShulkerBoxBlockEntity::tick);
	}
}
