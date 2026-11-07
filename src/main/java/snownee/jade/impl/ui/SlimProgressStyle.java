package snownee.jade.impl.ui;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.overlay.DisplayHelper;

public class SlimProgressStyle extends ProgressStyle {

	public int color;

	@Override
	public ProgressStyle color(int color, int color2) {
		if (color != color2) {
			throw new UnsupportedOperationException();
		}
		this.color = color;
		return this;
	}

	@Override
	public ProgressStyle textColor(int color) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ProgressStyle direction(Direction2D direction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void render(float x, float y, float w, float h, float progress, Text text) {
		if (overlay != null) {
			Vec2f size = new Vec2f(w * progress, h);
			overlay.size(size);
			overlay.render(x, y, size.x, size.y);
		} else {
			DisplayHelper.INSTANCE.drawGradientProgress(x, y, w, h, progress, color, color);
		}
	}

}
