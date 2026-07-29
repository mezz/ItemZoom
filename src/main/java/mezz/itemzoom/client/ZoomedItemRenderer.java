package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jspecify.annotations.Nullable;

public class ZoomedItemRenderer extends PictureInPictureRenderer<ZoomedItemRenderState> {
	private @Nullable Object modelOnTextureIdentity = null;

	public ZoomedItemRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}

	@Override
	public Class<ZoomedItemRenderState> getRenderStateClass() {
		return ZoomedItemRenderState.class;
	}

	@Override
	protected void renderToTexture(ZoomedItemRenderState renderState, PoseStack poseStack) {
		poseStack.scale(1.0F, -1.0F, -1.0F);

		TrackingItemStackRenderState itemStackRenderState = renderState.itemStackRenderState();
		Lighting.Entry lighting = itemStackRenderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.gameRenderer.getLighting().setupFor(lighting);

		FeatureRenderDispatcher featureRenderDispatcher = minecraft.gameRenderer.getFeatureRenderDispatcher();
		SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();
		itemStackRenderState.submit(poseStack, submitNodeStorage, 15728880, OverlayTexture.NO_OVERLAY, 0);
		featureRenderDispatcher.renderAllFeatures();
		this.modelOnTextureIdentity = itemStackRenderState.getModelIdentity();
	}

	@Override
	protected boolean textureIsReadyToBlit(ZoomedItemRenderState renderState) {
		TrackingItemStackRenderState itemStackRenderState = renderState.itemStackRenderState();
		return !itemStackRenderState.isAnimated() && itemStackRenderState.getModelIdentity().equals(this.modelOnTextureIdentity);
	}

	@Override
	protected float getTranslateY(int height, int guiScale) {
		return height / 2.0F;
	}

	@Override
	protected String getTextureLabel() {
		return "itemzoom_item";
	}
}
