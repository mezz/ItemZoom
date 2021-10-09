package mezz.itemzoom.client.compat;

import java.util.Optional;

import mezz.itemzoom.client.Constants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {

	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Constants.MOD_ID, "plugin");
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGlobalGuiHandler(new GuiHandler());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		IIngredientListOverlay ingredientListOverlay = jeiRuntime.getIngredientListOverlay();
		JeiCompat.ingredientListOverlay = Optional.of(ingredientListOverlay);
	}
}
