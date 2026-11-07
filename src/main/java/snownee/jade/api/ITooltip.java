package snownee.jade.api;

import java.util.List;
import java.util.function.UnaryOperator;

import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;

/**
 * Tooltip that you can append text and other render-able stuffs to.
 *
 * @author Snownee
 */
public interface ITooltip {

	void clear();

	/**
	 * Returns tooltip's number of lines
	 */
	int size();

	default boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Add a text to a new line
	 */
	default void add(Text text) {
		add(text, null);
	}

	/**
	 * Add a tagged text to a new line
	 */
	default void add(Text text, Identifier tag) {
		add(size(), text, tag);
	}

	default void add(int index, Text text) {
		add(index, text, null);
	}

	default void add(int index, Text text, Identifier tag) {
		add(index, IElementHelper.get().text(text).tag(tag));
	}

	default void addAll(List<Text> texts) {
		texts.forEach(this::add);
	}

	/**
	 * Add a render-able element to a new line
	 */
	default void add(IElement element) {
		add(size(), element);
	}

	default void add(int index, List<IElement> elements) {
		boolean first = true;
		for (IElement element : elements) {
			if (first) {
				add(index, element);
			} else {
				append(index, element);
			}
			first = false;
		}
	}

	default void add(List<IElement> elements) {
		add(size(), elements);
	}

	void add(int index, IElement element);

	/**
	 * Append a text to the last line
	 * <p>
	 * IMPORTANT: DO NOT use this to concat texts
	 */
	default void append(Text text) {
		append(text, null);
	}

	/**
	 * Append a tagged text to the last line
	 * <p>
	 * IMPORTANT: DO NOT use this to concat texts
	 */
	default void append(Text text, Identifier tag) {
		append(IElementHelper.get().text(text).tag(tag));
	}

	/**
	 * Append a render-able element to the last line
	 */
	default void append(IElement element) {
		append(size() - 1, element);
	}

	/**
	 * Append render-able elements to the last line
	 */
	default void append(int index, List<IElement> elements) {
		for (IElement element : elements) {
			append(index, element);
		}
	}

	void append(int index, IElement element);

	/**
	 * Clear all elements that are tagged with this tag
	 *
	 * @return true if any element is removed
	 */
	boolean remove(Identifier tag);

	/**
	 * Replace all elements that are tagged with this tag at the position of the first found element
	 *
	 * @return true if any element is replaced
	 */
	boolean replace(Identifier tag, UnaryOperator<List<List<IElement>>> elements);

	boolean replace(Identifier tag, Text text);

	@Deprecated
	IElementHelper getElementHelper();

	/**
	 * Get all elements that are tagged with this tag
	 */
	List<IElement> get(Identifier tag);

	List<IElement> get(int index, Align align);

	String getMessage();

	void setLineMargin(int index, Direction2D side, int margin);
}
