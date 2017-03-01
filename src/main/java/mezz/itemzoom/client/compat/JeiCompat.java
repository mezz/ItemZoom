package mezz.itemzoom.client.compat;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import mezz.jei.api.IItemListOverlay;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;

public class JeiCompat {
	public static Optional<IItemListOverlay> itemListOverlay = Optional.absent();

	@Nullable
	public static ItemStack getStackUnderMouse() {
		Optional<IItemListOverlay> itemListOverlay = JeiCompat.itemListOverlay;
		if (itemListOverlay.isPresent()) {
			ItemStack stackUnderMouse = itemListOverlay.get().getStackUnderMouse();
			if (stackUnderMouse != null) {
				return stackUnderMouse;
			}
		}
		return null;
	}

	public static boolean isLoaded() {
		return Loader.isModLoaded("JEI");
	}
}
