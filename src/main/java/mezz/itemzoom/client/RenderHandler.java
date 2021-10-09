package mezz.itemzoom.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import mezz.itemzoom.ItemZoom;
import mezz.itemzoom.client.compat.JeiCompat;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class RenderHandler {
	@Nullable
	public static Rectangle2d rendering = null;
	@Nullable
	private static Rectangle2d renderedThisFrame = null;
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
		renderedThisFrame = null;
	}

	public void onItemStackTooltip(@Nullable ItemStack itemStack, int x, int y, MatrixStack matrixStack) {
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
			Rectangle2d renderArea = getRenderingArea(containerScreen, x);
			// avoid rendering zoomed items in the same space as the item being hovered over
			if (!renderArea.contains(x, y)) {
				if (renderZoomedStack(itemStack, renderArea, minecraft, matrixStack)) {
					renderedThisFrame = renderArea;
				}
			}
		}
	}

	public static Rectangle2d getRenderingArea(ContainerScreen<?> containerScreen, int mouseX) {
		Minecraft minecraft = containerScreen.getMinecraft();
		MainWindow window = minecraft.getMainWindow();
		int guiRight = containerScreen.getGuiLeft() + containerScreen.getXSize();
		int spaceOnLeft = getSpaceOnLeft(containerScreen);
		int spaceOnRight = window.getScaledWidth() - guiRight;

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
			return new Rectangle2d(0, y, spaceOnLeft, height);
		} else {
			return new Rectangle2d(guiRight, y, spaceOnRight, height);
		}
	}

	private static int getSpaceOnLeft(ContainerScreen<?> containerScreen) {
		if (containerScreen instanceof IRecipeShownListener) {
			RecipeBookGui guiRecipeBook = ((IRecipeShownListener) containerScreen).getRecipeGui();
			if (guiRecipeBook.isVisible()) {
				return guiRecipeBook.recipeTabs.stream()
						.findAny()
						.map(tab -> tab.field_230690_l_)
						.orElse((guiRecipeBook.width - 147) / 2 - guiRecipeBook.xOffset);
			}
		}
		return containerScreen.getGuiLeft();
	}

	@SuppressWarnings("deprecation")
	private boolean renderZoomedStack(ItemStack itemStack, Rectangle2d availableArea, Minecraft minecraft, MatrixStack matrixStack) {
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
		FontRenderer font = getFontRenderer(minecraft, itemStack);

		GlStateManager.pushMatrix();
		GlStateManager.translatef(xPosition, yPosition, 0);
		GlStateManager.scalef(scale, scale, 1);
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
			int y = availableAreaY + ((availableAreaHeight + Math.round(19 * scale)) / 2);
			if (stringWidth < availableAreaWidth) {
				int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
				font.func_238421_b_(matrixStack, modName, x, y, 4210752);

				y += font.FONT_HEIGHT;
			}

			if (config.isToggledEnabled()) {
				String toggleText = keyBinding.func_238171_j_().getString();
				stringWidth = font.getStringWidth(toggleText);
				if (stringWidth < availableAreaWidth) {
					int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
					font.func_238421_b_(matrixStack, toggleText, x, y, 4210752);
				}
			}
		}
		return true;
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
