package dev.rok.eve;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Holds the sponge's upgrade tier so the level survives placing and breaking.
 * The tier round-trips through the eve:upgrade_level item component via the
 * implicit-components hooks plus copy_components in the block's loot table.
 */
public class AbsorbingSpongeBlockEntity extends BlockEntity {
	private int tier = 1;

	public AbsorbingSpongeBlockEntity(BlockPos pos, BlockState state) {
		super(EVE.ABSORBING_SPONGE_BLOCK_ENTITY, pos, state);
	}

	public int getTier() {
		return this.tier;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("tier", this.tier);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.tier = input.getIntOr("tier", 1);
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		this.tier = components.getOrDefault(EVE.UPGRADE_LEVEL, 1);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(EVE.UPGRADE_LEVEL, this.tier);
		// Rebuild the lore too so the dropped item shows its level again.
		components.set(DataComponents.LORE, UpgradeHelper.spongeLore(this.tier));
	}
}
