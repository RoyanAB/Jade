package snownee.jade.api.ui;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resource.Identifier;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.impl.ui.StyledElement;
import snownee.jade.overlay.DisplayHelper;

public abstract class BoxStyle implements Cloneable {
	private static final int[] DEFAULT_PADDING = new int[]{4, 4, 4, 4};
	public final float[] boxProgressOffset;
	public final int[] padding;
	public ColorPalette boxProgressColors;

	public BoxStyle(@Nullable float[] boxProgressOffset, ColorPalette boxProgressColors, @Nullable int[] padding) {
		this.boxProgressOffset = boxProgressOffset;
		this.boxProgressColors = boxProgressColors;
		this.padding = padding == null ? DEFAULT_PADDING.clone() : padding;
	}

	public static BoxStyle getNestedBox() {
		return IThemeHelper.get().theme().nestedBoxStyle;
	}

	public static BoxStyle getViewGroup() {
		return IThemeHelper.get().theme().viewGroupStyle;
	}

	public static GradientBorder getTransparent() {
		return GradientBorder.TRANSPARENT;
	}

	public static BoxStyle getSprite(Identifier sprite, @Nullable int[] padding) {
		return new BoxStyle.SpriteBase(null, ColorPalette.DEFAULT, padding, sprite, null);
	}

	public abstract void render(StyledElement element, float x, float y, float w, float h, float alpha);

	public abstract float borderWidth();

	public float boxProgressOffset(Direction2D dir) {
		return boxProgressOffset == null ? 0 : boxProgressOffset[dir.ordinal()];
	}

	public int padding(Direction2D dir) {
		return padding[dir.ordinal()];
	}

	@Override
	public abstract BoxStyle clone();

	public boolean hasRoundCorner() {
		return false;
	}

	public static BoxStyle fromJson(JsonObject o) {
		if (o.has("sprite")) {
			return SpriteBase.fromJson(o);
		}
		return GradientBorder.fromJson(o);
	}

	protected static int[] readIntArray(JsonObject o, String key, int[] def) {
		if (!o.has(key)) {
			return def;
		}
		JsonElement e = o.get(key);
		if (e.isJsonPrimitive()) {
			int i = e.getAsInt();
			return new int[]{i, i, i, i};
		}
		JsonArray a = e.getAsJsonArray();
		int[] ret = new int[a.size()];
		for (int i = 0; i < ret.length; i++) {
			JsonElement element = a.get(i);
			if (element.getAsJsonPrimitive().isString()) {
				ret[i] = Color.valueOf(element.getAsString()).toInt();
			} else {
				ret[i] = element.getAsInt();
			}
		}
		return ret;
	}

	protected static float[] readFloatArray(JsonObject o, String key) {
		if (!o.has(key)) {
			return null;
		}
		JsonElement e = o.get(key);
		if (e.isJsonPrimitive()) {
			float f = e.getAsFloat();
			return new float[]{f, f, f, f};
		}
		JsonArray a = e.getAsJsonArray();
		float[] ret = new float[a.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = a.get(i).getAsFloat();
		}
		return ret;
	}

	protected static int getColor(JsonObject o, String key, int def) {
		if (!o.has(key)) {
			return def;
		}
		try {
			return Color.valueOf(o.get(key).getAsString()).toInt();
		} catch (Exception e) {
			return def;
		}
	}

	public static class GradientBorder extends BoxStyle {
		public static final GradientBorder TRANSPARENT = new GradientBorder(null, ColorPalette.DEFAULT, null, -1, new int[]{-1, -1, -1, -1}, 0, false);
		public static final GradientBorder DEFAULT_NESTED_BOX = new GradientBorder(null, ColorPalette.DEFAULT, null, -1, new int[]{0xFF808080, 0xFF808080, 0xFF808080, 0xFF808080}, 1, null);
		public static final GradientBorder DEFAULT_VIEW_GROUP = new GradientBorder(null, ColorPalette.DEFAULT, new int[]{2, 2, 2, 2}, 0x44444444, new int[]{0x44444444, 0x44444444, 0x44444444, 0x44444444}, 0.75F, null);
		public int bgColor;
		public int[] borderColor;
		public float borderWidth;
		@Nullable
		public Boolean roundCorner;

		public GradientBorder(@Nullable float[] boxProgressOffset, ColorPalette boxProgressColors, @Nullable int[] padding, int bgColor, int[] borderColor, float borderWidth, @Nullable Boolean roundCorner) {
			super(boxProgressOffset, boxProgressColors, padding);
			this.bgColor = bgColor;
			this.borderColor = borderColor;
			this.borderWidth = borderWidth;
			this.roundCorner = roundCorner;
		}

		public static GradientBorder fromJson(JsonObject o) {
			ColorPalette colors = ColorPalette.DEFAULT;
			if (o.has("boxProgressColors")) {
				JsonObject c = o.getAsJsonObject("boxProgressColors");
				colors = new ColorPalette(getColor(c, "normal", ColorPalette.DEFAULT.normal()), getColor(c, "info", ColorPalette.DEFAULT.info()), getColor(c, "title", ColorPalette.DEFAULT.title()), getColor(c, "success", ColorPalette.DEFAULT.success()), getColor(c, "warning", ColorPalette.DEFAULT.warning()), getColor(c, "danger", ColorPalette.DEFAULT.danger()), getColor(c, "failure", ColorPalette.DEFAULT.failure()));
			}
			Boolean round = o.has("roundCorner") ? o.get("roundCorner").getAsBoolean() : null;
			return new GradientBorder(readFloatArray(o, "boxProgressOffset"), colors, readIntArray(o, "padding", null), getColor(o, "backgroundColor", -1), readIntArray(o, "borderColor", new int[]{-1, -1, -1, -1}), o.has("borderWidth") ? o.get("borderWidth").getAsFloat() : 1F, round);
		}

		@Override
		public float borderWidth() {
			return borderWidth;
		}

		@Override
		public void render(StyledElement element, float x, float y, float w, float h, float alpha) {
			boolean roundCorner = hasRoundCorner();
			if (bgColor != -1) {
				int bg = IWailaConfig.IConfigOverlay.applyAlpha(bgColor, alpha);
				DisplayHelper.INSTANCE.drawGradientRect(x + borderWidth, y + borderWidth, w - borderWidth - borderWidth, h - borderWidth - borderWidth, bg, bg);
				if (roundCorner) {
					DisplayHelper.INSTANCE.drawGradientRect(x, y - 1, w, 1, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(x, y + h, w, 1, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(x - 1, y, 1, h, bg, bg);
					DisplayHelper.INSTANCE.drawGradientRect(x + w, y, 1, h, bg, bg);
				}
			}
			if (borderWidth > 0) {
				int[] borderColors = new int[4];
				for (int i = 0; i < 4; i++) {
					if (borderColor[i] != -1) {
						borderColors[i] = IWailaConfig.IConfigOverlay.applyAlpha(borderColor[i], alpha);
					}
				}
				DisplayHelper.INSTANCE.drawGradientRect(x, y + borderWidth, borderWidth, h - borderWidth - borderWidth, borderColors[0], borderColors[3]);
				DisplayHelper.INSTANCE.drawGradientRect(x + w - borderWidth, y + borderWidth, borderWidth, h - borderWidth - borderWidth, borderColors[1], borderColors[2]);
				DisplayHelper.INSTANCE.drawGradientRect(x, y, w, borderWidth, borderColors[0], borderColors[1]);
				DisplayHelper.INSTANCE.drawGradientRect(x, y + h - borderWidth, w, borderWidth, borderColors[3], borderColors[2]);
			}
		}

		@Override
		public GradientBorder clone() {
			return new GradientBorder(boxProgressOffset == null ? null : boxProgressOffset.clone(), boxProgressColors, padding == null ? null : padding.clone(), bgColor, borderColor.clone(), borderWidth, roundCorner);
		}

		@Override
		public boolean hasRoundCorner() {
			return roundCorner == null ? !IWailaConfig.get().getOverlay().getSquare() : roundCorner;
		}
	}

	public static class SpriteBase extends BoxStyle {
		public Identifier sprite;
		@Nullable
		public Identifier withIconSprite;

		public SpriteBase(@Nullable float[] boxProgressOffset, ColorPalette boxProgressColors, @Nullable int[] padding, Identifier sprite, @Nullable Identifier withIconSprite) {
			super(boxProgressOffset, boxProgressColors, padding);
			this.sprite = sprite;
			this.withIconSprite = withIconSprite;
		}

		public static SpriteBase fromJson(JsonObject o) {
			ColorPalette colors = ColorPalette.DEFAULT;
			if (o.has("boxProgressColors")) {
				JsonObject c = o.getAsJsonObject("boxProgressColors");
				colors = new ColorPalette(getColor(c, "normal", ColorPalette.DEFAULT.normal()), getColor(c, "info", ColorPalette.DEFAULT.info()), getColor(c, "title", ColorPalette.DEFAULT.title()), getColor(c, "success", ColorPalette.DEFAULT.success()), getColor(c, "warning", ColorPalette.DEFAULT.warning()), getColor(c, "danger", ColorPalette.DEFAULT.danger()), getColor(c, "failure", ColorPalette.DEFAULT.failure()));
			}
			return new SpriteBase(readFloatArray(o, "boxProgressOffset"), colors, readIntArray(o, "padding", null), new Identifier(o.get("sprite").getAsString()), o.has("withIconSprite") ? new Identifier(o.get("withIconSprite").getAsString()) : null);
		}

		@Override
		public void render(StyledElement element, float x, float y, float w, float h, float alpha) {
			Identifier texture = sprite;
			if (withIconSprite != null && element.getIcon() != null) {
				texture = withIconSprite;
			}
			DisplayHelper.INSTANCE.drawTexture(texture, x, y, w, h, alpha);
		}

		@Override
		public float borderWidth() {
			return 0;
		}

		@Override
		public SpriteBase clone() {
			return new SpriteBase(boxProgressOffset == null ? null : boxProgressOffset.clone(), boxProgressColors, padding == null ? null : padding.clone(), sprite, withIconSprite);
		}
	}
}
