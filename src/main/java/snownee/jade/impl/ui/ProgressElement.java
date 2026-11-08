package snownee.jade.impl.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.*;
import snownee.jade.overlay.ProgressTracker.TrackInfo;
import snownee.jade.overlay.WailaTickHandler;

public class ProgressElement extends Element implements StyledElement {
	private final float progress;
	@Nullable
	private final Text text;
	private final ProgressStyle style;
	private final BoxStyle boxStyle;
	private TrackInfo track;
	private final boolean canDecrease;

	public ProgressElement(float progress, @Nullable Text text, ProgressStyle style, BoxStyle boxStyle, boolean canDecrease) {
		this.progress = MathHelper.clamp(progress, 0, 1);
		this.text = text;
		this.style = style;
		this.boxStyle = boxStyle;
		this.canDecrease = canDecrease;
	}

	@Override
	public Vec2f getSize() {
		int height = text == null ? 8 : 14;
		float width = 0;
		width += boxStyle.borderWidth() * 2;
		if (text != null) {
			width += Minecraft.getInstance().textRenderer.getWidth(text.getString()) + 3;
		}
		width = Math.max(20, width);
		if (getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, canDecrease, width);
			width = track.getWidth();
		}
		return new Vec2f(width, height);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		float width = style.direction().isHorizontal() && style.fitContentX() ? maxX - x : getCachedSize().x;
		float height = style.direction().isVertical() && style.fitContentY() ? maxY - y : getCachedSize().y;
		x = style.direction().isHorizontal() ? x : x + (maxX - x - width) / 2;
		y = style.direction().isVertical() ? y : y + (maxY - y - height) / 2;
		boxStyle.render(this, x, y, width, height, IDisplayHelper.get().opacity());
		float progress = this.progress;
		if (track == null && getTag() != null) {
			track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, canDecrease, width);
		}
		if (track != null) {
			progress = track.tick(Minecraft.getInstance().getTickDelta());
		}
		float b = boxStyle.borderWidth();
		style.render(x + b, y + b, width - b * 2, height - b * 2, progress, text);
	}

	@Override
	@Nullable
	public String getMessage() {
		return text == null ? null : text.getString();
	}

	@Override
	public IElement getIcon() {
		return null;
	}

	@Override
	public BoxStyle getStyle() {
		return boxStyle;
	}
}
