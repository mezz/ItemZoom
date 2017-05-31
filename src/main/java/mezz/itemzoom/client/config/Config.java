package mezz.itemzoom.client.config;

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
	private static final String category = "itemzoom";
	private static final int MIN_ZOOM = 10;
	private static final int MAX_ZOOM = 100;
	private static final int DEFAULT_ZOOM = MAX_ZOOM;

	private static boolean toggledEnabled = true;
	private static int zoomAmount = DEFAULT_ZOOM;
	private static boolean jeiOnly = false;
	private static boolean showHelpText = true;

	@Nullable
	public static Configuration getConfig() {
		return config;
	}

	public static String getCategory() {
		return category;
	}

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
		int newZoomAmount = Math.round(getZoomAmount() * 1.1f);
		setZoomAmount(newZoomAmount);
	}

	public static void decreaseZoom() {
		int newZoomAmount = Math.round(getZoomAmount() / 1.1f);
		setZoomAmount(newZoomAmount);
	}

	public static int getZoomAmount() {
		return zoomAmount;
	}

	public static boolean showHelpText() {
		return showHelpText;
	}

	public static void setZoomAmount(int zoomAmount) {
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
		load();
	}

	public static void load() {
		if (config == null) {
			return;
		}

		String configComment = I18n.format("config.itemzoom.toggle.enabled.comment");
		toggledEnabled = config.getBoolean("toggled.enabled", category, true, configComment, "config.itemzoom.toggle.enabled");

		configComment = I18n.format("config.itemzoom.zoom.amount.comment");
		zoomAmount = config.getInt("zoom.amount", category, DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, configComment, "config.itemzoom.zoom.amount");

		configComment = I18n.format("config.itemzoom.jei.only.comment");
		jeiOnly = config.getBoolean("jei.only", category, false, configComment, "config.itemzoom.jei.only");

		configComment = I18n.format("config.itemzoom.show.help.text.comment");
		showHelpText = config.getBoolean("show.help.text", category, true, configComment, "config.itemzoom.show.help.text");

		if (config.hasChanged()) {
			config.save();
		}
	}
}
