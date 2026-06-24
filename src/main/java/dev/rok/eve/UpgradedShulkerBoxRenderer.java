package dev.rok.eve;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;

/**
 * Renders the placed upgraded shulker box with our emblem textures instead of
 * the vanilla shulker sprites. Reuses the vanilla renderer's model and transform
 * and just swaps the sprite, so the lid animation and orientation are unchanged.
 */
public class UpgradedShulkerBoxRenderer extends ShulkerBoxRenderer {
	public UpgradedShulkerBoxRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void submit(ShulkerBoxRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		DyeColor color = state.color;
		SpriteId sprite = Sheets.SHULKER_MAPPER.apply(
				EVE.id("upgraded_shulker" + (color == null ? "" : "_" + color.getName())));
		poseStack.pushPose();
		poseStack.mulPose(ShulkerBoxRenderer.modelTransform(state.direction));
		this.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY,
				state.progress, state.breakProgress, sprite, 0);
		poseStack.popPose();
	}
}
