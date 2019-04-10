package mezz.itemzoom.client.compat;

import java.util.Optional;

import mezz.jei.api.runtime.IIngredientListOverlay;
import net.minecraft.item.ItemStack;

public class JeiCompat {
	public static Optional<IIngredientListOverlay> ingredientListOverlay = Optional.empty();

	public static ItemStack getStackUnderMouse() {
		if (ingredientListOverlay.isPresent()) {
			Object ingredientUnderMouse = ingredientListOverlay.get().getIngredientUnderMouse();
			if (ingredientUnderMouse instanceof ItemStack) {
				return (ItemStack) ingredientUnderMouse;
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean isLoaded() {
		return ingredientListOverlay.isPresent();
	}
}
