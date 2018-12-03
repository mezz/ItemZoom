package mezz.itemzoom.client.compat;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import mezz.itemzoom.client.EventHandler;
import mezz.jei.api.IIngredientListOverlay;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IGlobalGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

@JEIPlugin
public class JeiModPlugin implements IModPlugin {
	@Override
	public void register(IModRegistry registry) {
		try {
			registry.addGlobalGuiHandlers(new IGlobalGuiHandler() {
				@Override
				public Collection<Rectangle> getGuiExtraAreas() {
					if (EventHandler.rendering) {
						GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
						if (currentScreen instanceof GuiContainer) {
							GuiContainer guiContainer = (GuiContainer) currentScreen;
							return Collections.singleton(new Rectangle(0, 0, guiContainer.getGuiLeft(), guiContainer.height));
						}
					}
					return Collections.emptySet();
				}
			});
		} catch (RuntimeException | LinkageError ignored) {
			// only JEI 4.14.0 or higher supports addGlobalGuiHandlers
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		IIngredientListOverlay ingredientListOverlay = jeiRuntime.getIngredientListOverlay();
		JeiCompat.ingredientListOverlay = Optional.of(ingredientListOverlay);
	}
}
