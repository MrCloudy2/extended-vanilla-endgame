package dev.rok.eve;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

/**
 * Client setup: render the placed Upgraded Shulker Box with our own renderer,
 * which reuses the vanilla shulker model/animation but swaps in the emblem
 * textures so placed boxes look distinct too.
 */
public class EVEClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(EVE.UPGRADED_SHULKER_BOX_BLOCK_ENTITY, UpgradedShulkerBoxRenderer::new);
	}
}
