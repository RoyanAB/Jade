package snownee.jade.api.theme;

import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import snownee.jade.Internals;

import java.util.Collection;

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
