package snownee.jade.impl.config.entry;

import net.minecraft.resource.Identifier;
import net.minecraft.util.math.MathHelper;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.impl.config.PluginConfig;

public class IntConfigEntry extends ConfigEntry<Integer> {

	private final boolean slider;
	private final int min;
	private final int max;

	public IntConfigEntry(Identifier id, int defaultValue, int min, int max, boolean slider) {
		super(id, defaultValue);
		this.slider = slider;
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean isValidValue(Object value) {
		return value instanceof Number && ((Number) value).intValue() >= min && ((Number) value).intValue() <= max;
	}

	@Override
	public void setValue(Object value) {
		super.setValue(((Number) value).intValue());
	}

	@Override
	public OptionValue<?> createUI(OptionsList options, String optionName) {
		if (slider) {
			return options.slider(optionName, getValue(), f -> PluginConfig.INSTANCE.set(id, (int) (float) f), min, max, f -> (float) Math.round(f));
		} else {
			return options.input(optionName, getValue(), i -> PluginConfig.INSTANCE.set(id, MathHelper.clamp(i, min, max)), InputOptionValue.INTEGER.and($ -> {
				try {
					return isValidValue(Integer.valueOf($));
				} catch (NumberFormatException e) {
					return false;
				}
			}));
		}
	}

}
