package snownee.jade.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class CallbackContainer<T> {

	private final PriorityStore<T, T> priorities = new PriorityStore<>($ -> 0, Function.identity());

	public void add(int priority, T callback) {
		Objects.requireNonNull(callback);
		priorities.put(callback, priority);
	}

	public void sort() {
		priorities.sort(Collections.emptySet());
	}

	public List<T> callbacks() {
		return priorities.getSortedList();
	}

	public void call(Consumer<T> consumer) {
		for (T callback : callbacks()) {
			consumer.accept(callback);
		}
	}

}
