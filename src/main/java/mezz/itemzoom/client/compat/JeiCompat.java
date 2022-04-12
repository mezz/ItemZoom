package mezz.itemzoom.client.compat;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientListOverlay;
import net.minecraft.world.item.ItemStack;

public class JeiCompat {
	public static Optional<IIngredientListOverlay> ingredientListOverlay = Optional.empty();
	public static Optional<IBookmarkOverlay> bookmarkOverlay = Optional.empty();
	private static final List<Supplier<Optional<ItemStack>>> suppliers = List.of(
			JeiCompat::getIngredientOverlayStack,
			JeiCompat::getBookmarkOverlayStack
	);

	public static ItemStack getStackUnderMouse() {
		return suppliers.stream()
				.map(Supplier::get)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElse(ItemStack.EMPTY);
	}

	private static Optional<ItemStack> getIngredientOverlayStack() {
        return ingredientListOverlay.map(i -> i.getIngredientUnderMouse(VanillaTypes.ITEM_STACK));
    }

	private static Optional<ItemStack> getBookmarkOverlayStack() {
		return bookmarkOverlay.map(b -> b.getIngredientUnderMouse(VanillaTypes.ITEM_STACK));
	}

	public static boolean isLoaded() {
		return ingredientListOverlay.isPresent();
	}
}
