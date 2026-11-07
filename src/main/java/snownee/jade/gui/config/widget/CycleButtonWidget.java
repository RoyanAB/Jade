package snownee.jade.gui.config.widget;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Text;

public class CycleButtonWidget<T> extends JadeButton {

	private final List<T> values;
	private final Function<T, Text> nameProvider;
	private final Consumer<T> onValueChange;
	@Nullable
	private final Text label;
	private final boolean displayOnlyValue;
	@Nullable
	public Function<T, Text> tooltipProvider;
	private T value;

	public CycleButtonWidget(List<T> values, Function<T, Text> nameProvider, T value, Consumer<T> onValueChange) {
		this(values, nameProvider, value, onValueChange, null, true);
	}

	public CycleButtonWidget(List<T> values, Function<T, Text> nameProvider, T value, Consumer<T> onValueChange, @Nullable Text label, boolean displayOnlyValue) {
		super(0, 0, 100, 20, "");
		this.values = values;
		this.nameProvider = nameProvider;
		this.onValueChange = onValueChange;
		this.label = label;
		this.displayOnlyValue = displayOnlyValue;
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	private String currentMessage() {
		String valueName = nameProvider.apply(value).getFormattedString();
		if (displayOnlyValue || label == null) {
			return valueName;
		}
		return label.getString() + ": " + valueName;
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		this.message = currentMessage();
		super.render(mouseX, mouseY, tickDelta);
	}

	@Override
	public void render(net.minecraft.client.Minecraft mc, int mouseX, int mouseY, float tickDelta) {
		this.message = currentMessage();
		super.render(mc, mouseX, mouseY, tickDelta);
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.Minecraft mc, int mouseX, int mouseY) {
		boolean clicked = super.mouseClicked(mc, mouseX, mouseY);
		if (clicked && active && visible) {
			cycle();
		}
		return clicked;
	}

	private void cycle() {
		if (values.isEmpty()) {
			return;
		}
		int index = values.indexOf(value);
		T next = values.get((index + 1) % values.size());
		value = next;
		onValueChange.accept(next);
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (!active || !visible || button != 0) {
			return false;
		}
		cycle();
		return true;
	}

	@Override
	@Nullable
	public Text getTooltip() {
		return tooltipProvider == null ? null : tooltipProvider.apply(value);
	}
}
