package snownee.jade.gui.config.value;

import net.minecraft.text.Text;
import snownee.jade.gui.config.widget.CycleButtonWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CycleOptionValue<T> extends OptionValue<T> {

	private final CycleButtonWidget<T> button;

	public CycleOptionValue(String optionName, Builder<T> builder, T value, Consumer<T> setter) {
		super(optionName, setter);
		this.value = value;
		this.button = new CycleButtonWidget<>(builder.values, builder.nameProvider, value, v -> {
			this.value = v;
			save();
		}, null, true);
		this.button.tooltipProvider = builder.tooltipProvider;
		addWidget(button, 0);
	}

	@Override
	public void setValue(T value) {
		this.value = value;
		button.setValue(value);
		save();
	}

	public static class Builder<T> {
		private final Function<T, Text> nameProvider;
		private List<T> values = new ArrayList<>();
		public Function<T, Text> tooltipProvider;

		public Builder(Function<T, Text> nameProvider) {
			this.nameProvider = nameProvider;
		}

		public Builder<T> withValues(List<T> values) {
			this.values = values;
			return this;
		}

		public Builder<T> withTooltip(Function<T, Text> tooltipProvider) {
			this.tooltipProvider = tooltipProvider;
			return this;
		}

		public Builder<T> displayOnlyValue() {
			return this;
		}
	}
}
