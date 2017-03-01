package mezz.itemzoom.client;

import javax.annotation.Nullable;
import java.io.File;

import mezz.itemzoom.ItemZoom;
import mezz.itemzoom.client.compat.JeiCompat;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Config {
	@Nullable
	private static Configuration config;
	private static String category = "itemzoom";
	private static final float MIN_ZOOM = 0.1f;
	private static final float MAX_ZOOM = 1.0f;
	private static final float DEFAULT_ZOOM = MAX_ZOOM;

	private static boolean toggledEnabled = true;
	private static float zoomAmount = DEFAULT_ZOOM;
	private static boolean jeiOnly = false;

	public static boolean isToggledEnabled() {
		return toggledEnabled;
	}

	public static void toggleEnabled() {
		toggledEnabled = !toggledEnabled;
		if (config != null) {
			String configComment = I18n.format("config.itemzoom.toggle.enabled");
			Property property = config.get(category, "toggled.enabled", true, configComment);
			property.set(toggledEnabled);
			if (config.hasChanged()) {
				config.save();
			}
		}
	}

	public static void increaseZoom() {
		setZoomAmount(getZoomAmount() * 1.1f);
	}

	public static void decreaseZoom() {
		setZoomAmount(getZoomAmount() / 1.1f);
	}

	public static float getZoomAmount() {
		return zoomAmount;
	}

	public static void setZoomAmount(float zoomAmount) {
		if (zoomAmount > MAX_ZOOM) {
			zoomAmount = MAX_ZOOM;
		} else if (zoomAmount < MIN_ZOOM) {
			zoomAmount = MIN_ZOOM;
		}

		if (Config.zoomAmount != zoomAmount) {
			Config.zoomAmount = zoomAmount;
			if (config != null) {
				String configComment = I18n.format("config.itemzoom.zoom.amount");
				configComment = configComment + " [range: " + MIN_ZOOM + " ~ " + MAX_ZOOM + ", default: " + DEFAULT_ZOOM + "]";
				Property property = config.get(category, "zoom.amount", DEFAULT_ZOOM, configComment, MIN_ZOOM, MAX_ZOOM);
				property.set(Config.zoomAmount);
				if (config.hasChanged()) {
					config.save();
				}
			}
		}
	}

	public static boolean isJeiOnly() {
		return jeiOnly && JeiCompat.isLoaded();
	}

	public static void preInit(FMLPreInitializationEvent event) {
		File configFile = new File(event.getModConfigurationDirectory(), ItemZoom.MOD_ID + ".cfg");
		config = new Configuration(configFile, "1.0");

		String configComment = I18n.format("config.itemzoom.toggle.enabled");
		toggledEnabled = config.getBoolean("toggled.enabled", category, true, configComment);

		configComment = I18n.format("config.itemzoom.zoom.amount");
		zoomAmount = config.getFloat("zoom.amount", category, DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, configComment);

		configComment = I18n.format("config.itemzoom.jei.only");
		jeiOnly = config.getBoolean("jei.only", category, false, configComment);

		if (config.hasChanged()) {
			config.save();
		}
	}
}
