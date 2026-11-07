package snownee.jade.api.theme;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Style;
import snownee.jade.api.ui.ColorPalette;

public class TextSetting {
	public static final TextSetting DEFAULT = new TextSetting(ColorPalette.DEFAULT, true, null, 0xFFFFFFFF);

	private final ColorPalette colors;
	private final boolean shadow;
	@Nullable
	private final Style modNameStyle;
	private final int itemAmountColor;

	public TextSetting(ColorPalette colors, boolean shadow, @Nullable Style modNameStyle, int itemAmountColor) {
		this.colors = colors;
		this.shadow = shadow;
		this.modNameStyle = modNameStyle;
		this.itemAmountColor = itemAmountColor;
	}

	public ColorPalette colors() {
		return colors;
	}

	public boolean shadow() {
		return shadow;
	}

	@Nullable
	public Style modNameStyle() {
		return modNameStyle;
	}

	public int itemAmountColor() {
		return itemAmountColor;
	}
}
