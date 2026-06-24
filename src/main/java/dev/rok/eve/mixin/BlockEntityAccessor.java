package dev.rok.eve.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Lets a subclass of a vanilla block entity (which hardcodes its type in the
 * super constructor) correct its type after construction. Used so the Upgraded
 * Shulker Box can extend {@code ShulkerBoxBlockEntity} — reusing all of its
 * behaviour and renderer — while registering under its own block entity type.
 */
@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {
	@Mutable
	@Accessor("type")
	void eve$setType(BlockEntityType<?> type);
}
