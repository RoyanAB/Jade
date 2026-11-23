package snownee.jade.api.ui;

import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

/**
 * A basic render-able element to be drawn in Jade's tooltip.
 *
 * @author Snownee
 */
public interface IElement {

	/**
	 * Force a size that this element reserve
	 */
	IElement size(@Nullable Vec2f size);

	/**
	 * Calculate the default reserved area of this element.
	 * <p>
	 * Modders call getCachedSize instead
	 */
	Vec2f getSize();

	Vec2f getCachedSize();

	/**
	 * Draw method for the renderer.
	 *
	 * @param x    left edge
	 * @param y    top edge
	 * @param maxX Max width this element can expand to
	 * @param maxY Max height this element can expand to
	 */
	void render(float x, float y, float maxX, float maxY);

	IElement align(Align align);

	Align getAlignment();

	/**
	 * Reposition this element with an offset
	 */
	IElement translate(Vec2f translation);

	Vec2f getTranslation();

	/**
	 * Tag this element for identify by other component providers or
	 * adding transition animation if this element is a progress bar
	 */
	IElement tag(Identifier tag);

	Identifier getTag();

	@Nullable
	default String getMessage() {
		return null;
	}

	@Nullable
	String getCachedMessage();

	IElement clearCachedMessage();

	IElement message(@Nullable String message);

	enum Align {
		LEFT, RIGHT, CENTER;
		public static final Align[] VALUES = values();
	}
}
