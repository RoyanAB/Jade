package snownee.jade.impl.ui;

import net.minecraft.util.math.Vec2f;
import snownee.jade.api.ui.Element;

public class SpacerElement extends Element {

	private final Vec2f dimension;

	public SpacerElement(Vec2f dimension) {
		this.dimension = dimension;
	}

	@Override
	public Vec2f getSize() {
		return dimension;
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
	}
}
