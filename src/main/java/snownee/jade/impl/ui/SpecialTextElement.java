package snownee.jade.impl.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.ITextElement;
import snownee.jade.overlay.DisplayHelper;

public class SpecialTextElement extends TextElement {

	private float scale = 1;
	private int zOffset;

	public SpecialTextElement(Text text) {
		super(text);
	}

	@Override
	public Vec2f getSize() {
		return new Vec2f(Minecraft.getInstance().textRenderer.getWidth(text.getString()) * scale, Minecraft.getInstance().textRenderer.fontHeight * scale + 1);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		GlStateManager.pushMatrix();
		GlStateManager.translatef(x, y + scale, zOffset);
		GlStateManager.scalef(scale, scale, 1);
		DisplayHelper.INSTANCE.drawText(text, 0, 0, IThemeHelper.get().getNormalColor());
		GlStateManager.popMatrix();
	}

	@Override
	public SpecialTextElement toSpecial() {
		return this;
	}

	@Override
	public ITextElement scale(float scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public ITextElement zOffset(int zOffset) {
		this.zOffset = zOffset;
		return this;
	}
}
