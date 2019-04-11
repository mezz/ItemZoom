package mezz.itemzoom.client;

import mezz.itemzoom.client.config.Config;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InputHandler {
	private final Config config;
	private final KeyBindings keyBindings;
	private boolean enableKeyHeld = false;

	public InputHandler(Config config, KeyBindings keyBindings) {
		this.config = config;
		this.keyBindings = keyBindings;
	}

	public boolean handleInput(InputMappings.Input input) {
		if (keyBindings.toggle.isActiveAndMatches(input)) {
			config.toggleEnabled();
			return true;
		// TODO when forge config supports changing config values at runtime
//		} else if (keyBindings.zoomIn.isActiveAndMatches(input)) {
//			config.increaseZoom();
//			return true;
//		} else if (keyBindings.zoomOut.isActiveAndMatches(input)) {
//			config.decreaseZoom();
//			return true;
		} else if (keyBindings.hold.isActiveAndMatches(input)) {
			enableKeyHeld = true;
			return true;
		}
		return false;
	}

	public boolean handleInputReleased(InputMappings.Input input) {
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
