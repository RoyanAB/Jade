package snownee.jade.api.ui;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Vec2f;

/**
 * A general implementation of {@link IElement}
 *
 * @author Snownee
 */
public abstract class Element implements IElement {

	protected Align align = Align.LEFT;
	protected Vec2f translation = Vec2f.ZERO;
	protected Identifier tag;
	protected Vec2f size;
	private static final String DEFAULT_MESSAGE = "";
	protected String message = DEFAULT_MESSAGE;

	@Override
	public IElement size(@Nullable Vec2f size) {
		this.size = size;
		return this;
	}

	@Override
	public Vec2f getCachedSize() {
		if (size == null)
			size = getSize();
		return size;
	}

	@Override
	public IElement align(Align align) {
		Objects.requireNonNull(align);
		this.align = align;
		return this;
	}

	@Override
	public Align getAlignment() {
		return align;
	}

	@Override
	public IElement translate(Vec2f translation) {
		Objects.requireNonNull(translation);
		this.translation = translation;
		return this;
	}

	@Override
	public Vec2f getTranslation() {
		return translation;
	}

	@Override
	public IElement tag(Identifier tag) {
		this.tag = tag;
		return this;
	}

	@Override
	public Identifier getTag() {
		return tag;
	}

	@Override
	@Nullable
	public String getCachedMessage() {
		if (message == DEFAULT_MESSAGE) {
			message = getMessage();
		}
		return message;
	}

	@Override
	public IElement clearCachedMessage() {
		message = DEFAULT_MESSAGE;
		return this;
	}

	@Override
	public IElement message(@Nullable String message) {
		this.message = message;
		return this;
	}

}
