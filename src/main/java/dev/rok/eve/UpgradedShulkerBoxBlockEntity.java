package dev.rok.eve;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A 27-slot container that, unlike a vanilla shulker box, uses a generic chest
 * menu — so its slots accept any item, including (upgraded and normal) shulker
 * boxes. Contents round-trip into the item via the inherited
 * {@code minecraft:container} component handling, so it behaves like a shulker
 * box: break it and the items come with it.
 */
public class UpgradedShulkerBoxBlockEntity extends RandomizableContainerBlockEntity {
	private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

	public UpgradedShulkerBoxBlockEntity(BlockPos pos, BlockState state) {
		super(EVE.UPGRADED_SHULKER_BOX_BLOCK_ENTITY, pos, state);
	}

	@Override
	public int getContainerSize() {
		return 27;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.eve.upgraded_shulker_box");
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new UpgradedShulkerBoxMenu(containerId, inventory, this);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		// Mirror the menu rule for hoppers/automation: no nesting +1 boxes.
		return !stack.is(EVE.UPGRADED_SHULKER_BOX_ITEM);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(input)) {
			ContainerHelper.loadAllItems(input, this.items);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!this.trySaveLootTable(output)) {
			ContainerHelper.saveAllItems(output, this.items, false);
		}
	}
}
