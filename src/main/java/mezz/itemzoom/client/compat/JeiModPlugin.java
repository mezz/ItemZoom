package mezz.itemzoom.client.compat;

import mezz.itemzoom.client.Constants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {
	private static final ResourceLocation pluginUid = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "plugin");

	@Override
	public ResourceLocation getPluginUid() {
		return pluginUid;
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
