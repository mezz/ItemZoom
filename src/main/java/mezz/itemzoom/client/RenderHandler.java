package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.Window;
import mezz.itemzoom.client.compat.JeiCompat;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Matrix3x2fStack;

import javax.annotation.Nullable;
import java.util.function.Supplier;


public class RenderHandler {
	@Nullable
	public static Rect2i rendering = null;
	@Nullable
	private static Rect2i renderedThisFrame = null;
	private final Config config;
	private final Supplier<Boolean> isEnableKeyHeld;

	public RenderHandler(Config config, Supplier<Boolean> isEnableKeyHeld) {
		this.config = config;
		this.isEnableKeyHeld = isEnableKeyHeld;
	}

	public void onScreenDrawn() {
		rendering = renderedThisFrame;
		renderedThisFrame = null;
	}

	public void onItemStackTooltip(GuiGraphicsExtractor guiGraphics, @Nullable ItemStack itemStack, int x, int y) {
		if (!config.isToggledEnabled() && !isEnableKeyHeld.get()) {
			return;
		}
		if (itemStack == null || itemStack.isEmpty()) {
			return;
		}
		if (config.isJeiOnly() && !ItemStack.isSameItem(itemStack, JeiCompat.getStackUnderMouse())) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Screen currentScreen = minecraft.screen;
		if (currentScreen instanceof AbstractContainerScreen<?> containerScreen) {
			Rect2i renderArea = getRenderingArea(containerScreen, x);
			// avoid rendering zoomed items in the same space as the item being hovered over
			if (!renderArea.contains(x, y)) {
				if (renderZoomedStack(guiGraphics, itemStack, renderArea, minecraft)) {
					renderedThisFrame = renderArea;
				}
			}
		}
	}

	public Rect2i getRenderingArea(AbstractContainerScreen<?> containerScreen, int mouseX) {
		Minecraft minecraft = containerScreen.getMinecraft();
		Window window = minecraft.getWindow();
		int guiRight = containerScreen.getLeftPos() + containerScreen.getImageHeight();
		int spaceOnLeft = getSpaceOnLeft(containerScreen);
		int spaceOnRight = window.getGuiScaledWidth() - guiRight;

		final boolean renderLeft;
		if (mouseX < containerScreen.getLeftPos()) {
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

		int y = containerScreen.getTopPos();
		int height = containerScreen.getImageHeight();
		if (renderLeft) {
			return new Rect2i(0, y, spaceOnLeft, height);
		} else {
			return new Rect2i(guiRight, y, spaceOnRight, height);
		}
	}

	private int getSpaceOnLeft(AbstractContainerScreen<?> containerScreen) {
		if (containerScreen instanceof AbstractRecipeBookScreen<?> recipeListener) {
			RecipeBookComponent<?> guiRecipeBook = recipeListener.recipeBookComponent;
			if (guiRecipeBook.isVisible()) {
				return guiRecipeBook.tabButtons.stream()
						.findAny()
						.map(AbstractWidget::getX)
						.orElse((guiRecipeBook.width - 147) / 2 - guiRecipeBook.xOffset);
			}
		}
		return containerScreen.getLeftPos();
	}

	private boolean renderZoomedStack(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, Rect2i availableArea, Minecraft minecraft) {
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

		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(xPosition, yPosition);
			poseStack.scale(scale, scale);

			guiGraphics.item(itemStack, 0, 0);

			renderItemOverlayIntoGUI(guiGraphics, itemStack);
		}
		poseStack.pushMatrix();

		if (config.showHelpText()) {
			int y = availableAreaY + ((availableAreaHeight + Math.round(19 * scale)) / 2);

			String modName = Constants.MOD_NAME;
			Font nameFont = getFont(minecraft, itemStack, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);

			int stringWidth = nameFont.width(modName);
			if (stringWidth < availableAreaWidth) {
				int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
				guiGraphics.text(nameFont, modName, x, y, ARGB.opaque(4210752), false);

				y += nameFont.lineHeight;
			}

			if (config.isToggledEnabled()) {
				KeyBindings keyBindings = KeyBindings.getInstance();
				Component displayName = keyBindings.toggle.getTranslatedKeyMessage();
				String toggleText = displayName.getString();
				Font minecraftFont = minecraft.font;
				stringWidth = minecraftFont.width(toggleText);
				if (stringWidth < availableAreaWidth) {
					int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
					guiGraphics.text(minecraftFont, toggleText, x, y, ARGB.opaque(4210752), false);
				}
			}
		}
		return true;
	}

	private static Font getFont(Minecraft minecraft, ItemStack itemStack, IClientItemExtensions.FontContext context) {
		IClientItemExtensions renderProperties = IClientItemExtensions.of(itemStack);
		Font fontRenderer = renderProperties.getFont(itemStack, context);
		if (fontRenderer == null) {
			fontRenderer = minecraft.font;
		}
		return fontRenderer;
	}

	public void renderItemOverlayIntoGUI(GuiGraphicsExtractor guiGraphics, ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();

		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			if (config.showStackSize() && itemStack.getCount() != 1) {
				String countString = String.valueOf(itemStack.getCount());
				Font itemCountFont = getFont(minecraft, itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);

				RenderBuffers renderBuffers = minecraft.renderBuffers();
				MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
				guiGraphics.text(itemCountFont, countString, (int) (17.0F - itemCountFont.width(countString)), (int) 9.0F, ARGB.opaque(0xFFFFFF), true);
				bufferSource.endBatch();
			}

			if (config.showDurabilityBar() && itemStack.isBarVisible()) {
				int k = itemStack.getBarWidth();
				int l = itemStack.getBarColor();
				guiGraphics.fill(2, 13, 15, 15, -0xFFFFFF);
				guiGraphics.fill(2, 13, 2 + k, 14, l | -0xFFFFFF);
			}

			if (config.showCooldown()) {
				LocalPlayer localplayer = minecraft.player;
				if (localplayer != null) {
					ItemCooldowns cooldowns = localplayer.getCooldowns();
					float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
					float cooldownPercent = cooldowns.getCooldownPercent(itemStack, partialTicks);
					if (cooldownPercent > 0.0F) {
						int i1 = Mth.floor(16.0F * (1.0F - cooldownPercent));
						int j1 = i1 + Mth.ceil(16.0F * cooldownPercent);
						guiGraphics.fill(0, i1, 16, j1, Integer.MAX_VALUE);
					}
				}
			}
		}
		poseStack.popMatrix();
	}

}
