package mezz.itemzoom;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(ItemZoom.MOD_ID)
public class ItemZoom {
	public static final String MOD_NAME = "Item Zoom";
	public static final String MOD_ID = "itemzoom";

	public ItemZoom() {
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ItemZoomClient::run);
	}

}
