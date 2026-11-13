package snownee.jade.util;

import com.google.common.collect.Iterators;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Iterator;
import java.util.List;

/**
 * 1.12's {@link Style} can only hold one of the 16 {@link net.minecraft.text.Formatting} colors, so
 * a color chosen by a theme or by a provider would have to be rounded to the nearest vanilla color.
 * <p>
 * This wrapper keeps the exact ARGB color next to the text. {@code DisplayHelper#drawText} walks the
 * text tree and passes that color straight to {@code TextRenderer#draw}, which accepts an arbitrary
 * ARGB value.
 * <p>
 * The wrapper is transparent: style, siblings, content and formatted string are all forwarded to the
 * delegate, so everything that only understands {@code Text}/{@code Style} keeps working.
 */
public class RgbText implements Text {

	private final Text delegate;
	private final int color;

	public RgbText(Text delegate, int color) {
		this.delegate = delegate;
		this.color = (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
	}

	public Text getDelegate() {
		return delegate;
	}

	/**
	 * @return the exact ARGB color that should be used to render this text
	 */
	public int getColor() {
		return color;
	}

	@Override
	public Text setStyle(Style style) {
		delegate.setStyle(style);
		return this;
	}

	@Override
	public Style getStyle() {
		return delegate.getStyle();
	}

	@Override
	public Text append(String text) {
		delegate.append(text);
		return this;
	}

	@Override
	public Text append(Text text) {
		delegate.append(text);
		return this;
	}

	@Override
	public String getContent() {
		return delegate.getString();
	}

	@Override
	public String getString() {
		return delegate.getString();
	}

	@Override
	public String getFormattedString() {
		return delegate.getFormattedString();
	}

	@Override
	public List<Text> getSiblings() {
		return delegate.getSiblings();
	}

	@Override
	public Text copy() {
		return new RgbText(delegate.copy(), color);
	}

	@Override
	public Iterator<Text> iterator() {
		return Iterators.singletonIterator(this);
	}

}
