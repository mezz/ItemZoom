package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.itemzoom.client.compat.JeiCompat;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

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

	public void onItemStackTooltip(GuiGraphics guiGraphics, @Nullable ItemStack itemStack, int x, int y) {
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
						.map(AbstractWidget::getX)
						.orElse((guiRecipeBook.width - 147) / 2 - guiRecipeBook.xOffset);
			}
		}
		return containerScreen.getGuiLeft();
	}

	private boolean renderZoomedStack(GuiGraphics guiGraphics, ItemStack itemStack, Rect2i availableArea, Minecraft minecraft) {
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

		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			poseStack.translate(xPosition, yPosition, 232.0F);
			poseStack.scale(scale, scale, scale);

			guiGraphics.renderItem(itemStack, 0, 0);
			RenderSystem.setShader(GameRenderer::getPositionColorShader);

			renderItemOverlayIntoGUI(guiGraphics, itemStack);
		}
		poseStack.popPose();

		RenderSystem.applyModelViewMatrix();

		if (config.showHelpText()) {
			int y = availableAreaY + ((availableAreaHeight + Math.round(19 * scale)) / 2);

			String modName = Constants.MOD_NAME;
			Font nameFont = getFont(minecraft, itemStack, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);

			int stringWidth = nameFont.width(modName);
			if (stringWidth < availableAreaWidth) {
				int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
				guiGraphics.drawString(nameFont, modName, x, y, 4210752, false);

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
					guiGraphics.drawString(minecraftFont, toggleText, x, y, 4210752, false);
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

	public void renderItemOverlayIntoGUI(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();

		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		{
			if (config.showStackSize() && itemStack.getCount() != 1) {
				String countString = String.valueOf(itemStack.getCount());
				Font itemCountFont = getFont(minecraft, itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);

				poseStack.translate(0.0F, 0.0F, 200.0F);
				RenderBuffers renderBuffers = minecraft.renderBuffers();
				MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
				itemCountFont.drawInBatch(
						countString,
						17.0F - itemCountFont.width(countString),
						9.0F,
						0xFFFFFF,
						true,
						poseStack.last().pose(),
						bufferSource,
						Font.DisplayMode.NORMAL,
						0,
						0xF000F0
				);
				bufferSource.endBatch();
			}

			if (config.showDurabilityBar() && itemStack.isBarVisible()) {
				RenderSystem.disableDepthTest();
				int k = itemStack.getBarWidth();
				int l = itemStack.getBarColor();
				guiGraphics.fill(2, 13, 15, 15, -0xFFFFFF);
				guiGraphics.fill(2, 13, 2 + k, 14, l | -0xFFFFFF);
				RenderSystem.enableDepthTest();
			}

			if (config.showCooldown()) {
				LocalPlayer localplayer = minecraft.player;
				if (localplayer != null) {
					ItemCooldowns cooldowns = localplayer.getCooldowns();
					float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
					float cooldownPercent = cooldowns.getCooldownPercent(itemStack.getItem(), partialTicks);
					if (cooldownPercent > 0.0F) {
						RenderSystem.disableDepthTest();
						int i1 = Mth.floor(16.0F * (1.0F - cooldownPercent));
						int j1 = i1 + Mth.ceil(16.0F * cooldownPercent);
						guiGraphics.fill(0, i1, 16, j1, Integer.MAX_VALUE);
						RenderSystem.enableDepthTest();
					}
				}
			}
		}
		poseStack.popPose();
	}

}
