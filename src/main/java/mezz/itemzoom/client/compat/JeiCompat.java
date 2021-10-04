package mezz.itemzoom.client.compat;

import java.util.Optional;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientListOverlay;
import net.minecraft.world.item.ItemStack;

public class JeiCompat {
	public static Optional<IIngredientListOverlay> ingredientListOverlay = Optional.empty();

	public static ItemStack getStackUnderMouse() {
		return ingredientListOverlay
				.map(i -> i.getIngredientUnderMouse(VanillaTypes.ITEM))
				.orElse(ItemStack.EMPTY);
	}

	public static boolean isLoaded() {
		return ingredientListOverlay.isPresent();
	}
}
