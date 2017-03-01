package mezz.itemzoom;

import mezz.itemzoom.client.config.Config;
import mezz.itemzoom.client.EventHandler;
import mezz.itemzoom.client.KeyBindings;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(
		modid = ItemZoom.MOD_ID,
		name = ItemZoom.MOD_NAME,
		version = ItemZoom.VERSION,
		guiFactory = "mezz.itemzoom.client.config.ItemZoomModGuiFactory",
		clientSideOnly = true
)
public class ItemZoom {
	public static final String MOD_NAME = "Item Zoom";
	public static final String MOD_ID = "itemzoom";
	public static final String VERSION = "@VERSION@";

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void preInit(FMLPreInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new EventHandler());
		Config.preInit(event);
	}

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void init(FMLInitializationEvent event) {
		KeyBindings.init();
	}
}
