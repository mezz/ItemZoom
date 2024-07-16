package mezz.itemzoom;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.itemzoom.client.Constants;
import mezz.itemzoom.client.InputHandler;
import mezz.itemzoom.client.KeyBindings;
import mezz.itemzoom.client.RenderHandler;
import mezz.itemzoom.client.config.Config;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class ItemZoom {
	public ItemZoom() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		ModContainer activeContainer = modLoadingContext.getActiveContainer();
		IEventBus modEventBus = activeContainer.getEventBus();
		if (modEventBus == null) {
			throw new IllegalStateException("Missing mod event bus");
		}

		Config config = new Config();
		activeContainer.registerConfig(ModConfig.Type.CLIENT, config.getConfigSpec());

        modEventBus.addListener(EventPriority.NORMAL, false, RegisterKeyMappingsEvent.class, KeyBindings::create);
        modEventBus.addListener(EventPriority.NORMAL, false, ModConfigEvent.Loading.class, configLoadingEvent -> {
            setup(config);
        });
    }

	private static void setup(Config config) {
		InputHandler inputHandler = new InputHandler(config);
		RenderHandler renderHandler = new RenderHandler(config, inputHandler::isEnableKeyHeld);

		IEventBus eventBus = NeoForge.EVENT_BUS;
		setupInputHandler(inputHandler, eventBus);
		setupRenderHandler(renderHandler, eventBus);
	}

	private static void setupInputHandler(InputHandler inputHandler, IEventBus eventBus) {
		eventBus.addListener(EventPriority.LOW, false, ScreenEvent.KeyPressed.Post.class, (event) -> {
			InputConstants.Key input = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
			if (inputHandler.handleInput(input)) {
				event.setCanceled(true);
			}
		});
		eventBus.addListener(EventPriority.LOW, false, ScreenEvent.KeyReleased.Post.class, (event) -> {
			InputConstants.Key input = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
			if (inputHandler.handleInputReleased(input)) {
				event.setCanceled(true);
			}
		});
		eventBus.addListener(EventPriority.LOW, false, ScreenEvent.MouseButtonPressed.Pre.class, (event) -> {
			InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(event.getButton());
			if (inputHandler.handleInput(input)) {
				event.setCanceled(true);
			}
		});
		eventBus.addListener(EventPriority.LOW, false, ScreenEvent.MouseButtonReleased.Pre.class, (event) -> {
			InputConstants.Key input = InputConstants.Type.MOUSE.getOrCreate(event.getButton());
			if (inputHandler.handleInputReleased(input)) {
				event.setCanceled(true);
			}
		});
	}

	private static void setupRenderHandler(RenderHandler renderHandler, IEventBus eventBus) {
		eventBus.addListener(EventPriority.NORMAL, false, ScreenEvent.Render.Post.class, (event) -> {
			renderHandler.onScreenDrawn();
		});
		eventBus.addListener(EventPriority.NORMAL, false, RenderTooltipEvent.Pre.class, (event) -> {
			renderHandler.onItemStackTooltip(event.getGraphics(), event.getItemStack(), event.getX(), event.getY());
		});
	}
}
