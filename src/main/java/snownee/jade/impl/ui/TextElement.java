package snownee.jade.impl.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.ITextElement;
import snownee.jade.overlay.DisplayHelper;

public class TextElement extends Element implements ITextElement {

	public final Text text;

	public TextElement(Text text) {
		this.text = text;
	}

	@Override
	public Vec2f getSize() {
		return new Vec2f(Minecraft.getInstance().textRenderer.getWidth(text.getString()), Minecraft.getInstance().textRenderer.fontHeight - 1);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		DisplayHelper.INSTANCE.drawText(text, x, y, IThemeHelper.get().getNormalColor());
	}

	@Override
	@Nullable
	public String getMessage() {
		return text.getString();
	}

	public SpecialTextElement toSpecial() {
		return new SpecialTextElement(text);
	}

	@Override
	public ITextElement scale(float scale) {
		return toSpecial().scale(scale);
	}

	@Override
	public ITextElement zOffset(int zOffset) {
		return toSpecial().zOffset(zOffset);
	}

}
