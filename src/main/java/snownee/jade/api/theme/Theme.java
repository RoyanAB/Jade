package snownee.jade.api.theme;

import net.minecraft.resource.Identifier;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IBoxElement;

public class Theme {

	public static final Identifier DEFAULT_THEME_ID = Identifiers.JADE("dark");
	public BoxStyle tooltipStyle;
	public BoxStyle nestedBoxStyle;
	public BoxStyle viewGroupStyle;
	public Identifier id;
	public TextSetting text;
	public Boolean changeRoundCorner;
	public float changeOpacity;
	public boolean lightColorScheme;
	public boolean hidden;
	public Identifier iconSlotSprite;
	public int iconSlotInflation;
	public IBoxElement iconSlotSpriteCache;

	public Theme(BoxStyle tooltipStyle, BoxStyle nestedBoxStyle, BoxStyle viewGroupStyle, TextSetting text, @Nullable Boolean changeRoundCorner, float changeOpacity, boolean lightColorScheme, boolean hidden, @Nullable Identifier iconSlotSprite, int iconSlotInflation) {
		this.tooltipStyle = tooltipStyle;
		this.nestedBoxStyle = nestedBoxStyle;
		this.viewGroupStyle = viewGroupStyle;
		this.text = text;
		this.changeRoundCorner = changeRoundCorner;
		this.changeOpacity = changeOpacity;
		this.lightColorScheme = lightColorScheme;
		this.hidden = hidden;
		this.iconSlotSprite = iconSlotSprite;
		this.iconSlotInflation = iconSlotInflation;
	}

}
