package mezz.itemzoom.client;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import mezz.itemzoom.client.compat.JeiCompat;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.RenderProperties;

@OnlyIn(Dist.CLIENT)
public class RenderHandler {
	@Nullable
	public static Rect2i rendering = null;
	@Nullable
	private static Rect2i renderedThisFrame = null;
	private final Config config;
	private final Supplier<Boolean> isEnableKeyHeld;
	private final KeyMapping keyMapping;

	public RenderHandler(Config config, Supplier<Boolean> isEnableKeyHeld, KeyMapping keyMapping) {
		this.config = config;
		this.isEnableKeyHeld = isEnableKeyHeld;
		this.keyMapping = keyMapping;
	}

	public void onScreenDrawn() {
		rendering = renderedThisFrame;
		renderedThisFrame = null;
	}

	public void onItemStackTooltip(@Nullable ItemStack itemStack, int x, int y) {
		if (!config.isToggledEnabled() && !isEnableKeyHeld.get()) {
			return;
		}
		if (itemStack == null || itemStack.isEmpty()) {
			return;
		}
		if (config.isJeiOnly() && !ItemStack.isSame(itemStack, JeiCompat.getStackUnderMouse())) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Screen currentScreen = minecraft.screen;
		if (currentScreen instanceof AbstractContainerScreen<?> containerScreen) {
			Rect2i renderArea = getRenderingArea(containerScreen, x);
			// avoid rendering zoomed items in the same space as the item being hovered over
			if (!renderArea.contains(x, y)) {
				if (renderZoomedStack(itemStack, renderArea, minecraft)) {
					renderedThisFrame = renderArea;
				}
			}
		}
	}

	public Rect2i getRenderingArea(AbstractContainerScreen<?> containerScreen, int mouseX) {
		Minecraft minecraft = containerScreen.getMinecraft();
		Window window = minecraft.getWindow();
		int guiRight = containerScreen.getGuiLeft() + containerScreen.getXSize();
		int spaceOnLeft = getSpaceOnLeft(containerScreen);
		int spaceOnRight = window.getGuiScaledWidth() - guiRight;

		final boolean renderLeft;
		if (mouseX < containerScreen.getGuiLeft()) {
			// mouse is to the left side of the gui, render on the right.
			renderLeft = false;
		} else if (mouseX > guiRight) {
			// mouse is to the right side of the gui, render on the left.
			renderLeft = true;
		} else {
			// mouse is over the gui somewhere, pick whichever size has more space,
			// but bias a bit toward picking the left
			renderLeft = (spaceOnLeft * 1.1) >= spaceOnRight;
		}

		int y = containerScreen.getGuiTop();
		int height = containerScreen.getYSize();
		if (renderLeft) {
			return new Rect2i(0, y, spaceOnLeft, height);
		} else {
			return new Rect2i(guiRight, y, spaceOnRight, height);
		}
	}

	private int getSpaceOnLeft(AbstractContainerScreen<?> containerScreen) {
		if (containerScreen instanceof RecipeUpdateListener recipeListener) {
			RecipeBookComponent guiRecipeBook = recipeListener.getRecipeBookComponent();
			if (guiRecipeBook.isVisible()) {
				return guiRecipeBook.tabButtons.stream()
						.findAny()
						.map(tab -> tab.x)
						.orElse((guiRecipeBook.width - 147) / 2 - guiRecipeBook.xOffset);
			}
		}
		return containerScreen.getGuiLeft();
	}

	private boolean renderZoomedStack(ItemStack itemStack, Rect2i availableArea, Minecraft minecraft) {
		final int availableAreaX = availableArea.getX();
		final int availableAreaY = availableArea.getY();
		final int availableAreaWidth = availableArea.getWidth();
		final int availableAreaHeight = availableArea.getHeight();

		// item is 16 wide, give it some extra space on each side by using 17 here
		final float scale = config.getZoomAmount() / 100f * availableAreaWidth / 17f;
		if (scale <= 2.0f) {
			// not enough room to be useful
			return false;
		}

		final float renderWidth = scale * 16;
		final float renderHeight = scale * 16;
		final float xPosition = availableAreaX + ((availableAreaWidth - renderWidth) / 2f);
		final float yPosition = availableAreaY + ((availableAreaHeight - renderHeight) / 2f);
		Font font = getFont(minecraft, itemStack);

		PoseStack modelPoseStack = RenderSystem.getModelViewStack();
		modelPoseStack.pushPose();
		{
			modelPoseStack.translate(xPosition, yPosition, 0);
			modelPoseStack.scale(scale, scale, 1);

			minecraft.getItemRenderer().renderAndDecorateItem(itemStack, 0, 0);
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
		}
		modelPoseStack.popPose();

		renderItemOverlayIntoGUI(font, itemStack);

		RenderSystem.applyModelViewMatrix();

		if (config.showHelpText()) {
			int y = availableAreaY + ((availableAreaHeight + Math.round(19 * scale)) / 2);
			float z = minecraft.getItemRenderer().blitOffset + 200.0F;
			PoseStack poseStack = new PoseStack();
			poseStack.translate(0, 0, z);

			String modName = Constants.MOD_NAME;
			int stringWidth = font.width(modName);
			if (stringWidth < availableAreaWidth) {
				int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
				font.draw(poseStack, modName, x, y, 4210752);

				y += font.lineHeight;
			}

			if (config.isToggledEnabled()) {
				Component displayName = keyMapping.getTranslatedKeyMessage();
				String toggleText = displayName.getString();
				stringWidth = font.width(toggleText);
				if (stringWidth < availableAreaWidth) {
					int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
					font.draw(poseStack, toggleText, x, y, 4210752);
				}
			}
		}
		return true;
	}

	private static Font getFont(Minecraft minecraft, ItemStack itemStack) {
		IItemRenderProperties renderProperties = RenderProperties.get(itemStack);
		Font fontRenderer = renderProperties.getFont(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.font;
		}
		return fontRenderer;
	}

	public void renderItemOverlayIntoGUI(Font font, ItemStack stack) {
		if (!stack.isEmpty()) {
			Minecraft minecraft = Minecraft.getInstance();
			Tesselator tesselator = Tesselator.getInstance();

			if (config.showStackSize() && stack.getCount() != 1) {
				String s = String.valueOf(stack.getCount());
				float x = (19 - 2 - font.width(s));
				float y = (6 + 3);
				float z = minecraft.getItemRenderer().blitOffset + 200.0F;

				PoseStack poseStack = new PoseStack();
				poseStack.translate(0.0D, 0.0D, z);
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(bufferBuilder);
				font.drawInBatch(s, x, y, 16777215, true, poseStack.last().pose(), bufferSource, false, 0, 15728880);
				bufferSource.endBatch();
			}

			if (config.showDurabilityBar() && stack.getItem().showDurabilityBar(stack)) {
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.disableBlend();
				BufferBuilder bufferbuilder = tesselator.getBuilder();
				double durability = stack.getItem().getDurabilityForDisplay(stack);
				int i = Math.round(13.0F - (float)durability * 13.0F);
				int rgb = stack.getItem().getRGBDurabilityForDisplay(stack);
				fillRect(bufferbuilder, 2, 13, 13, 2, 0, 0, 0, 255);
				fillRect(bufferbuilder, 2, 13, i, 1, rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 255);
				RenderSystem.enableBlend();
				RenderSystem.enableTexture();
				RenderSystem.enableDepthTest();
			}

			if (config.showCooldown()) {
				LocalPlayer localplayer = minecraft.player;
				float f;
				if (localplayer == null) {
					f = 0.0F;
				} else {
					ItemCooldowns cooldowns = localplayer.getCooldowns();
					f = cooldowns.getCooldownPercent(stack.getItem(), minecraft.getFrameTime());
				}
				if (f > 0.0F) {
					RenderSystem.disableDepthTest();
					RenderSystem.disableTexture();
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					BufferBuilder bufferbuilder = tesselator.getBuilder();
					fillRect(bufferbuilder, 0, Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
					RenderSystem.enableTexture();
					RenderSystem.enableDepthTest();
				}
			}
		}
	}

	/**
	 * Modeled after {@link ItemRenderer#fillRect(BufferBuilder, int, int, int, int, int, int, int, int)}
	 */
	@SuppressWarnings("JavadocReference")
	private static void fillRect(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		renderer.vertex(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.vertex(x, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.vertex(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.vertex(x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.end();
		BufferUploader.end(renderer);
	}
}
