package snownee.jade.impl.ui;

import com.google.common.base.Preconditions;

import net.minecraft.client.Minecraft;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

public class SimpleProgressStyle extends ProgressStyle {

	public boolean autoTextColor = true;
	public int color;
	public int color2;
	public int textColor;
	public boolean vertical;

	public SimpleProgressStyle() {
		color(0xFFFFFFFF);
	}

	private static double brightness(int rgb) {
		int r = (rgb >> 16) & 255;
		int g = (rgb >> 8) & 255;
		int b = rgb & 255;
		return Math.max(r, Math.max(g, b)) / 255.0;
	}

	@Override
	public ProgressStyle color(int color, int color2) {
		this.color = color;
		this.color2 = color2;
		return this;
	}

	@Override
	public ProgressStyle direction(Direction2D direction) {
		Preconditions.checkArgument(direction == Direction2D.UP || direction == Direction2D.RIGHT, "Only UP and RIGHT are supported");
		super.direction(direction);
		vertical = direction == Direction2D.UP;
		return this;
	}

	@Override
	public void render(float x, float y, float width, float height, float progress, Text text) {
		progress *= choose(true, width, height);
		float progressY = y;
		if (vertical) {
			progressY += height - progress;
		}
		if (progress > 0) {
			if (overlay != null) {
				Vec2f size = new Vec2f(choose(true, progress, width), choose(false, progress, height));
				overlay.size(size);
				overlay.render(x, progressY, size.x, size.y);
			} else {
				Color color3 = Color.rgb(color);
				int lighter = Color.hsl(color3.getHue(), color3.getSaturation(), color3.getLightness() * 0.7f, color3.getOpacity()).toInt();

				float half = choose(true, height, width) / 2;
				DisplayHelper.INSTANCE.drawGradientRect(x, progressY, choose(true, progress, half), choose(false, progress, half), lighter, color, vertical);
				DisplayHelper.INSTANCE.drawGradientRect(x + choose(false, half, 0), progressY + choose(true, half, 0), choose(true, progress, half), choose(false, progress, half), color, lighter, vertical);
				if (color != color2) {
					if (vertical) {
						for (float yy = y + height; yy > progressY; yy -= 2) {
							float fy = Math.max(progressY, yy + 1);
							DisplayHelper.fill(x, yy, x + width, fy, color2);
						}
					} else {
						for (float xx = x + 1; xx < x + progress; xx += 2) {
							float fx = Math.min(x + width, xx + 1);
							DisplayHelper.fill(xx, y, fx, y + height, color2);
						}
					}
				}
			}
		}
		if (text != null) {
			if (autoTextColor) {
				autoTextColor = false;
				if (overlay == null && brightness(color2) > 0.75f) {
					textColor = 0xFF000000;
				} else {
					textColor = 0xFFFFFFFF;
				}
			}
			y += height - Minecraft.getInstance().textRenderer.fontHeight;
			if (vertical && Minecraft.getInstance().textRenderer.fontHeight < progress) {
				y -= progress;
				y += Minecraft.getInstance().textRenderer.fontHeight + 2;
			}
			int color = IConfigOverlay.applyAlpha(textColor, OverlayRenderer.alpha);
			DisplayHelper.INSTANCE.drawText(text, (int) x + 1, (int) y, color);
		}
	}

	private float choose(boolean expand, float x, float y) {
		return vertical ^ expand ? x : y;
	}

	@Override
	public ProgressStyle textColor(int color) {
		textColor = color;
		autoTextColor = false;
		return this;
	}

}
