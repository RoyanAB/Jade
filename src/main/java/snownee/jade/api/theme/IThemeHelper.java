package snownee.jade.api.theme;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import net.minecraft.text.Text;
import net.minecraft.resource.Identifier;
import snownee.jade.Internals;

public interface IThemeHelper {
	static IThemeHelper get() {
		return Internals.getThemeHelper();
	}

	Theme theme();

	default int getNormalColor() {
		return theme().text.colors().normal();
	}

	Collection<Theme> getThemes();

	@NotNull
	Theme getTheme(Identifier id);

	Text info(Object componentOrString);

	Text success(Object componentOrString);

	Text warning(Object componentOrString);

	Text danger(Object componentOrString);

	Text failure(Object componentOrString);

	Text title(Object componentOrString);

	Text modName(Object componentOrString);

	Text seconds(int ticks);

	default boolean isLightColorScheme() {
		return theme().lightColorScheme;
	}
}
