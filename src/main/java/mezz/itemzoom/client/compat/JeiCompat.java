package mezz.itemzoom.client.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import mezz.jei.api.runtime.IScreenHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class JeiCompat {
	private static @Nullable IIngredientListOverlay ingredientListOverlay = null;
	private static @Nullable IBookmarkOverlay bookmarkOverlay = null;
	private static @Nullable IRecipesGui recipesGui = null;
	private static @Nullable IScreenHelper screenHelper = null;

	private JeiCompat() {
	}

	public static void setRuntime(IJeiRuntime jeiRuntime) {
		ingredientListOverlay = jeiRuntime.getIngredientListOverlay();
		bookmarkOverlay = jeiRuntime.getBookmarkOverlay();
		recipesGui = jeiRuntime.getRecipesGui();
		screenHelper = jeiRuntime.getScreenHelper();
	}

	public static void clearRuntime() {
		ingredientListOverlay = null;
		bookmarkOverlay = null;
		recipesGui = null;
		screenHelper = null;
	}

	public static ItemStack getStackUnderMouse() {
		ItemStack itemStack = getIngredientOverlayStack();
		if (!itemStack.isEmpty()) {
			return itemStack;
		}

		itemStack = getBookmarkOverlayStack();
		if (!itemStack.isEmpty()) {
			return itemStack;
		}

		return getRecipesGuiStackUnderMouse();
	}

	public static ItemStack getRecipesGuiStackUnderMouse() {
		IRecipesGui currentRecipesGui = recipesGui;
		if (currentRecipesGui == null) {
			return ItemStack.EMPTY;
		}
		return currentRecipesGui.getIngredientUnderMouse(VanillaTypes.ITEM_STACK)
				.orElse(ItemStack.EMPTY);
	}

	public static @Nullable Screen getRecipesGuiParentScreen() {
		IRecipesGui currentRecipesGui = recipesGui;
		if (currentRecipesGui == null) {
			return null;
		}
		return currentRecipesGui.getParentScreen().orElse(null);
	}

	public static @Nullable GuiArea getGuiArea(Screen screen) {
		IScreenHelper currentScreenHelper = screenHelper;
		if (currentScreenHelper == null) {
			return null;
		}
		return currentScreenHelper.getGuiProperties(screen)
				.map(GuiArea::new)
				.orElse(null);
	}

	private static ItemStack getIngredientOverlayStack() {
		IIngredientListOverlay currentIngredientListOverlay = ingredientListOverlay;
		if (currentIngredientListOverlay == null) {
			return ItemStack.EMPTY;
		}
		return orEmpty(currentIngredientListOverlay.getIngredientUnderMouse(VanillaTypes.ITEM_STACK));
	}

	private static ItemStack getBookmarkOverlayStack() {
		IBookmarkOverlay currentBookmarkOverlay = bookmarkOverlay;
		if (currentBookmarkOverlay == null) {
			return ItemStack.EMPTY;
		}
		return orEmpty(currentBookmarkOverlay.getIngredientUnderMouse(VanillaTypes.ITEM_STACK));
	}

	private static ItemStack orEmpty(@Nullable ItemStack itemStack) {
		if (itemStack == null) {
			return ItemStack.EMPTY;
		}
		return itemStack;
	}

	public static boolean isLoaded() {
		return ingredientListOverlay != null;
	}

	public record GuiArea(int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		public GuiArea(IGuiProperties guiProperties) {
			this(
					guiProperties.guiLeft(),
					guiProperties.guiTop(),
					guiProperties.guiXSize(),
					guiProperties.guiYSize(),
					guiProperties.screenWidth(),
					guiProperties.screenHeight()
			);
		}
	}
}
