package mezz.itemzoom.client;

import mezz.itemzoom.ItemZoom;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class KeyBindings {
	public final KeyBinding toggle;
	public final KeyBinding hold;
	// TODO when forge config supports changing config values at runtime
//	public final KeyBinding zoomIn;
//	public final KeyBinding zoomOut;

	public KeyBindings() {
		InputMappings.Input zKey = InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_Z);
		InputMappings.Input none = InputMappings.INPUT_INVALID;
		String category = ItemZoom.MOD_NAME;
		KeyBinding[] allBindings = {
			toggle = new KeyBinding("key.itemzoom.toggle", KeyConflictContext.GUI, KeyModifier.SHIFT, zKey, category),
			hold = new KeyBinding("key.itemzoom.hold", KeyConflictContext.GUI, KeyModifier.NONE, none, category),
//			zoomIn = new KeyBinding("key.itemzoom.zoom.in", KeyConflictContext.GUI, KeyModifier.NONE, none, category),
//			zoomOut = new KeyBinding("key.itemzoom.zoom.out", KeyConflictContext.GUI, KeyModifier.NONE, none, category)
		};
		for (KeyBinding binding : allBindings) {
			ClientRegistry.registerKeyBinding(binding);
		}
	}
}
