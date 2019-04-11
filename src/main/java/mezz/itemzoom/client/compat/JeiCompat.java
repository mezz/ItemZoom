package mezz.itemzoom.client.compat;

import java.util.Optional;

import mezz.jei.api.runtime.IIngredientListOverlay;
import net.minecraft.item.ItemStack;

public class JeiCompat {
	public static Optional<IIngredientListOverlay> ingredientListOverlay = Optional.empty();

	public static ItemStack getStackUnderMouse() {
		return ingredientListOverlay
				.map(IIngredientListOverlay::getIngredientUnderMouse)
				.filter(ingredient -> ingredient instanceof ItemStack)
				.map(ingredient -> (ItemStack) ingredient)
				.orElse(ItemStack.EMPTY);
	}

	public static boolean isLoaded() {
		return ingredientListOverlay.isPresent();
	}
}
