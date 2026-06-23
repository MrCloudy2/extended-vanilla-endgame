package dev.rok.eve;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 27-slot menu for the upgraded shulker box. Its slots accept normal items and
 * vanilla shulker boxes but reject another upgraded box, so a +1 box can't be
 * nested inside a +1 box.
 *
 * It reuses the vanilla GENERIC_9x3 menu type (chest screen, no client code).
 * The vanilla shulker menu can't be reused here because its slots reject ALL
 * shulker boxes on the client; the rule is enforced server-side instead.
 */
public class UpgradedShulkerBoxMenu extends AbstractContainerMenu {
	private static final int BOX_SLOTS = 27;

	private final Container container;

	public UpgradedShulkerBoxMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(BOX_SLOTS));
	}

	public UpgradedShulkerBoxMenu(int containerId, Inventory playerInventory, Container container) {
		super(MenuType.GENERIC_9x3, containerId);
		checkContainerSize(container, BOX_SLOTS);
		this.container = container;
		container.startOpen(playerInventory.player);

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						return !stack.is(EVE.UPGRADED_SHULKER_BOX_ITEM);
					}
				});
			}
		}
		this.addStandardInventorySlots(playerInventory, 8, 84);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack moved = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			moved = stack.copy();
			if (slotIndex < BOX_SLOTS) {
				if (!this.moveItemStackTo(stack, BOX_SLOTS, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(stack, 0, BOX_SLOTS, false)) {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return moved;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);
	}
}
