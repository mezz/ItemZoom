package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.Window;
import mezz.itemzoom.client.compat.JeiCompat;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class RenderHandler {
	private static final int ITEM_SIZE = 16;
	private static final int MIN_RENDER_SIZE = ITEM_SIZE * 2;
	private static final int PADDING = 4;
	private static final int HELP_TEXT_GAP = 4;
	private static final int HELP_TEXT_COLOR = ARGB.opaque(0x404040);
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

	public static void registerPictureInPictureRenderers(RegisterPictureInPictureRenderersEvent event) {
		event.register(ZoomedItemRenderState.class, ZoomedItemRenderer::new);
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
		ItemStack jeiStackUnderMouse = JeiCompat.getStackUnderMouse();
		boolean isJeiHoveredItem = ItemStack.isSameItem(itemStack, jeiStackUnderMouse);
		if (config.isJeiOnly() && !isJeiHoveredItem) {
			return;
		}
		ItemStack recipesGuiStackUnderMouse = JeiCompat.getRecipesGuiStackUnderMouse();
		boolean isRecipesGuiHoveredItem = ItemStack.isSameItem(itemStack, recipesGuiStackUnderMouse);

		Minecraft minecraft = Minecraft.getInstance();
		Screen currentScreen = minecraft.screen;
		if (currentScreen != null) {
			Rect2i renderArea = getRenderingArea(minecraft, currentScreen, isJeiHoveredItem, isRecipesGuiHoveredItem, x);
			if (renderArea == null) {
				return;
			}
			// avoid rendering zoomed items in the same space as the item being hovered over
			if (!renderArea.contains(x, y)) {
				if (renderZoomedStack(guiGraphics, itemStack, renderArea, minecraft)) {
					renderedThisFrame = renderArea;
				}
			}
		}
	}

	private @Nullable Rect2i getRenderingArea(
			Minecraft minecraft,
			Screen currentScreen,
			boolean isJeiHoveredItem,
			boolean isRecipesGuiHoveredItem,
			int mouseX
	) {
		if (currentScreen instanceof AbstractContainerScreen<?> containerScreen) {
			return getRenderingArea(containerScreen, mouseX);
		}

		if (!isJeiHoveredItem) {
			return null;
		}

		JeiCompat.GuiArea guiArea = JeiCompat.getGuiArea(currentScreen);
		if (guiArea != null) {
			if (isRecipesGuiHoveredItem) {
				return getLeftRenderingArea(guiArea);
			}
			return getRenderingArea(guiArea, mouseX);
		}

		Screen parentScreen = JeiCompat.getRecipesGuiParentScreen();
		if (parentScreen instanceof AbstractContainerScreen<?> containerScreen) {
			return getRenderingArea(containerScreen, mouseX);
		}

		return getRenderingArea(minecraft.getWindow(), mouseX);
	}

	public Rect2i getRenderingArea(AbstractContainerScreen<?> containerScreen, int mouseX) {
		Minecraft minecraft = containerScreen.getMinecraft();
		Window window = minecraft.getWindow();
		int guiRight = containerScreen.getLeftPos() + containerScreen.getImageWidth();
		int spaceOnLeft = getSpaceOnLeft(containerScreen);
		int spaceOnRight = window.getGuiScaledWidth() - guiRight;
		boolean renderLeft = shouldRenderLeft(mouseX, containerScreen.getLeftPos(), guiRight, spaceOnLeft, spaceOnRight);

		int y = containerScreen.getTopPos();
		int height = containerScreen.getImageHeight();
		if (renderLeft) {
			return new Rect2i(0, y, spaceOnLeft, height);
		} else {
			return new Rect2i(guiRight, y, spaceOnRight, height);
		}
	}

	private static Rect2i getLeftRenderingArea(JeiCompat.GuiArea guiArea) {
		return new Rect2i(0, guiArea.guiTop(), Math.max(0, guiArea.guiLeft()), guiArea.guiYSize());
	}

	private static Rect2i getRenderingArea(JeiCompat.GuiArea guiArea, int mouseX) {
		int guiLeft = guiArea.guiLeft();
		int guiRight = guiLeft + guiArea.guiXSize();
		int spaceOnLeft = Math.max(0, guiLeft);
		int spaceOnRight = Math.max(0, guiArea.screenWidth() - guiRight);
		boolean renderLeft = shouldRenderLeft(mouseX, guiLeft, guiRight, spaceOnLeft, spaceOnRight);

		if (renderLeft) {
			return new Rect2i(0, guiArea.guiTop(), spaceOnLeft, guiArea.guiYSize());
		} else {
			return new Rect2i(guiRight, guiArea.guiTop(), spaceOnRight, guiArea.guiYSize());
		}
	}

	private static Rect2i getRenderingArea(Window window, int mouseX) {
		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();
		int spaceOnLeft = Math.max(0, mouseX - PADDING);
		int spaceOnRight = Math.max(0, screenWidth - mouseX - PADDING);
		boolean renderLeft = (spaceOnLeft * 1.1) >= spaceOnRight;

		if (renderLeft) {
			return new Rect2i(0, 0, spaceOnLeft, screenHeight);
		} else {
			return new Rect2i(mouseX + PADDING, 0, spaceOnRight, screenHeight);
		}
	}

	private static boolean shouldRenderLeft(int mouseX, int guiLeft, int guiRight, int spaceOnLeft, int spaceOnRight) {
		if (mouseX < guiLeft) {
			// mouse is to the left side of the gui, render on the right.
			return false;
		} else if (mouseX > guiRight) {
			// mouse is to the right side of the gui, render on the left.
			return true;
		} else {
			// mouse is over the gui somewhere, pick whichever size has more space,
			// but bias a bit toward picking the left
			return (spaceOnLeft * 1.1) >= spaceOnRight;
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

		List<TextLine> helpTextLines = getHelpTextLines(itemStack, minecraft, availableAreaWidth - PADDING * 2);
		int helpTextHeight = getHelpTextHeight(helpTextLines);

		int maximumRenderSize = Math.min(
				availableAreaWidth - PADDING * 2,
				availableAreaHeight - helpTextHeight - PADDING * 2
		);
		int renderSize = Math.round(maximumRenderSize * config.getZoomAmount() / 100f);
		if (renderSize <= MIN_RENDER_SIZE) {
			// not enough room to be useful
			return false;
		}

		final int totalHeight = renderSize + helpTextHeight;
		final int xPosition = availableAreaX + ((availableAreaWidth - renderSize) / 2);
		final int yPosition = availableAreaY + ((availableAreaHeight - totalHeight) / 2);

		renderZoomedItem(guiGraphics, itemStack, minecraft, xPosition, yPosition, renderSize);

		float scale = renderSize / (float) ITEM_SIZE;
		Matrix3x2fStack poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			poseStack.translate(xPosition, yPosition);
			poseStack.scale(scale, scale);

			renderItemOverlayIntoGUI(guiGraphics, itemStack);
		}
		poseStack.popMatrix();

		if (!helpTextLines.isEmpty()) {
			int y = yPosition + renderSize + HELP_TEXT_GAP;
			for (TextLine line : helpTextLines) {
				int stringWidth = line.font().width(line.text());
				int x = availableAreaX + ((availableAreaWidth - stringWidth) / 2);
				guiGraphics.text(line.font(), line.text(), x, y, HELP_TEXT_COLOR, true);

				y += line.font().lineHeight;
			}
		}
		return true;
	}

	private List<TextLine> getHelpTextLines(ItemStack itemStack, Minecraft minecraft, int maximumWidth) {
		List<TextLine> lines = new ArrayList<>();
		if (!config.showHelpText()) {
			return lines;
		}

		Font nameFont = getFont(minecraft, itemStack, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
		addTextLine(lines, nameFont, Constants.MOD_NAME, maximumWidth);

		if (config.isToggledEnabled()) {
			KeyBindings keyBindings = KeyBindings.getInstance();
			Component displayName = keyBindings.toggle.getTranslatedKeyMessage();
			addTextLine(lines, minecraft.font, displayName.getString(), maximumWidth);
		}
		return lines;
	}

	private static void addTextLine(List<TextLine> lines, Font font, String text, int maximumWidth) {
		if (font.width(text) < maximumWidth) {
			lines.add(new TextLine(font, text));
		}
	}

	private static int getHelpTextHeight(List<TextLine> lines) {
		if (lines.isEmpty()) {
			return 0;
		}

		int height = HELP_TEXT_GAP;
		for (TextLine line : lines) {
			height += line.font().lineHeight;
		}
		return height;
	}

	private static void renderZoomedItem(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, Minecraft minecraft, int x, int y, int renderSize) {
		TrackingItemStackRenderState renderState = new TrackingItemStackRenderState();
		minecraft.getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.GUI, minecraft.level, minecraft.player, 0);
		ScreenRectangle scissorArea = guiGraphics.peekScissorStack();
		guiGraphics.submitPictureInPictureRenderState(new ZoomedItemRenderState(renderState, x, y, x + renderSize, y + renderSize, renderSize, scissorArea));
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

				guiGraphics.text(itemCountFont, countString, (int) (17.0F - itemCountFont.width(countString)), (int) 9.0F, ARGB.opaque(0xFFFFFF), true);
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

	private record TextLine(Font font, String text) {
	}

}
