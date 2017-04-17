package mezz.itemzoom.client.config;

import java.util.ArrayList;
import java.util.List;

import mezz.itemzoom.ItemZoom;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ItemZoomModConfigGui extends GuiConfig {
	public ItemZoomModConfigGui(GuiScreen parent) {
		super(parent, getConfigElements(), ItemZoom.MOD_ID, false, false, getTitle());
	}

	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> configElements = new ArrayList<IConfigElement>();

		Configuration config = Config.getConfig();
		if (config != null) {
			ConfigCategory category = config.getCategory(Config.getCategory());
			configElements.addAll(new ConfigElement(category).getChildElements());
		}

		return configElements;
	}

	private static String getTitle() {
		String titleFormatKey = "config.itemzoom.title";
		String titleFormat;
		if (I18n.canTranslate(titleFormatKey)) {
			titleFormat = I18n.translateToLocal(titleFormatKey);
		} else {
			titleFormat = I18n.translateToFallback(titleFormatKey);
		}
		return titleFormat.replace("%MODNAME", ItemZoom.MOD_NAME);
	}
}
