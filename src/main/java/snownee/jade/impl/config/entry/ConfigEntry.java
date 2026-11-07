package snownee.jade.impl.config.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.resource.Identifier;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;

public abstract class ConfigEntry<T> {

	public final Identifier id;
	private final T defaultValue;
	private boolean synced;
	protected T value;
	private List<Consumer<Identifier>> listeners = new ArrayList<>();

	public ConfigEntry(Identifier id, T defaultValue) {
		this.id = id;
		this.value = this.defaultValue = defaultValue;
	}

	public Identifier getId() {
		return id;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public T getValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if (!Objects.equals(this.value, value)) {
			this.value = (T) value;
			notifyChange();
		}
	}

	public abstract boolean isValidValue(Object value);

	public abstract OptionValue<?> createUI(OptionsList options, String optionName);

	public void addListener(Consumer<Identifier> listener) {
		listeners.add(listener);
	}

	public void notifyChange() {
		for (Consumer<Identifier> listener : listeners) {
			listener.accept(id);
		}
	}
}
