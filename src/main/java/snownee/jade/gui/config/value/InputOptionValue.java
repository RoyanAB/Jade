package snownee.jade.gui.config.value;

import java.util.function.Consumer;
import java.util.function.Predicate;

import snownee.jade.gui.config.widget.JadeTextField;

public class InputOptionValue<T> extends OptionValue<T> {

	public static final Predicate<String> INTEGER = s -> s.matches("[-+]?[0-9]+");
	public static final Predicate<String> FLOAT = s -> s.matches("[-+]?([0-9]*\\.[0-9]+|[0-9]+)");

	private final JadeTextField textField;
	private final Predicate<String> validator;

	@SuppressWarnings("unchecked")
	public InputOptionValue(Runnable responder, String optionName, T value, Consumer<T> setter, Predicate<String> validator) {
		super(optionName, setter);
		this.value = value;
		this.validator = validator;
		textField = new JadeTextField(client.textRenderer, 0, 0, 98, 18);
		textField.setText(String.valueOf(value));
		textField.responder = s -> {
			if (this.validator.test(s)) {
				setValue(s);
				textField.setEditableColor(0xFFFFFF);
			} else {
				textField.setEditableColor(0xFF0000);
			}
			responder.run();
		};
		addWidget(textField, 0);
	}

	@SuppressWarnings("unchecked")
	private void setValue(String text) {
		if (value instanceof String)
			value = (T) text;

		try {
			if (value instanceof Integer)
				value = (T) Integer.valueOf(text);
			else if (value instanceof Short)
				value = (T) Short.valueOf(text);
			else if (value instanceof Byte)
				value = (T) Byte.valueOf(text);
			else if (value instanceof Long)
				value = (T) Long.valueOf(text);
			else if (value instanceof Double)
				value = (T) Double.valueOf(text);
			else if (value instanceof Float)
				value = (T) Float.valueOf(text);
		} catch (NumberFormatException e) {
			// no-op
		}

		save();
	}

	@Override
	public boolean isValidValue() {
		return validator.test(textField.getText());
	}

	@Override
	public void setValue(T value) {
		textField.setText(String.valueOf(value));
	}

}
