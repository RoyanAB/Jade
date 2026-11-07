package snownee.jade.impl.ui;

import net.minecraft.util.math.Vec2f;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;

public class CompoundElement extends Element {

	protected final IElement large;
	protected final IElement small;

	public CompoundElement(IElement large, IElement small) {
		this.large = large;
		this.small = small;
	}

	@Override
	public Vec2f getSize() {
		return large.getSize();
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		Vec2f largeSize = large.getCachedSize();
		Vec2f smallSize = small.getCachedSize();
		large.render(x, y, maxX, maxY);
		small.render(x + largeSize.x - smallSize.x, y + largeSize.y - smallSize.y, maxX, maxY);
	}

}
