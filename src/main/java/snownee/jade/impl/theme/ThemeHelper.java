package snownee.jade.impl.theme;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.Resource;
import net.minecraft.client.resource.manager.ResourceManager;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.TextSetting;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.ColorPalette;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ThemeHelper implements IThemeHelper {
	public static final ThemeHelper INSTANCE = new ThemeHelper();
	public static final Identifier ID = new Identifier(Jade.MODID, "themes");
	private static final List<String> BUILT_IN_THEMES = Arrays.asList("dark", "create", "top", "waila");
	private final Map<Identifier, Theme> themes = new TreeMap<>(Comparator.comparing(Identifier::toString));
	private Theme fallback;

	public static Style colorStyle(int color) {
		Formatting formatting = nearestFormatting(color);
		return new Style().setColor(formatting);
	}

	private static Formatting nearestFormatting(int color) {
		Formatting best = Formatting.GRAY;
		double bestDist = Double.MAX_VALUE;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		for (Formatting formatting : Formatting.values()) {
			if (!formatting.isColor()) {
				continue;
			}
			int c = formatting.getId();
			int cr = (c >> 16) & 0xFF;
			int cg = (c >> 8) & 0xFF;
			int cb = c & 0xFF;
			double dist = (r - cr) * (r - cr) + (g - cg) * (g - cg) + (b - cb) * (b - cb);
			if (dist < bestDist) {
				bestDist = dist;
				best = formatting;
			}
		}
		return best;
	}

	public void reload() {
		ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if (resourceManager == null) {
			return;
		}
		Set<Identifier> existingKeys = new java.util.HashSet<>(themes.keySet());
		Theme enable = null;
		WailaConfig.ConfigOverlay config = Jade.CONFIG.get().getOverlay();
		themes.clear();
		for (String name : BUILT_IN_THEMES) {
			Identifier id = new Identifier(Jade.MODID, "jade_themes/" + name + ".json");
			JsonObject json;
			try (Resource resource = resourceManager.getResource(id); InputStream in = resource.asStream()) {
				json = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			} catch (Exception e) {
				continue;
			}
			Identifier themeId = new Identifier(Jade.MODID, name);
			int version = json.has("version") ? json.get("version").getAsInt() : 0;
			if (version < 100 || version > 199) {
				Jade.LOGGER.warn("Theme {} has unsupported version {}. Skipping.", themeId, version);
				continue;
			}
			try {
				Theme theme = parse(themeId, json);
				themes.put(themeId, theme);
				if (enable == null && json.has("autoEnable") && json.get("autoEnable").getAsBoolean() && !existingKeys.contains(themeId)) {
					enable = theme;
				}
			} catch (Exception e) {
				Jade.LOGGER.error("Failed to load theme " + themeId, e);
			}
		}
		fallback = themes.get(Theme.DEFAULT_THEME_ID);
		if (fallback == null) {
			fallback = defaultTheme();
			themes.put(Theme.DEFAULT_THEME_ID, fallback);
		}
		int hash = 0;
		for (Identifier id : themes.keySet()) {
			hash = 31 * hash + id.hashCode();
		}
		if (hash != config.themesHash) {
			if (hash != 0 && enable != null) {
				config.activeTheme = enable.id;
				Jade.LOGGER.info("Auto enabled theme {}", enable.id);
				if (enable.changeRoundCorner != null) {
					config.setSquare(enable.changeRoundCorner);
				}
				if (enable.changeOpacity != 0) {
					config.setAlpha(enable.changeOpacity);
				}
			}
			config.themesHash = hash;
			Jade.CONFIG.save();
		}
		config.applyTheme(config.activeTheme);
	}

	private Theme defaultTheme() {
		return new Theme(BoxStyle.GradientBorder.DEFAULT_NESTED_BOX.clone(), BoxStyle.GradientBorder.DEFAULT_NESTED_BOX.clone(), BoxStyle.GradientBorder.DEFAULT_VIEW_GROUP.clone(), TextSetting.DEFAULT, null, 0, false, false, null, 0);
	}

	private static Theme parse(Identifier id, JsonObject o) {
		BoxStyle tooltipStyle = BoxStyle.fromJson(o.getAsJsonObject("tooltipStyle"));
		BoxStyle nestedBoxStyle = o.has("nestedBoxStyle") ? BoxStyle.fromJson(o.getAsJsonObject("nestedBoxStyle")) : BoxStyle.GradientBorder.DEFAULT_NESTED_BOX.clone();
		BoxStyle viewGroupStyle = o.has("viewGroupStyle") ? BoxStyle.fromJson(o.getAsJsonObject("viewGroupStyle")) : BoxStyle.GradientBorder.DEFAULT_VIEW_GROUP.clone();
		TextSetting text = TextSetting.DEFAULT;
		if (o.has("text")) {
			JsonObject t = o.getAsJsonObject("text");
			ColorPalette colors = ColorPalette.DEFAULT;
			if (t.has("colors")) {
				JsonObject c = t.getAsJsonObject("colors");
				colors = new ColorPalette(getColor(c, "normal", ColorPalette.DEFAULT.normal()), getColor(c, "info", ColorPalette.DEFAULT.info()), getColor(c, "title", ColorPalette.DEFAULT.title()), getColor(c, "success", ColorPalette.DEFAULT.success()), getColor(c, "warning", ColorPalette.DEFAULT.warning()), getColor(c, "danger", ColorPalette.DEFAULT.danger()), getColor(c, "failure", ColorPalette.DEFAULT.failure()));
			}
			boolean shadow = !t.has("shadow") || t.get("shadow").getAsBoolean();
			int itemAmountColor = getColor(t, "itemAmountColor", 0xFFFFFFFF);
			text = new TextSetting(colors, shadow, null, itemAmountColor);
		}
		Boolean roundCorner = o.has("changeRoundCorner") ? o.get("changeRoundCorner").getAsBoolean() : null;
		float changeOpacity = o.has("changeOpacity") ? o.get("changeOpacity").getAsFloat() : 0;
		boolean light = o.has("lightColorScheme") && o.get("lightColorScheme").getAsBoolean();
		boolean hidden = o.has("hidden") && o.get("hidden").getAsBoolean();
		Identifier iconSprite = o.has("iconSlotSprite") ? new Identifier(o.get("iconSlotSprite").getAsString()) : null;
		int inflation = o.has("iconSlotInflation") ? o.get("iconSlotInflation").getAsInt() : 0;
		Theme theme = new Theme(tooltipStyle, nestedBoxStyle, viewGroupStyle, text, roundCorner, changeOpacity, light, hidden, iconSprite, inflation);
		theme.id = id;
		return theme;
	}

	private static int getColor(JsonObject o, String key, int def) {
		if (!o.has(key)) {
			return def;
		}
		try {
			return Color.valueOf(o.get(key).getAsString()).toInt();
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public Theme theme() {
		Theme theme = OverlayRenderer.theme();
		if (theme == null) {
			if (fallback == null) {
				fallback = defaultTheme();
			}
			theme = fallback;
		}
		return theme;
	}

	@Override
	public Collection<Theme> getThemes() {
		return themes.values();
	}

	@Override
	@NotNull
	public Theme getTheme(Identifier id) {
		Theme theme = themes.getOrDefault(id, fallback);
		if (theme == null) {
			theme = theme();
		}
		return Objects.requireNonNull(theme);
	}

	@Override
	public Text info(Object componentOrString) {
		return color(componentOrString, theme().text.colors().info());
	}

	@Override
	public Text success(Object componentOrString) {
		return color(componentOrString, theme().text.colors().success());
	}

	@Override
	public Text warning(Object componentOrString) {
		return color(componentOrString, theme().text.colors().warning());
	}

	@Override
	public Text danger(Object componentOrString) {
		return color(componentOrString, theme().text.colors().danger());
	}

	@Override
	public Text failure(Object componentOrString) {
		return color(componentOrString, theme().text.colors().failure());
	}

	@Override
	public Text title(Object componentOrString) {
		Text text;
		if (componentOrString instanceof Text) {
			text = (Text) componentOrString;
		} else {
			text = new LiteralText(Objects.toString(componentOrString));
		}
		return color(DisplayHelper.INSTANCE.stripColor(text), theme().text.colors().title());
	}

	@Override
	public Text modName(Object componentOrString) {
		Text text;
		if (componentOrString instanceof Text) {
			text = (Text) componentOrString;
		} else {
			text = new LiteralText(Objects.toString(componentOrString));
		}
		return text.setStyle(IWailaConfig.get().getFormatting().getItemModNameStyle());
	}

	@Override
	public Text seconds(int ticks) {
		ticks /= 20;
		if (ticks >= 60) {
			int minutes = ticks / 60;
			ticks %= 60;
			if (ticks == 0) {
				return info(JadeClient.format("jade.minutes", minutes));
			} else {
				return info(JadeClient.format("jade.minutes_seconds", minutes, ticks));
			}
		}
		return info(JadeClient.format("jade.seconds", ticks));
	}

	protected Text color(Object componentOrString, int color) {
		if (componentOrString instanceof Text) {
			return ((Text) componentOrString).setStyle(colorStyle(color));
		} else {
			return new LiteralText(Objects.toString(componentOrString)).setStyle(colorStyle(color));
		}
	}
}
