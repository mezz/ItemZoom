package mezz.itemzoom.client.config;

import mezz.itemzoom.client.compat.JeiCompat;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

@OnlyIn(Dist.CLIENT)
public class Config {
	private static final int MIN_ZOOM = 10;
	private static final int MAX_ZOOM = 100;
	private static final int DEFAULT_ZOOM = 80;

	private final ConfigValue<Boolean> toggledEnabled;
	private final ConfigValue<Integer> zoomAmount;
	private final ConfigValue<Boolean> jeiOnly;
	private final ConfigValue<Boolean> showHelpText;
	private final ConfigValue<Boolean> showDamageBar;
	private final ConfigValue<Boolean> showStackSize;
	private ForgeConfigSpec configSpec;

	public Config() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("itemzoom");

		toggledEnabled = builder
				.comment(I18n.format("config.itemzoom.toggle.enabled.comment"))
				.translation("config.itemzoom.toggle.enabled")
				.define("toggled.enabled", true);

		zoomAmount = builder
				.comment(I18n.format("config.itemzoom.zoom.amount.comment"))
				.translation("config.itemzoom.zoom.amount")
				.defineInRange("zoom.amount", DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, Integer.class);

		jeiOnly = builder
				.comment(I18n.format("config.itemzoom.jei.only.comment"))
				.translation("config.itemzoom.jei.only")
				.define("jei.only", false);

		showHelpText = builder
				.comment(I18n.format("config.itemzoom.show.help.text.comment"))
				.translation("config.itemzoom.show.help.text")
				.define("show.help.text", true);

		showDamageBar = builder
				.comment(I18n.format("config.itemzoom.show.damage.bar.comment"))
				.translation("config.itemzoom.show.damage.bar")
				.define("show.damage.bar", false);

		showStackSize = builder
				.comment(I18n.format("config.itemzoom.show.stack.size.comment"))
				.translation("config.itemzoom.show.stack.size")
				.define("show.stack.size", false);

		configSpec = builder.build();
	}

	public boolean isToggledEnabled() {
		return toggledEnabled.get();
	}

	public void toggleEnabled() {
		// TODO when forge config supports changing config values at runtime
//		toggledEnabled = !toggledEnabled;
//		if (config != null) {
//			String configComment = I18n.format("config.itemzoom.toggle.enabled");
//			Property property = config.get(category, "toggled.enabled", true, configComment);
//			property.set(toggledEnabled);
//			if (config.hasChanged()) {
//				config.save();
//			}
//		}
	}

	public void increaseZoom() {
		int newZoomAmount = Math.round(getZoomAmount() * 1.1f);
		setZoomAmount(newZoomAmount);
	}

	public void decreaseZoom() {
		int newZoomAmount = Math.round(getZoomAmount() / 1.1f);
		setZoomAmount(newZoomAmount);
	}

	public int getZoomAmount() {
		return zoomAmount.get();
	}

	public boolean showHelpText() {
		return showHelpText.get();
	}

	public boolean showDamageBar() {
		return showDamageBar.get();
	}

	public boolean showStackSize() {
		return showStackSize.get();
	}

	public void setZoomAmount(int zoomAmount) {
		// TODO when forge config supports changing config values at runtime
//		if (zoomAmount > MAX_ZOOM) {
//			zoomAmount = MAX_ZOOM;
//		} else if (zoomAmount < MIN_ZOOM) {
//			zoomAmount = MIN_ZOOM;
//		}
//
//		if (this.zoomAmount != zoomAmount) {
//			this.zoomAmount = zoomAmount;
//			if (config != null) {
//				String configComment = I18n.format("config.itemzoom.zoom.amount");
//				configComment = configComment + " [range: " + MIN_ZOOM + " ~ " + MAX_ZOOM + ", default: " + DEFAULT_ZOOM + "]";
//				Property property = config.get(category, "zoom.amount", DEFAULT_ZOOM, configComment, MIN_ZOOM, MAX_ZOOM);
//				property.set(Config.zoomAmount);
//				if (config.hasChanged()) {
//					config.save();
//				}
//			}
//		}
	}

	public boolean isJeiOnly() {
		return jeiOnly.get() && JeiCompat.isLoaded();
	}

	public ForgeConfigSpec getConfigSpec() {
		return configSpec;
	}
}
