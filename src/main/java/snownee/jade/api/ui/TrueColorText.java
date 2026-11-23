package snownee.jade.api.ui;

import com.google.common.collect.Iterators;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class TrueColorText implements Text {

	private final Text delegate;
	private final int color;

	public TrueColorText(Text delegate, int color) {
		this.delegate = Objects.requireNonNull(delegate, "delegate");
		//an alpha of 0 means "not specified" everywhere else in the mod (see IConfigOverlay#applyAlpha),
		//so a 0xRRGGBB value becomes opaque instead of invisible
		this.color = (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
	}

	public Text getDelegate() {
		return delegate;
	}

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
		return delegate.getContent();
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
		return new TrueColorText(delegate.copy(), color);
	}

	@Override
	public Iterator<Text> iterator() {
		return Iterators.transform(delegate.iterator(), this::wrap);
	}

	private Text wrap(Text node) {
		if (node instanceof TrueColorText) {
			//an explicitly coloured child keeps its own colour, as a child style would in 1.20
			return node;
		}
		if (node == delegate) {
			//LiteralText#iterator yields itself first, reuse the wrapper instead of nesting one more
			return this;
		}
		return new TrueColorText(node, color);
	}

	@Override
	public String toString() {
		return "TrueColorText{color=" + Integer.toHexString(color) + ", delegate=" + delegate + '}';
	}

}
