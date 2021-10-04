package mezz.itemzoom.client.compat;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import mezz.itemzoom.client.Constants;
import mezz.itemzoom.client.RenderHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Constants.MOD_ID, "plugin");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGlobalGuiHandler(new IGlobalGuiHandler() {
			@Override
			public Collection<Rect2i> getGuiExtraAreas() {
				if (RenderHandler.rendering) {
					Minecraft minecraft = Minecraft.getInstance();
					Screen currentScreen = minecraft.screen;
					if (currentScreen instanceof AbstractContainerScreen<?> containerScreen) {
						Rect2i rect = new Rect2i(0, 0, containerScreen.getGuiLeft(), containerScreen.getGuiTop());
						return Collections.singleton(rect);
					}
				}
				return Collections.emptySet();
			}
		});
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		IIngredientListOverlay ingredientListOverlay = jeiRuntime.getIngredientListOverlay();
		JeiCompat.ingredientListOverlay = Optional.of(ingredientListOverlay);
	}
}
