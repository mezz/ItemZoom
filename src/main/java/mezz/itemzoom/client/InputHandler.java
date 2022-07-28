package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.itemzoom.client.config.Config;

public class InputHandler {
	private final Config config;
	private boolean enableKeyHeld = false;

	public InputHandler(Config config) {
		this.config = config;
	}

	public boolean handleInput(InputConstants.Key input) {
		KeyBindings keyBindings = KeyBindings.getInstance();
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
		KeyBindings keyBindings = KeyBindings.getInstance();
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
