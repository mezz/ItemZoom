package mezz.itemzoom.client.compat;

import mezz.itemzoom.client.Constants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {
	private static final Identifier pluginUid = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "plugin");

	@Override
	public Identifier getPluginUid() {
		return pluginUid;
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGlobalGuiHandler(new GuiHandler());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		JeiCompat.setRuntime(jeiRuntime);
	}

	@Override
	public void onRuntimeUnavailable() {
		JeiCompat.clearRuntime();
	}
}
