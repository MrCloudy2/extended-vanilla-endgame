package dev.rok.eve;

import org.jspecify.annotations.Nullable;

import dev.rok.eve.mixin.BlockEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A real shulker box (extends the vanilla block entity, so it keeps the lid
 * animation, contents-in-item drops, and renderer) with two changes: it
 * registers under its own block entity type, and its menu/automation accept
 * normal shulker boxes — but never another upgraded shulker box.
 */
public class UpgradedShulkerBoxBlockEntity extends ShulkerBoxBlockEntity {
	public UpgradedShulkerBoxBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
		// The vanilla constructor hardcodes BlockEntityType.SHULKER_BOX; correct it.
		((BlockEntityAccessor) this).eve$setType(EVE.UPGRADED_SHULKER_BOX_BLOCK_ENTITY);
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new UpgradedShulkerBoxMenu(containerId, inventory, this);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return !EVE.isUpgradedShulkerBox(stack);
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
		// Hoppers may insert normal shulker boxes and any item, but not a +1 box.
		return !EVE.isUpgradedShulkerBox(stack);
	}
}
