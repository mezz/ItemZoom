package mezz.itemzoom;

import mezz.itemzoom.client.InputHandler;
import mezz.itemzoom.client.KeyBindings;
import mezz.itemzoom.client.RenderHandler;
import mezz.itemzoom.client.config.Config;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ItemZoom.MOD_ID)
public class ItemZoom {
	public static final String MOD_NAME = "Item Zoom";
	public static final String MOD_ID = "itemzoom";

	public ItemZoom() {
		DistExecutor.runWhenOn(Dist.CLIENT, ()->()-> {
			Config config = new Config();
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, config.getConfigSpec());

			IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
			modEventBus.addListener(EventPriority.NORMAL, false, FMLClientSetupEvent.class, event -> this.setup(config));
		});
	}

	private void setup(Config config) {
		KeyBindings keyBindings = new KeyBindings();
		InputHandler inputHandler = new InputHandler(config, keyBindings);
		RenderHandler renderHandler = new RenderHandler(config, inputHandler::isEnableKeyHeld, keyBindings.toggle.func_197978_k());

		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		setupInputHandler(inputHandler, eventBus);
		setupRenderHandler(renderHandler, eventBus);
	}

	private static void setupInputHandler(InputHandler inputHandler, IEventBus eventBus) {
		eventBus.addListener(EventPriority.LOW, false, GuiScreenEvent.KeyboardKeyPressedEvent.Post.class, (event) -> {
			InputMappings.Input input = InputMappings.getInputByCode(event.getKeyCode(), event.getScanCode());
			if (inputHandler.handleInput(input)) {
				event.setCanceled(true);
			}
		});
		eventBus.addListener(EventPriority.LOW, false, GuiScreenEvent.KeyboardKeyReleasedEvent.Post.class, (event) -> {
			InputMappings.Input input = InputMappings.getInputByCode(event.getKeyCode(), event.getScanCode());
			if (inputHandler.handleInputReleased(input)) {
				event.setCanceled(true);
			}
		});
		eventBus.addListener(EventPriority.LOW, false, GuiScreenEvent.MouseClickedEvent.Post.class, (event) -> {
			InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(event.getButton());
			if (inputHandler.handleInput(input)) {
				event.setCanceled(true);
			}
		});
		eventBus.addListener(EventPriority.LOW, false, GuiScreenEvent.MouseReleasedEvent.Post.class, (event) -> {
			InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(event.getButton());
			if (inputHandler.handleInputReleased(input)) {
				event.setCanceled(true);
			}
		});
	}

	private static void setupRenderHandler(RenderHandler renderHandler, IEventBus eventBus) {
		eventBus.addListener(EventPriority.NORMAL, false, GuiScreenEvent.DrawScreenEvent.Post.class, (event) -> {
			renderHandler.onScreenDrawn();
		});
		eventBus.addListener(EventPriority.NORMAL, false, RenderTooltipEvent.Pre.class, (event) -> {
			renderHandler.onItemStackTooltip(event.getStack(), event.getX());
		});
	}
}
