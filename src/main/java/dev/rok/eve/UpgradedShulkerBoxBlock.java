package dev.rok.eve;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The upgraded shulker box block. Extends the vanilla shulker box so it inherits
 * all of its behaviour — lid opening, collision, contents-preserving drops, the
 * entity renderer — but uses its own block entity (which relaxes the slot rule
 * to allow normal shulker boxes inside, but not upgraded boxes). One block per
 * dye colour, plus the default.
 */
public class UpgradedShulkerBoxBlock extends ShulkerBoxBlock {
	public static final MapCodec<UpgradedShulkerBoxBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DyeColor.CODEC.optionalFieldOf("color").forGetter(b -> Optional.ofNullable(b.getColor())),
			propertiesCodec()
	).apply(i, (color, properties) -> new UpgradedShulkerBoxBlock(color.orElse(null), properties)));

	public UpgradedShulkerBoxBlock(@Nullable DyeColor color, Properties properties) {
		super(color, properties);
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
