package dev.rok.eve;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;

public class EVE implements ModInitializer {
	public static final String MOD_ID = "eve";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	/** Items in this tag can be put in the smithing table base slot for an upgrade. */
	public static final TagKey<Item> UPGRADABLE = TagKey.create(Registries.ITEM, id("upgradable"));

	/** Stores the current upgrade level (+1, +2, ...) on an item stack. */
	public static final DataComponentType<Integer> UPGRADE_LEVEL = DataComponentType.<Integer>builder()
			.persistent(Codec.intRange(0, UpgradeHelper.MAX_LEVEL))
			.networkSynchronized(ByteBufCodecs.VAR_INT)
			.build();

	/**
	 * One core per level, index 0 = "+1" core. Each core is crafted from two
	 * cores of the previous tier plus a catalyst, so the material cost roughly
	 * doubles with every level.
	 */
	public static final List<Item> UPGRADE_CORES = makeCores();

	public static final Item WING_SMITHING_TEMPLATE = registerItem("wing_smithing_template",
			new Item.Properties().rarity(Rarity.EPIC).fireResistant());

	/**
	 * Netherite chestplate fused with an elytra: full netherite protection plus
	 * elytra flight (vanilla glider component). The only chestplate that can go
	 * through the +N upgrade path.
	 */
	public static final Item WINGED_NETHERITE_CHESTPLATE = registerItem("winged_netherite_chestplate",
			new Item.Properties()
					.humanoidArmor(ArmorMaterials.NETHERITE, ArmorType.CHESTPLATE)
					.fireResistant()
					.rarity(Rarity.EPIC)
					.component(DataComponents.GLIDER, Unit.INSTANCE));

	public static final RecipeSerializer<UpgradeRecipe> UPGRADE_SERIALIZER =
			new RecipeSerializer<>(UpgradeRecipe.MAP_CODEC, UpgradeRecipe.STREAM_CODEC);

	private static Item registerItem(String name, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties.setId(key)));
	}

	private static List<Item> makeCores() {
		List<Item> cores = new ArrayList<>();
		for (int level = 1; level <= UpgradeHelper.MAX_LEVEL; level++) {
			cores.add(registerItem("upgrade_core_" + level,
					new Item.Properties().rarity(Rarity.RARE).fireResistant()));
		}
		return List.copyOf(cores);
	}

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("upgrade_level"), UPGRADE_LEVEL);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("upgrade"), UPGRADE_SERIALIZER);

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
			UPGRADE_CORES.forEach(output::accept);
			output.accept(WING_SMITHING_TEMPLATE);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(output ->
				output.accept(WINGED_NETHERITE_CHESTPLATE));
	}
}
