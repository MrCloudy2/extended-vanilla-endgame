package dev.rok.eve;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

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

	/**
	 * Endgame sponge, max +7: absorbs water, and lava from +1 up; never turns
	 * wet, and the item is fireproof so it survives a swim in its own prey.
	 */
	public static final AbsorbingSpongeBlock ABSORBING_SPONGE = registerSpongeBlock();
	public static final Item ABSORBING_SPONGE_ITEM = registerSpongeItem();
	public static final BlockEntityType<AbsorbingSpongeBlockEntity> ABSORBING_SPONGE_BLOCK_ENTITY =
			FabricBlockEntityTypeBuilder.create(AbsorbingSpongeBlockEntity::new, ABSORBING_SPONGE).build();

	public static final RecipeSerializer<UpgradeRecipe> UPGRADE_SERIALIZER = new UpgradeRecipe.Serializer();

	/**
	 * The per-level smithing catalyst, index 0 = the +1 catalyst. Used by the
	 * JEI integration for display; this MUST mirror the additions in the
	 * data/eve/recipe/upgrade_*.json files (which are authoritative for play).
	 */
	public static final List<Item> UPGRADE_CATALYSTS = List.of(
			Items.IRON_BLOCK, Items.EMERALD_BLOCK, Items.GOLD_BLOCK, Items.DIAMOND, Items.DIAMOND_BLOCK,
			Items.NETHERITE_INGOT, Items.NETHERITE_BLOCK, Items.ENCHANTED_GOLDEN_APPLE, Items.NETHER_STAR,
			Items.HEAVY_CORE);

	private static Item registerItem(String name, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(properties.setId(key)));
	}

	private static AbsorbingSpongeBlock registerSpongeBlock() {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id("absorbing_sponge"));
		return Registry.register(BuiltInRegistries.BLOCK, key, new AbsorbingSpongeBlock(
				BlockBehaviour.Properties.of()
						.mapColor(MapColor.COLOR_CYAN)
						.strength(0.6F)
						.sound(SoundType.SPONGE)
						.setId(key)));
	}

	private static Item registerSpongeItem() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id("absorbing_sponge"));
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(ABSORBING_SPONGE,
				new Item.Properties()
						.useBlockDescriptionPrefix()
						.fireResistant()
						.rarity(Rarity.RARE)
						.setId(key)));
	}

	private static List<Item> makeCores() {
		List<Item> cores = new ArrayList<>();
		for (int level = 1; level <= UpgradeHelper.MAX_LEVEL; level++) {
			int tier = level;
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id("upgrade_core_" + level));
			Item core = Registry.register(BuiltInRegistries.ITEM, key, new UpgradeCoreItem(
					new Item.Properties().rarity(Rarity.RARE).fireResistant().setId(key), tier));
			cores.add(core);
		}
		return List.copyOf(cores);
	}

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("upgrade_level"), UPGRADE_LEVEL);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("upgrade"), UPGRADE_SERIALIZER);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("absorbing_sponge"), ABSORBING_SPONGE_BLOCK_ENTITY);

		ItemGroupEvents.modifyEntriesEvent(tab("ingredients")).register(entries -> {
			UPGRADE_CORES.forEach(core -> entries.accept(new ItemStack(core)));
			entries.accept(new ItemStack(WING_SMITHING_TEMPLATE));
		});
		ItemGroupEvents.modifyEntriesEvent(tab("combat")).register(entries ->
				entries.accept(new ItemStack(WINGED_NETHERITE_CHESTPLATE)));
		ItemGroupEvents.modifyEntriesEvent(tab("functional_blocks")).register(entries ->
				entries.accept(new ItemStack(ABSORBING_SPONGE_ITEM)));
	}

	/** Vanilla creative-tab fields are private in this version, so build the key from its id. */
	private static ResourceKey<CreativeModeTab> tab(String id) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(id));
	}
}
