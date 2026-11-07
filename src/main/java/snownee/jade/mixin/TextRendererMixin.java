package snownee.jade.mixin;

import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.gui.JadeFont;

@Mixin(TextRenderer.class)
public abstract class TextRendererMixin implements JadeFont {

	@Shadow
	private float x;
	@Shadow
	private float r;
	@Shadow
	private float g;
	@Shadow
	private float b;
	@Shadow
	private float a;

	private float jade$glint1 = Float.NaN;
	private float jade$glint2 = Float.NaN;
	private float jade$glint1Strength = 1;
	private float jade$glint2Strength = 1;

	private void jade$applyGlint() {
		if (Float.isNaN(jade$glint1)) {
			return;
		}
		float dist = Math.abs(x - jade$glint1);
		float localGlint1 = 0.65F + MathHelper.clamp(1 - dist / 20, 0, 1) * 0.35F * jade$glint1Strength;
		dist = Math.abs(x - jade$glint2);
		float localGlint2 = 0.65F + MathHelper.clamp(1 - dist / 20, 0, 1) * 0.35F * jade$glint2Strength;
		float factor = Math.max(localGlint1, localGlint2);
		GlStateManager.color4f(r, g, b, a * factor);
	}

	@Inject(method = "drawBasicGlyph", at = @At("HEAD"))
	private void jade$glintBasicGlyph(int index, boolean italic, CallbackInfoReturnable<Float> cir) {
		jade$applyGlint();
	}

	@Inject(method = "drawUnicodeGlyph", at = @At("HEAD"))
	private void jade$glintUnicodeGlyph(char chr, boolean italic, CallbackInfoReturnable<Float> cir) {
		jade$applyGlint();
	}

	@Override
	public void jade$setGlint(float glint1, float glint2) {
		jade$glint1 = glint1;
		jade$glint2 = glint2;
	}

	@Override
	public void jade$setGlintStrength(float glint1Strength, float glint2Strength) {
		jade$glint1Strength = glint1Strength;
		jade$glint2Strength = glint2Strength;
	}
}
