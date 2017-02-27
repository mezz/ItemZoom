package mezz.itemzoom.client;

import mezz.itemzoom.ItemZoom;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {
	public static final KeyBinding TOGGLE = new KeyBinding("key.itemzoom.toggle", KeyConflictContext.GUI, KeyModifier.SHIFT, Keyboard.KEY_Z, ItemZoom.MOD_NAME);
	public static final KeyBinding HOLD = new KeyBinding("key.itemzoom.hold", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_NONE, ItemZoom.MOD_NAME);
	public static final KeyBinding ZOOM_IN = new KeyBinding("key.itemzoom.zoom.in", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_NONE, ItemZoom.MOD_NAME);
	public static final KeyBinding ZOOM_OUT = new KeyBinding("key.itemzoom.zoom.out", KeyConflictContext.GUI, KeyModifier.NONE, Keyboard.KEY_NONE, ItemZoom.MOD_NAME);

	public static void init() {
		ClientRegistry.registerKeyBinding(TOGGLE);
		ClientRegistry.registerKeyBinding(HOLD);
		ClientRegistry.registerKeyBinding(ZOOM_IN);
		ClientRegistry.registerKeyBinding(ZOOM_OUT);
	}
}
