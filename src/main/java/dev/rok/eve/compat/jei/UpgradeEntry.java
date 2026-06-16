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
 * One JEI entry per upgrade level: the Upgrade Core for the tier, the catalyst,
 * and the set of upgradable items shown going from +(level-1) to +level.
 */
public record UpgradeEntry(int level, ItemStack core, ItemStack catalyst,
		List<ItemStack> bases, List<ItemStack> results) {

	public static List<UpgradeEntry> all() {
		List<UpgradeEntry> entries = new ArrayList<>();
		for (int level = 1; level <= UpgradeHelper.MAX_LEVEL; level++) {
			List<ItemStack> bases = new ArrayList<>();
			List<ItemStack> results = new ArrayList<>();
			for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(EVE.UPGRADABLE)) {
				Item item = holder.value();
				if (UpgradeHelper.maxLevel(new ItemStack(item)) < level) {
					continue;
				}
				// A plain sponge only enters the system at +1 (transforming into the
				// absorbing sponge); the absorbing sponge cannot be a +0 base.
				if (item == Items.SPONGE && level != 1) {
					continue;
				}
				if (item == EVE.ABSORBING_SPONGE_ITEM && level == 1) {
					continue;
				}

				bases.add(level == 1 ? new ItemStack(item) : UpgradeHelper.upgrade(new ItemStack(item), level - 1));
				Item resultItem = item == Items.SPONGE ? EVE.ABSORBING_SPONGE_ITEM : item;
				results.add(UpgradeHelper.upgrade(new ItemStack(resultItem), level));
			}
			entries.add(new UpgradeEntry(level,
					new ItemStack(EVE.UPGRADE_CORES.get(level - 1)),
					new ItemStack(EVE.UPGRADE_CATALYSTS.get(level - 1)),
					bases, results));
		}
		return entries;
	}
}
