package dev.rok.eve.compat.jei;

import dev.rok.eve.EVE;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class EVEJeiPlugin implements IModPlugin {
	@Override
	public Identifier getPluginUid() {
		return EVE.id("jei");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new GearUpgradeCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(GearUpgradeCategory.TYPE, UpgradeEntry.all());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		// Both the smithing table and every Upgrade Core point at the category.
		registration.addRecipeCatalysts(GearUpgradeCategory.TYPE, Blocks.SMITHING_TABLE);
		EVE.UPGRADE_CORES.forEach(core -> registration.addRecipeCatalysts(GearUpgradeCategory.TYPE, core));
	}
}
