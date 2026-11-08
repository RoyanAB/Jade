package snownee.jade.impl.config.entry;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.config.PluginConfig;

import java.util.Locale;

public class EnumConfigEntry<E extends Enum<E>> extends ConfigEntry<E> {

	public EnumConfigEntry(Identifier id, E defaultValue) {
		super(id, defaultValue);
	}

	@Override
	public boolean isValidValue(Object value) {
		if (value.getClass() == String.class) {
			try {
				Enum.valueOf(getDefaultValue().getDeclaringClass(), (String) value);
				return true;
			} catch (Throwable e) {
				return false;
			}
		}
		return value.getClass() == getDefaultValue().getClass();
	}

	@Override
	public void setValue(Object value) {
		if (value.getClass() == String.class) {
			value = Enum.valueOf(getDefaultValue().getDeclaringClass(), (String) value);
		}
		super.setValue(value);
	}

	@Override
	public OptionValue<?> createUI(OptionsList options, String optionName) {
		return options.choices(optionName, getValue(), e -> PluginConfig.INSTANCE.set(id, e), builder -> {
			builder.withTooltip(e -> {
				String key = OptionsList.Entry.makeKey(optionName + "_" + e.name().toLowerCase(Locale.ENGLISH) + "_desc");
				if (!I18n.hasTranslation(key))
					return null;
				return new LiteralText(I18n.translate(key));
			});
		});
	}

}
