package mezz.itemzoom.client.compat;

import com.google.common.base.Optional;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IItemListOverlay;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class JeiModPlugin extends BlankModPlugin {
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		IItemListOverlay itemListOverlay = jeiRuntime.getItemListOverlay();
		JeiCompat.itemListOverlay = Optional.of(itemListOverlay);
	}
}
