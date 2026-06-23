package dev.rok.eve;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Smithing table recipe that takes any item from the eve:upgradable tag at
 * upgrade level (level - 1), the matching upgrade core as template, and a
 * per-level catalyst, and outputs the same item upgraded to {@code level}
 * with all its enchantments and damage preserved.
 *
 * (1.21.11 variant: implements SmithingRecipe directly — this version has no
 * SimpleSmithingRecipe/CommonInfo, and RecipeSerializer is an interface.)
 */
public class UpgradeRecipe implements SmithingRecipe {
	final Ingredient template;
	final Ingredient addition;
	final int level;
	private PlacementInfo placementInfo;

	public UpgradeRecipe(Ingredient template, Ingredient addition, int level) {
		this.template = template;
		this.addition = addition;
		this.level = level;
	}

	public int level() {
		return this.level;
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
	public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider provider) {
		ItemStack result = input.base().is(Items.SPONGE)
				? new ItemStack(EVE.ABSORBING_SPONGE_ITEM)
				: input.base().copyWithCount(1);
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
	public RecipeSerializer<? extends SmithingRecipe> getSerializer() {
		return EVE.UPGRADE_SERIALIZER;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (this.placementInfo == null) {
			this.placementInfo = PlacementInfo.createFromOptionals(List.of(
					Optional.of(this.template), Optional.of(this.baseIngredient()), Optional.of(this.addition)));
		}
		return this.placementInfo;
	}

	public static class Serializer implements RecipeSerializer<UpgradeRecipe> {
		private static final MapCodec<UpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
				Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
				Codec.intRange(1, UpgradeHelper.MAX_LEVEL).fieldOf("level").forGetter(recipe -> recipe.level)
		).apply(i, UpgradeRecipe::new));

		private static final StreamCodec<RegistryFriendlyByteBuf, UpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
				Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.template,
				Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.addition,
				ByteBufCodecs.VAR_INT, recipe -> recipe.level,
				UpgradeRecipe::new);

		@Override
		public MapCodec<UpgradeRecipe> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, UpgradeRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
