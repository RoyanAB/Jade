package snownee.jade.impl.ui;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.Element;
import snownee.jade.overlay.DisplayHelper;

public class ItemStackElement extends Element {

	private final ItemStack stack;
	private final float scale;
	private final String text;
	public static final ItemStackElement EMPTY = new ItemStackElement(ItemStack.EMPTY, 1, null);

	private ItemStackElement(ItemStack stack, float scale, @Nullable String text) {
		this.stack = stack;
		this.scale = scale == 0 ? 1 : scale;
		this.text = text;
	}

	public static ItemStackElement of(ItemStack stack) {
		return of(stack, 1);
	}

	public static ItemStackElement of(ItemStack stack, float scale) {
		return of(stack, scale, null);
	}

	public static ItemStackElement of(ItemStack stack, float scale, @Nullable String text) {
		if (scale == 1 && stack.isEmpty()) {
			return EMPTY;
		}
		return new ItemStackElement(stack, scale, text);
	}

	@Override
	public Vec2f getSize() {
		int size = MathHelper.floor(18 * scale);
		return new Vec2f(size, size);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		if (stack.isEmpty())
			return;
		DisplayHelper.INSTANCE.drawItem(x + 1, y + 1, stack, scale, text);
	}

	@Override
	@Nullable
	public String getMessage() {
		if (stack.isEmpty())
			return null;
		return stack.getSize() + " " + stack.getHoverName();
	}

}
