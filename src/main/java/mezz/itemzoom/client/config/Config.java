package mezz.itemzoom.client.config;

import mezz.itemzoom.client.compat.JeiCompat;
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
	private final ForgeConfigSpec configSpec;

	public Config() {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		builder.push("itemzoom");

		toggledEnabled = builder
				.comment("If set to \"false\", Item Zoom will be disabled.")
				.translation("config.itemzoom.toggle.enabled")
				.define("toggled.enabled", true);

		zoomAmount = builder
				.comment("Set lower amount to make the item zoom less.")
				.translation("config.itemzoom.zoom.amount")
				.defineInRange("zoom.amount", DEFAULT_ZOOM, MIN_ZOOM, MAX_ZOOM, Integer.class);

		jeiOnly = builder
				.comment("Zoom items only from the JEI overlay with ingredients list.")
				.translation("config.itemzoom.jei.only")
				.define("jei.only", false);

		showHelpText = builder
				.comment("Display name \"Item Zoom\" and the hotkey to toggle this mod below the zoomed item.")
				.translation("config.itemzoom.show.help.text")
				.define("show.help.text", true);

		showDamageBar = builder
				.comment("Display the item's durability bar when zoomed.")
				.translation("config.itemzoom.show.damage.bar")
				.define("show.damage.bar", false);

		showStackSize = builder
				.comment("Display the item's stack size when zoomed.")
				.translation("config.itemzoom.show.stack.size")
				.define("show.stack.size", false);

		configSpec = builder.build();
	}

	public boolean isToggledEnabled() {
		return toggledEnabled.get();
	}

	public void toggleEnabled() {
		toggledEnabled.set(!toggledEnabled.get());
		toggledEnabled.save();
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
		if (zoomAmount > MAX_ZOOM) {
			zoomAmount = MAX_ZOOM;
		} else if (zoomAmount < MIN_ZOOM) {
			zoomAmount = MIN_ZOOM;
		}

		int oldZoomAmount = this.zoomAmount.get();
		if (oldZoomAmount != zoomAmount) {
			this.zoomAmount.set(zoomAmount);
			this.zoomAmount.save();
		}
	}

	public boolean isJeiOnly() {
		return jeiOnly.get() && JeiCompat.isLoaded();
	}

	public ForgeConfigSpec getConfigSpec() {
		return configSpec;
	}
}
