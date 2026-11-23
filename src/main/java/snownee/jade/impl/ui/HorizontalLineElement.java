package snownee.jade.impl.ui;

import net.minecraft.util.math.Vec2f;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class HorizontalLineElement extends Element {

	public int color = IThemeHelper.get().getNormalColor();

	@Override
	public Vec2f getSize() {
		return new Vec2f(10, 4);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		y = (y + maxY - 0.5F) / 2;
		DisplayHelper.fill(x + 2, y, maxX - 2, y + 0.5F, color);
		if (IThemeHelper.get().theme().text.shadow()) {
			x += 0.5F;
			y += 0.5F;
			maxX += 0.5F;
			Color shadow = Color.rgb(color);
			shadow = Color.rgb(shadow.getRed() / 4, shadow.getGreen() / 4, shadow.getBlue() / 4, shadow.getOpacity());
			DisplayHelper.fill(x + 2, y, maxX - 2, y + 0.5F, shadow.toInt());
		}
	}

}
