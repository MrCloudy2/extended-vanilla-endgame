package dev.rok.eve;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleSmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Smithing table recipe that takes any item from the eve:upgradable tag at
 * upgrade level (level - 1), the matching upgrade core as template, and a
 * per-level catalyst, and outputs the same item upgraded to {@code level}
 * with all its enchantments and damage preserved.
 */
public class UpgradeRecipe extends SimpleSmithingRecipe {
	public static final MapCodec<UpgradeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
			Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
			Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
			Codec.intRange(1, UpgradeHelper.MAX_LEVEL).fieldOf("level").forGetter(recipe -> recipe.level)
	).apply(i, UpgradeRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
			Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.template,
			Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.addition,
			ByteBufCodecs.VAR_INT, recipe -> recipe.level,
			UpgradeRecipe::new);

	private final Ingredient template;
	private final Ingredient addition;
	private final int level;

	public UpgradeRecipe(Recipe.CommonInfo commonInfo, Ingredient template, Ingredient addition, int level) {
		super(commonInfo);
		this.template = template;
		this.addition = addition;
		this.level = level;
	}

	@Override
	public boolean matches(SmithingRecipeInput input, Level level) {
		return this.template.test(input.template())
				&& input.base().is(EVE.UPGRADABLE)
				&& this.level <= UpgradeHelper.maxLevel(input.base())
				&& UpgradeHelper.getLevel(input.base()) == this.level - 1
				&& this.addition.test(input.addition());
	}

	@Override
	public ItemStack assemble(SmithingRecipeInput input) {
		// A vanilla sponge becomes the absorbing sponge, and a vanilla shulker box
		// becomes the upgraded shulker box (carrying its contents over); everything
		// else keeps its identity.
		ItemStack base = input.base();
		ItemStack result;
		if (base.is(Items.SPONGE)) {
			result = new ItemStack(EVE.ABSORBING_SPONGE_ITEM);
		} else if (base.is(Items.SHULKER_BOX)) {
			result = new ItemStack(EVE.UPGRADED_SHULKER_BOX_ITEM);
			net.minecraft.world.item.component.ItemContainerContents contents =
					base.get(net.minecraft.core.component.DataComponents.CONTAINER);
			if (contents != null) {
				result.set(net.minecraft.core.component.DataComponents.CONTAINER, contents);
			}
		} else {
			result = base.copyWithCount(1);
		}
		return UpgradeHelper.upgrade(result, this.level);
	}

	@Override
	public Optional<Ingredient> templateIngredient() {
		return Optional.of(this.template);
	}

	@Override
	public Ingredient baseIngredient() {
		return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(EVE.UPGRADABLE));
	}

	@Override
	public Optional<Ingredient> additionIngredient() {
		return Optional.of(this.addition);
	}

	@Override
	public RecipeSerializer<UpgradeRecipe> getSerializer() {
		return EVE.UPGRADE_SERIALIZER;
	}

	@Override
	protected PlacementInfo createPlacementInfo() {
		return PlacementInfo.createFromOptionals(List.of(
				Optional.of(this.template), Optional.of(this.baseIngredient()), Optional.of(this.addition)));
	}
}
