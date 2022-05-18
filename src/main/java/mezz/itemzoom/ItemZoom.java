package mezz.itemzoom;

import mezz.itemzoom.client.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;

@Mod(Constants.MOD_ID)
public class ItemZoom {
	public ItemZoom() {
		// Make sure the mod being absent on the other network side does not cause the client to
		// display the server as incompatible
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY,
				(a, b) -> true));

		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ItemZoomClient::run);
	}
}
