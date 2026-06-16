package dev.rok.eve;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * An Upgrade Core for a specific tier. Adds a tooltip line naming the catalyst
 * you combine with it in the smithing table.
 */
public class UpgradeCoreItem extends Item {
	private final int level;

	public UpgradeCoreItem(Properties properties, int level) {
		super(properties);
		this.level = level;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
			Consumer<Component> adder, TooltipFlag flag) {
		Item catalyst = EVE.UPGRADE_CATALYSTS.get(this.level - 1);
		adder.accept(Component.translatable("item.eve.upgrade_core.catalyst", catalyst.getName(new ItemStack(catalyst)))
				.withStyle(ChatFormatting.GRAY));
	}
}
