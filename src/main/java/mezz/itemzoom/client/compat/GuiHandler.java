package mezz.itemzoom.client.compat;

import mezz.itemzoom.client.RenderHandler;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import net.minecraft.client.renderer.Rectangle2d;

import java.util.Collection;
import java.util.Collections;

public class GuiHandler implements IGlobalGuiHandler {
    @Override
    public Collection<Rectangle2d> getGuiExtraAreas() {
        Rectangle2d rendering = RenderHandler.rendering;
        if (rendering != null) {
            return Collections.singleton(rendering);
        }
        return Collections.emptySet();
    }
}
