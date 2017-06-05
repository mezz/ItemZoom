package mezz.itemzoom.client;

import mezz.itemzoom.ItemZoom;
import mezz.itemzoom.client.compat.JeiCompat;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class EventHandler {
	@SubscribeEvent(priority = EventPriority.LOW)
	public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (Keyboard.getEventKeyState()) {
			int eventKey = Keyboard.getEventKey();
			if (KeyBindings.TOGGLE.isActiveAndMatches(eventKey)) {
				Config.toggleEnabled();
				event.setCanceled(true);
			} else if (KeyBindings.ZOOM_IN.isActiveAndMatches(eventKey)) {
				Config.increaseZoom();
				event.setCanceled(true);
			} else if (KeyBindings.ZOOM_OUT.isActiveAndMatches(eventKey)) {
				Config.decreaseZoom();
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onItemStackTooltip(RenderTooltipEvent.Pre event) {
		if (!Config.isToggledEnabled() && !isEnableKeyHeld()) {
			return;
		}
		ItemStack itemStack = event.getStack();
		//noinspection ConstantConditions
		if (itemStack == null || itemStack.isEmpty()) {
			return;
		}
		if (Config.isJeiOnly() && !ItemStack.areItemStacksEqual(itemStack, JeiCompat.getStackUnderMouse())) {
			return;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen currentScreen = minecraft.currentScreen;
		if (currentScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) currentScreen;
			renderZoomedStack(itemStack, guiContainer, minecraft);
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (ItemZoom.MOD_ID.equals(eventArgs.getModID())) {
			Config.load();
		}
	}

	private static boolean isEnableKeyHeld() {
		if (Keyboard.getEventKeyState()) {
			int eventKey = Keyboard.getEventKey();
			if (KeyBindings.HOLD.isActiveAndMatches(eventKey)) {
				return true;
			}
		}
		return false;
	}

	private static void renderZoomedStack(ItemStack itemStack, GuiContainer guiContainer, Minecraft minecraft) {
		ScaledResolution scaledResolution = new ScaledResolution(minecraft);
		final float scale = Config.getZoomAmount() / 100f * guiContainer.getGuiLeft() / 17f; // item is 16 wide, give it some extra space on each side
		final float xPosition = (guiContainer.getGuiLeft() / scale - 16f) / 2f;
		final float yPosition = (scaledResolution.getScaledHeight() / scale - 16f) / 2f;
		FontRenderer font = getFontRenderer(minecraft, itemStack);

		GlStateManager.pushMatrix();
		GlStateManager.scale(scale, scale, 1);
		GlStateManager.translate(xPosition, yPosition, 0);
		ZoomRenderHelper.enableGUIStandardItemLighting(scale);

		minecraft.getRenderItem().zLevel += 100;
		minecraft.getRenderItem().renderItemAndEffectIntoGUI(minecraft.player, itemStack, 0, 0);
		renderItemOverlayIntoGUI(font, itemStack);
		minecraft.getRenderItem().zLevel -= 100;
		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();

		if (Config.showHelpText()) {
			String modName = ItemZoom.MOD_NAME;
			int stringWidth = font.getStringWidth(modName);
			int x = (guiContainer.getGuiLeft() - stringWidth) / 2;
			int y = (scaledResolution.getScaledHeight() + Math.round(17 * scale)) / 2;
			font.drawString(modName, x, y, 4210752);

			if (Config.isToggledEnabled()) {
				String toggleText = KeyBindings.TOGGLE.getDisplayName();
				stringWidth = font.getStringWidth(toggleText);
				x = (guiContainer.getGuiLeft() - stringWidth) / 2;
				y += font.FONT_HEIGHT;
				font.drawString(toggleText, x, y, 4210752);
			}
		}
	}

	private static FontRenderer getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}

	public static void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack) {
		if (!stack.isEmpty()) {
			if (Config.showStackSize() && stack.getCount() != 1) {
				String s = String.valueOf(stack.getCount());
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableBlend();
				fr.drawStringWithShadow(s, (float) (17 - fr.getStringWidth(s)), 9f, 16777215);
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
				// Fixes opaque cooldown overlay a bit lower
				// TODO: check if enabled blending still screws things up down the line.
				GlStateManager.enableBlend();
			}

			if (Config.showDamageBar() && stack.getItem().showDurabilityBar(stack)) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();
				GlStateManager.disableAlpha();
				GlStateManager.disableBlend();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer vertexbuffer = tessellator.getBuffer();
				double health = stack.getItem().getDurabilityForDisplay(stack);
				int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
				int i = Math.round(13.0F - (float) health * 13.0F);
				draw(vertexbuffer, 2, 13, 13, 2, 0, 0, 0, 255);
				draw(vertexbuffer, 2, 13, i, 1, rgbfordisplay >> 16 & 255, rgbfordisplay >> 8 & 255, rgbfordisplay & 255, 255);
				GlStateManager.enableBlend();
				GlStateManager.enableAlpha();
				GlStateManager.enableTexture2D();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}

			EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
			float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

			if (f3 > 0.0F) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepth();
				GlStateManager.disableTexture2D();
				Tessellator tessellator1 = Tessellator.getInstance();
				VertexBuffer vertexbuffer1 = tessellator1.getBuffer();
				draw(vertexbuffer1, 0, MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
				GlStateManager.enableTexture2D();
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
			}
		}
	}

	private static void draw(VertexBuffer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
		renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		renderer.pos((double) (x), (double) (y), 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos((double) (x), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos((double) (x + width), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos((double) (x + width), (double) (y), 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();
	}

}
