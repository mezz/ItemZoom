package mezz.itemzoom.client.compat;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import mezz.itemzoom.ItemZoom;
import mezz.itemzoom.client.RenderHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(ItemZoom.MOD_ID, "plugin");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGlobalGuiHandler(new IGlobalGuiHandler() {
			@Override
			public Collection<Rectangle2d> getGuiExtraAreas() {
				if (RenderHandler.rendering) {
					Screen currentScreen = Minecraft.getInstance().currentScreen;
					if (currentScreen instanceof ContainerScreen) {
						ContainerScreen<?> containerScreen = (ContainerScreen<?>) currentScreen;
						return Collections.singleton(new Rectangle2d(0, 0, containerScreen.getGuiLeft(), containerScreen.field_230709_l_));
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
