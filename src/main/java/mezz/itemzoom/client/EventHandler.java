package mezz.itemzoom.client;

import mezz.itemzoom.ItemZoom;
import mezz.itemzoom.client.compat.JeiCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
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
		final float scale = Config.getZoomAmount() * guiContainer.getGuiLeft() / 17f; // item is 16 wide, give it some extra space on each side
		final float xPosition = (guiContainer.getGuiLeft() / scale - 16f) / 2f;
		final float yPosition = (scaledResolution.getScaledHeight() / scale - 16f) / 2f;
		FontRenderer font = getFontRenderer(minecraft, itemStack);

		GlStateManager.pushMatrix();
		GlStateManager.scale(scale, scale, 1);
		GlStateManager.translate(xPosition, yPosition, 0);
		ZoomRenderHelper.enableGUIStandardItemLighting(scale);

		minecraft.getRenderItem().zLevel += 100;
		minecraft.getRenderItem().renderItemAndEffectIntoGUI(minecraft.player, itemStack, 0, 0);
		minecraft.getRenderItem().renderItemOverlayIntoGUI(font, itemStack, 0, 0, null);
		minecraft.getRenderItem().zLevel -= 100;
		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();

		GlStateManager.popMatrix();

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

	private static FontRenderer getFontRenderer(Minecraft minecraft, ItemStack itemStack) {
		FontRenderer fontRenderer = itemStack.getItem().getFontRenderer(itemStack);
		if (fontRenderer == null) {
			fontRenderer = minecraft.fontRendererObj;
		}
		return fontRenderer;
	}
}
