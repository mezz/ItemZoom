package mezz.itemzoom;

import mezz.itemzoom.client.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class ItemZoom {
	public ItemZoom() {
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ItemZoomClient::run);
	}
}
