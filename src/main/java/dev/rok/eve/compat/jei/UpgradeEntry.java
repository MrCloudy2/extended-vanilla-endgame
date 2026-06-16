package dev.rok.eve.compat.jei;

import java.util.ArrayList;
import java.util.List;

import dev.rok.eve.EVE;
import dev.rok.eve.UpgradeHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * One JEI entry per (upgradable item, level): the Upgrade Core and catalyst for
 * the tier, the item at +(level-1), and the item at +level. Keeping base and
 * result as single paired stacks (rather than two independently-cycling lists)
 * means focusing on an item in JEI shows its own before/after, never mismatched.
 */
public record UpgradeEntry(int level, ItemStack core, ItemStack catalyst, ItemStack base, ItemStack result) {

	public static List<UpgradeEntry> all() {
		List<UpgradeEntry> entries = new ArrayList<>();
		for (int level = 1; level <= UpgradeHelper.MAX_LEVEL; level++) {
			ItemStack core = new ItemStack(EVE.UPGRADE_CORES.get(level - 1));
			ItemStack catalyst = new ItemStack(EVE.UPGRADE_CATALYSTS.get(level - 1));
			for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(EVE.UPGRADABLE)) {
				Item item = holder.value();
				if (UpgradeHelper.maxLevel(new ItemStack(item)) < level) {
					continue;
				}
				// A plain sponge only enters the system at +1 (becoming the absorbing
				// sponge); the absorbing sponge cannot be a +0 base.
				if (item == Items.SPONGE && level != 1) {
					continue;
				}
				if (item == EVE.ABSORBING_SPONGE_ITEM && level == 1) {
					continue;
				}

				ItemStack base = level == 1 ? new ItemStack(item) : UpgradeHelper.upgrade(new ItemStack(item), level - 1);
				Item resultItem = item == Items.SPONGE ? EVE.ABSORBING_SPONGE_ITEM : item;
				ItemStack result = UpgradeHelper.upgrade(new ItemStack(resultItem), level);
				entries.add(new UpgradeEntry(level, core, catalyst, base, result));
			}
		}
		return entries;
	}
}
