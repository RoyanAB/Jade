package snownee.jade.impl.ui;

import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;

public class SpriteElement extends Element {

	private final Identifier sprite;
	private final int width;
	private final int height;

	public SpriteElement(Identifier sprite, int width, int height) {
		this.sprite = sprite;
		this.width = width;
		this.height = height;
	}

	@Override
	public Vec2f getSize() {
		return new Vec2f(width, height);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		Vec2f size = getCachedSize();
		IDisplayHelper.get().blitSprite(sprite, width, height, Math.round(x), Math.round(y), Math.round(size.x), Math.round(size.y));
	}

}
