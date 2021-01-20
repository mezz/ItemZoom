package mezz.itemzoom.client;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mezz.itemzoom.ItemZoom;
import mezz.itemzoom.client.compat.JeiCompat;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderHandler {
	public static boolean rendering = false;
	private static boolean renderedThisFrame = false;
	private final Config config;
	private final Supplier<Boolean> isEnableKeyHeld;
	private final KeyBinding keyBinding;

	public RenderHandler(Config config, Supplier<Boolean> isEnableKeyHeld, KeyBinding keyBinding) {
		this.config = config;
		this.isEnableKeyHeld = isEnableKeyHeld;
		this.keyBinding = keyBinding;
	}

	public void onScreenDrawn() {
		rendering = renderedThisFrame;
		renderedThisFrame = false;
	}

	public void onItemStackTooltip(@Nullable ItemStack itemStack, int x, MatrixStack matrixStack) {
		if (!config.isToggledEnabled() && !isEnableKeyHeld.get()) {
			return;
		}
		if (itemStack == null || itemStack.isEmpty()) {
			return;
		}
		if (config.isJeiOnly() && !ItemStack.areItemStacksEqual(itemStack, JeiCompat.getStackUnderMouse())) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Screen currentScreen = minecraft.currentScreen;
		if (currentScreen instanceof ContainerScreen) {
			ContainerScreen<?> containerScreen = (ContainerScreen<?>) currentScreen;
			if (x > containerScreen.getGuiLeft()) { // avoid rendering items in the same space as the item
				renderZoomedStack(itemStack, containerScreen, minecraft, matrixStack);
				renderedThisFrame = true;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void renderZoomedStack(ItemStack itemStack, ContainerScreen<?> containerScreen, Minecraft minecraft, MatrixStack matrixStack) {
		final int scaledHeight = minecraft.getMainWindow().getScaledHeight();
		final float scale = config.getZoomAmount() / 100f * containerScreen.getGuiLeft() / 17f; // item is 16 wide, give it some extra space on each side
		final float xPosition = (containerScreen.getGuiLeft() / scale - 16f) / 2f;
		final float yPosition = (scaledHeight / scale - 16f) / 2f;
		FontRenderer font = getFontRenderer(minecraft, itemStack);

		GlStateManager.pushMatrix();
		GlStateManager.scalef(scale, scale, 1);
		GlStateManager.translatef(xPosition, yPosition, 0);
		ZoomRenderHelper.enableGUIStandardItemLighting(scale);

		minecraft.getItemRenderer().zLevel += 100;
		minecraft.getItemRenderer().renderItemAndEffectIntoGUI(minecraft.player, itemStack, 0, 0);
		renderItemOverlayIntoGUI(font, itemStack, matrixStack);
		minecraft.getItemRenderer().zLevel -= 100;
		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();

		if (config.showHelpText()) {
			String modName = ItemZoom.MOD_NAME;
			int stringWidth = font.getStringWidth(modName);
			int x = (containerScreen.getGuiLeft() - stringWidth) / 2;
			int y = (scaledHeight + Math.round(17 * scale)) / 2;
			font.func_238421_b_(matrixStack, modName, x, y, 4210752);

			if (config.isToggledEnabled()) {
				String toggleText = keyBinding.func_238171_j_().getString();
				stringWidth = font.getStringWidth(toggleText);
				x = (containerScreen.getGuiLeft() - stringWidth) / 2;
				y += font.FONT_HEIGHT;
				font.func_238421_b_(matrixStack, toggleText, x, y, 4210752);
			}
		}
	}

	private static FontRenderer getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRenderer;
		}
		return fontRenderer;
	}

	@SuppressWarnings("deprecation")
	public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, MatrixStack matrixStack) {
		if (!stack.isEmpty()) {
			if (config.showStackSize() && stack.getCount() != 1) {
				matrixStack.push();
				matrixStack.translate(0, 0, 500);
				String s = String.valueOf(stack.getCount());
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableBlend();
				fr.func_238405_a_(matrixStack, s, (float) (17 - fr.getStringWidth(s)), 9f, 16777215);
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
				// Fixes opaque cooldown overlay a bit lower
				// TODO: check if enabled blending still screws things up down the line.
				GlStateManager.enableBlend();
				matrixStack.pop();
			}

			if (config.showDamageBar() && stack.getItem().showDurabilityBar(stack)) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableTexture();
				GlStateManager.disableAlphaTest();
				GlStateManager.disableBlend();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferBuilder = tessellator.getBuffer();
				double health = stack.getItem().getDurabilityForDisplay(stack);
				int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
				int i = Math.round(13.0F - (float) health * 13.0F);
				draw(bufferBuilder, 2, 13, 13, 2, 0, 0, 0, 255);
				draw(bufferBuilder, 2, 13, i, 1, rgbfordisplay >> 16 & 255, rgbfordisplay >> 8 & 255, rgbfordisplay & 255, 255);
				GlStateManager.enableBlend();
				GlStateManager.enableAlphaTest();
				GlStateManager.enableTexture();
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}

			ClientPlayerEntity entityplayersp = Minecraft.getInstance().player;
			float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getInstance().getRenderPartialTicks());

			if (f3 > 0.0F) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableTexture();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferBuilder = tessellator.getBuffer();
				draw(bufferBuilder, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
				GlStateManager.enableTexture();
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}
		}
	}

	private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		renderer.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos(x + width, y, 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();
	}

}
