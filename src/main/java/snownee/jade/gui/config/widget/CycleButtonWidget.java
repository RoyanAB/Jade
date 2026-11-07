package snownee.jade.gui.config.widget;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
	protected void drawMessage(net.minecraft.client.Minecraft mc) {
		int valueColor = !active ? 10526880 : getValueColor();
		int labelColor = !active ? 10526880 : 16777215;
		int textY = y + (height - 8) / 2;
		String valueText = nameProvider.apply(value).getString();
		if (displayOnlyValue || label == null) {
			drawCenteredString(mc.textRenderer, valueText, x + width / 2, textY, valueColor);
			return;
		}
		String labelText = label.getString();
		String separator = ": ";
		int labelWidth = mc.textRenderer.getWidth(labelText);
		int separatorWidth = mc.textRenderer.getWidth(separator);
		int valueWidth = mc.textRenderer.getWidth(valueText);
		int textX = x + width / 2 - (labelWidth + separatorWidth + valueWidth) / 2;
		mc.textRenderer.draw(labelText, textX, textY, labelColor);
		textX += labelWidth;
		mc.textRenderer.draw(separator, textX, textY, labelColor);
		textX += separatorWidth;
		mc.textRenderer.draw(valueText, textX, textY, valueColor);
	}

	private int getValueColor() {
		if (value instanceof Boolean) {
			return (Boolean) value ? 0xFFB9F6CA : 0xFFFF8A80;
		}
		return 16777215;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (!active || !visible || button != 0) {
			return false;
		}
		cycle();
		playClickSound(net.minecraft.client.Minecraft.getInstance().getSoundManager());
		return true;
	}

	@Override
	@Nullable
	public Text getTooltip() {
		return tooltipProvider == null ? null : tooltipProvider.apply(value);
	}
}
