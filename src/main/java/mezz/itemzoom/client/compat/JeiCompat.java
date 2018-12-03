package mezz.itemzoom.client.compat;

import java.util.Optional;

import mezz.jei.api.IIngredientListOverlay;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public class JeiCompat {
	public static Optional<IIngredientListOverlay> ingredientListOverlay = Optional.empty();

	public static ItemStack getStackUnderMouse() {
		Optional<IIngredientListOverlay> ingredientListOverlay = JeiCompat.ingredientListOverlay;
		if (ingredientListOverlay.isPresent()) {
			Object ingredientUnderMouse = ingredientListOverlay.get().getIngredientUnderMouse();
			if (ingredientUnderMouse instanceof ItemStack) {
				return (ItemStack) ingredientUnderMouse;
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean isLoaded() {
		return Loader.isModLoaded("jei");
	}
}
