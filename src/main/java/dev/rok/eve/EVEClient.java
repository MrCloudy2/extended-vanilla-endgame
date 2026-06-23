package dev.rok.eve;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;

/**
 * Client setup: render the Upgraded Shulker Box with the vanilla shulker box
 * renderer, so it gets the real animated lid and shulker texture.
 */
public class EVEClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(EVE.UPGRADED_SHULKER_BOX_BLOCK_ENTITY, ShulkerBoxRenderer::new);
	}
}
