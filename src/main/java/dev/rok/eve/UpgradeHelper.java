package dev.rok.eve;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Applies upgrade levels to item stacks. Everything is done through vanilla
 * data components (attribute modifiers, tool rules, max damage), so upgraded
 * items behave correctly without any runtime hooks.
 *
 * Per level: +1 attack damage, +1 armor, +0.5 armor toughness,
 * +20% mining speed, +25% max durability.
 */
public final class UpgradeHelper {
	public static final int MAX_LEVEL = 10;
	public static final int SPONGE_MAX_LEVEL = 7;

	public static final double ATTACK_DAMAGE_PER_LEVEL = 1.0;
	public static final double ARMOR_PER_LEVEL = 1.0;
	public static final double TOUGHNESS_PER_LEVEL = 0.5;
	public static final float MINING_SPEED_PER_LEVEL = 0.2f;
	public static final float DURABILITY_PER_LEVEL = 0.25f;

	// Sponge absorb depth per tier (index = tier). Water starts at the vanilla
	// depth of 6; lava starts smaller; each tier alternately grows one of them.
	private static final int[] SPONGE_WATER_DEPTH = {6, 6, 8, 8, 10, 10, 12, 12};
	private static final int[] SPONGE_LAVA_DEPTH = {0, 3, 3, 5, 5, 7, 7, 9};

	private UpgradeHelper() {
	}

	public static int getLevel(ItemStack stack) {
		return stack.getOrDefault(EVE.UPGRADE_LEVEL, 0);
	}

	/** Some items cap below MAX_LEVEL; the smithing recipe checks this. */
	public static int maxLevel(ItemStack stack) {
		if (stack.is(Items.SPONGE) || stack.is(EVE.ABSORBING_SPONGE_ITEM)) {
			return SPONGE_MAX_LEVEL;
		}
		if (stack.is(Items.SHULKER_BOX) || stack.is(EVE.UPGRADED_SHULKER_BOX_ITEM)) {
			return 1; // shulker box only goes to +1
		}
		return MAX_LEVEL;
	}

	public static int spongeWaterDepth(int tier) {
		return SPONGE_WATER_DEPTH[Math.clamp(tier, 1, SPONGE_MAX_LEVEL)];
	}

	public static int spongeLavaDepth(int tier) {
		return SPONGE_LAVA_DEPTH[Math.clamp(tier, 1, SPONGE_MAX_LEVEL)];
	}

	public static ItemLore spongeLore(int tier) {
		return new ItemLore(List.of(
				Component.translatable("item.eve.upgrade_level", tier).withStyle(ChatFormatting.GOLD),
				Component.translatable("item.eve.sponge_radii", spongeWaterDepth(tier), spongeLavaDepth(tier))
						.withStyle(ChatFormatting.GRAY)));
	}

	public static ItemStack upgrade(ItemStack stack, int level) {
		stack.set(EVE.UPGRADE_LEVEL, level);
		applyAttributes(stack, level);
		applyMiningSpeed(stack, level);
		applyDurability(stack, level);
		if (stack.is(EVE.ABSORBING_SPONGE_ITEM)) {
			stack.set(DataComponents.LORE, spongeLore(level));
		} else {
			stack.set(DataComponents.LORE, new ItemLore(List.of(
					Component.translatable("item.eve.upgrade_level", level).withStyle(ChatFormatting.GOLD))));
		}
		return stack;
	}

	private static void applyAttributes(ItemStack stack, int level) {
		ItemAttributeModifiers current = stack.getOrDefault(
				DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

		// Keep everything that is not ours (item defaults, other mods), then re-add
		// our bonuses sized for the new level.
		List<ItemAttributeModifiers.Entry> entries = new ArrayList<>();
		for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
			if (!entry.modifier().id().getNamespace().equals(EVE.MOD_ID)) {
				entries.add(entry);
			}
		}

		// Weapons/tools: anything whose item defaults already deal attack damage.
		ItemAttributeModifiers defaults = stack.getItem().components()
				.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		boolean dealsDamage = defaults.modifiers().stream()
				.anyMatch(entry -> entry.attribute().equals(Attributes.ATTACK_DAMAGE));
		if (dealsDamage) {
			entries.add(new ItemAttributeModifiers.Entry(
					Attributes.ATTACK_DAMAGE,
					new AttributeModifier(EVE.id("upgrade_attack_damage"),
							ATTACK_DAMAGE_PER_LEVEL * level, AttributeModifier.Operation.ADD_VALUE),
					EquipmentSlotGroup.MAINHAND));
		}

		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (equippable != null && equippable.slot().isArmor()) {
			EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(equippable.slot());
			entries.add(new ItemAttributeModifiers.Entry(
					Attributes.ARMOR,
					new AttributeModifier(EVE.id("upgrade_armor"),
							ARMOR_PER_LEVEL * level, AttributeModifier.Operation.ADD_VALUE),
					slot));
			entries.add(new ItemAttributeModifiers.Entry(
					Attributes.ARMOR_TOUGHNESS,
					new AttributeModifier(EVE.id("upgrade_toughness"),
							TOUGHNESS_PER_LEVEL * level, AttributeModifier.Operation.ADD_VALUE),
					slot));
		}

		stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(entries));
	}

	private static void applyMiningSpeed(ItemStack stack, int level) {
		// Rebuild the tool rules from the item's pristine defaults so repeated
		// upgrades don't compound the multiplier.
		Tool base = stack.getItem().components().get(DataComponents.TOOL);
		if (base == null) {
			base = stack.get(DataComponents.TOOL);
		}
		if (base == null) {
			return;
		}

		float multiplier = 1.0f + MINING_SPEED_PER_LEVEL * level;
		List<Tool.Rule> rules = base.rules().stream()
				.map(rule -> new Tool.Rule(rule.blocks(),
						rule.speed().map(speed -> {
							// Swords have a Float.MAX_VALUE "instantly mines" rule; scaling it
							// overflows to Infinity, which fails the POSITIVE_FLOAT codec and
							// corrupts the stack. Keep such speeds as-is.
							float scaled = speed * multiplier;
							return Float.isFinite(scaled) ? scaled : speed;
						}),
						rule.correctForDrops()))
				.toList();

		stack.set(DataComponents.TOOL,
				new Tool(rules, base.defaultMiningSpeed(), base.damagePerBlock(), base.canDestroyBlocksInCreative()));
	}

	private static void applyDurability(ItemStack stack, int level) {
		Integer base = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
		if (base == null || base <= 0) {
			return;
		}
		stack.set(DataComponents.MAX_DAMAGE, Math.round(base * (1.0f + DURABILITY_PER_LEVEL * level)));
	}
}
