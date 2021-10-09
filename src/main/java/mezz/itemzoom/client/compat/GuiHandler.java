package mezz.itemzoom.client.compat;

import mezz.itemzoom.client.RenderHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collection;
import java.util.Collections;

public class GuiHandler implements IGlobalGuiHandler {
    @Override
    public Collection<Rect2i> getGuiExtraAreas() {
        Rect2i rendering = RenderHandler.rendering;
        if (rendering != null) {
            return Collections.singleton(rendering);
        }
        return Collections.emptySet();
    }
}
