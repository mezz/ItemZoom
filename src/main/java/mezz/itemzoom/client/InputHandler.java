package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.itemzoom.client.config.Config;

public class InputHandler {
	private final Config config;
	private final KeyBindings keyBindings;
	private boolean enableKeyHeld = false;

	public InputHandler(Config config, KeyBindings keyBindings) {
		this.config = config;
		this.keyBindings = keyBindings;
	}

	public boolean handleInput(InputConstants.Key input) {
		if (keyBindings.toggle.isActiveAndMatches(input)) {
			config.toggleEnabled();
			return true;
		} else if (keyBindings.zoomIn.isActiveAndMatches(input)) {
			config.increaseZoom();
			return true;
		} else if (keyBindings.zoomOut.isActiveAndMatches(input)) {
			config.decreaseZoom();
			return true;
		} else if (keyBindings.hold.isActiveAndMatches(input)) {
			enableKeyHeld = true;
			return true;
		}
		return false;
	}

	public boolean handleInputReleased(InputConstants.Key input) {
		if (keyBindings.hold.isActiveAndMatches(input)) {
			enableKeyHeld = false;
			return true;
		}
		return false;
	}

	public boolean isEnableKeyHeld() {
		return enableKeyHeld;
	}
}
