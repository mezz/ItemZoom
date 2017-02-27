package mezz.itemzoom.client;

import java.io.File;

import mezz.itemzoom.ItemZoom;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Config {
	private static Configuration config;
	private static String category = "itemzoom";

	private static boolean toggledEnabled = true;

	public static boolean isToggledEnabled() {
		return toggledEnabled;
	}

	public static void toggleEnabled() {
		toggledEnabled = !toggledEnabled;
		if (config != null) {
			Property property = config.get(category, "toggledEnabled", true);
			property.set(toggledEnabled);
			if (config.hasChanged()) {
				config.save();
			}
		}
	}

	public static void preInit(FMLPreInitializationEvent event) {
		File configFile = new File(event.getModConfigurationDirectory(), ItemZoom.MOD_ID + ".cfg");
		config = new Configuration(configFile, "1.0");
		String configComment = I18n.format("key.itemzoom.toggle");
		toggledEnabled = config.getBoolean("toggledEnabled", category, true, configComment);
		if (config.hasChanged()) {
			config.save();
		}
	}
}
