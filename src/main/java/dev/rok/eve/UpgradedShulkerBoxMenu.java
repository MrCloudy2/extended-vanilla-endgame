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
 * A 27-slot container menu like a chest's — so it accepts normal items and
 * vanilla shulker boxes — except its box slots reject the upgraded shulker box
 * itself, so you can't nest a +1 box inside a +1 box.
 *
 * Reuses the vanilla GENERIC_9x3 menu type, so the client renders it with the
 * standard chest screen and no client-side code is needed; the slot rule is
 * enforced server-side (authoritative).
 */
public class UpgradedShulkerBoxMenu extends AbstractContainerMenu {
	private static final int ROWS = 3;
	private static final int BOX_SLOTS = ROWS * 9;

	private final Container container;

	public UpgradedShulkerBoxMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory, new SimpleContainer(BOX_SLOTS));
	}

	public UpgradedShulkerBoxMenu(int containerId, Inventory playerInventory, Container container) {
		super(MenuType.GENERIC_9x3, containerId);
		checkContainerSize(container, BOX_SLOTS);
		this.container = container;
		container.startOpen(playerInventory.player);

		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < 9; col++) {
				this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18) {
					@Override
					public boolean mayPlace(ItemStack stack) {
						// Everything but another upgraded shulker box (no +1-in-+1 nesting).
						return !stack.is(EVE.UPGRADED_SHULKER_BOX_ITEM);
					}
				});
			}
		}
		this.addStandardInventorySlots(playerInventory, 8, 18 + ROWS * 18 + 13);
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
