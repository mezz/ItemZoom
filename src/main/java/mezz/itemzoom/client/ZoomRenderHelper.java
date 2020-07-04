package mezz.itemzoom.client;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Modified methods from {@link GlStateManager}.
 * When the rendered items are bigger, they need stronger light.
 */
@SuppressWarnings("deprecation")
@OnlyIn(Dist.CLIENT)
public class ZoomRenderHelper {
	private static final Vector3d LIGHT0_POS = (new Vector3d(0.20000000298023224D, 1.0D, -0.699999988079071D)).normalize();
	private static final Vector3d LIGHT1_POS = (new Vector3d(-0.20000000298023224D, 1.0D, 0.699999988079071D)).normalize();

	public static void enableGUIStandardItemLighting(float scale) {
		GlStateManager.pushMatrix();
		GlStateManager.rotatef(-30.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(165.0F, 1.0F, 0.0F, 0.0F);
		enableStandardItemLighting(scale);
		GlStateManager.popMatrix();
	}

	public static void enableStandardItemLighting(float scale) {
		GlStateManager.enableLighting();
		GlStateManager.enableLight(0);
		GlStateManager.enableLight(1);
		GlStateManager.enableColorMaterial();
		GlStateManager.colorMaterial(1032, 5634);
		GlStateManager.light(16384, 4611, GlStateManager.getBuffer((float) LIGHT0_POS.x, (float) LIGHT0_POS.y, (float) LIGHT0_POS.z, 0.0f));
		float lightStrength = 0.3F * scale;
		GlStateManager.light(16384, 4609, GlStateManager.getBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
		GlStateManager.light(16384, 4608, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.light(16384, 4610, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.light(16385, 4611, GlStateManager.getBuffer((float) LIGHT1_POS.x, (float) LIGHT1_POS.y, (float) LIGHT1_POS.z, 0.0f));
		GlStateManager.light(16385, 4609, GlStateManager.getBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
		GlStateManager.light(16385, 4608, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.light(16385, 4610, GlStateManager.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GlStateManager.shadeModel(7424);
		float ambientLightStrength = 0.4F;
		GlStateManager.lightModel(2899, GlStateManager.getBuffer(ambientLightStrength, ambientLightStrength, ambientLightStrength, 1.0F));
	}
}
