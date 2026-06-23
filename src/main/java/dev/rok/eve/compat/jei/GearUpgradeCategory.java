package dev.rok.eve.compat.jei;

import dev.rok.eve.EVE;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GearUpgradeCategory implements IRecipeCategory<UpgradeEntry> {
	public static final RecipeType<UpgradeEntry> TYPE =
			RecipeType.create(EVE.MOD_ID, "gear_upgrade", UpgradeEntry.class);

	private static final int WIDTH = 116;
	private static final int HEIGHT = 26;
	private static final int ARROW_X = 70;
	private static final int ARROW_Y = 5;

	private final IDrawable icon;
	private final IDrawableStatic arrow;

	public GearUpgradeCategory(IGuiHelper guiHelper) {
		this.icon = guiHelper.createDrawableItemLike(EVE.UPGRADE_CORES.get(0));
		this.arrow = guiHelper.getRecipeArrow();
	}

	@Override
	public RecipeType<UpgradeEntry> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.translatable("category.eve.gear_upgrade");
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public IDrawable getIcon() {
		return this.icon;
	}

	@Override
	public void draw(UpgradeEntry recipe, IRecipeSlotsView slotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		this.arrow.draw(graphics, ARROW_X, ARROW_Y);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, UpgradeEntry recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, 2, 5)
				.addItemStack(recipe.core());
		var base = builder.addSlot(RecipeIngredientRole.INPUT, 24, 5)
				.addItemStack(recipe.base());
		if (recipe.level() > 1) {
			base.addRichTooltipCallback((slot, tooltip) -> tooltip.add(
					Component.translatable("category.eve.gear_upgrade.requires", recipe.level() - 1)));
		}
		builder.addSlot(RecipeIngredientRole.INPUT, 46, 5)
				.addItemStack(recipe.catalyst());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 96, 5)
				.addItemStack(recipe.result());
	}
}
