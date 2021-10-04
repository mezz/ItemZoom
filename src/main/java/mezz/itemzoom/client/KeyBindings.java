package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
	public final KeyMapping toggle;
	public final KeyMapping hold;
	public final KeyMapping zoomIn;
	public final KeyMapping zoomOut;

	public KeyBindings() {
		InputConstants.Key zKey = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Z);
		InputConstants.Key none = InputConstants.UNKNOWN;
		String category = Constants.MOD_NAME;
		KeyMapping[] allBindings = {
			toggle = new KeyMapping("key.itemzoom.toggle", KeyConflictContext.GUI, KeyModifier.SHIFT, zKey, category),
			hold = new KeyMapping("key.itemzoom.hold", KeyConflictContext.GUI, KeyModifier.NONE, none, category),
			zoomIn = new KeyMapping("key.itemzoom.zoom.in", KeyConflictContext.GUI, KeyModifier.NONE, none, category),
			zoomOut = new KeyMapping("key.itemzoom.zoom.out", KeyConflictContext.GUI, KeyModifier.NONE, none, category)
		};
		for (KeyMapping binding : allBindings) {
			ClientRegistry.registerKeyBinding(binding);
		}
	}
}
