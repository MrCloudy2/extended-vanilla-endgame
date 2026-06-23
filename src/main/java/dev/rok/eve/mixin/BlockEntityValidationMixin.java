package dev.rok.eve.mixin;

import dev.rok.eve.EVE;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The Upgraded Shulker Box's block entity extends ShulkerBoxBlockEntity, whose
 * super constructor hardcodes the SHULKER_BOX type — so the block-state
 * validation that runs inside the BlockEntity constructor sees that type
 * against our block and throws before we can correct it. Our block is genuinely
 * valid for our own type (set right after construction), so accept it here.
 */
@Mixin(BlockEntity.class)
public class BlockEntityValidationMixin {
	@Inject(method = "isValidBlockState", at = @At("HEAD"), cancellable = true)
	private void eve$allowUpgradedShulkerBox(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (state.is(EVE.UPGRADED_SHULKER_BOX)) {
			cir.setReturnValue(true);
		}
	}
}
