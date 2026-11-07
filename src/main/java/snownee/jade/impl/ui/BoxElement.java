package snownee.jade.impl.ui;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import snownee.jade.Jade;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.MessageType;
import snownee.jade.api.ui.Rect2i;
import snownee.jade.api.ui.TooltipRect;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.Tooltip;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.ProgressTracker;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ClientProxy;

public class BoxElement extends Element implements IBoxElement {
	private final Tooltip tooltip;
	private final BoxStyle style;
	private int[] padding;
	private IElement icon;
	private float boxProgress;
	private MessageType boxProgressType;
	private ProgressTracker.TrackInfo track;
	private Vec2f contentSize = Vec2f.ZERO;

	public BoxElement(Tooltip tooltip, BoxStyle style) {
		this.tooltip = tooltip;
		this.style = style;
	}

	private static int calculateMargin(int margin1, int margin2) {
		if (margin1 >= 0 && margin2 >= 0) {
			return Math.max(margin1, margin2);
		} else if (margin1 < 0 && margin2 < 0) {
			return Math.min(margin1, margin2);
		} else {
			return margin1 + margin2;
		}
	}

	@Override
	public Vec2f getSize() {
		if (tooltip.isEmpty()) {
			return Vec2f.ZERO;
		}
		float width = 0, height = 0;
		int lineCount = tooltip.lines.size();
		Tooltip.Line line = tooltip.lines.get(0);
		for (int i = 0; i < lineCount; i++) {
			Vec2f size = line.size();
			width = Math.max(width, size.x);
			height += size.y;
			if (i < lineCount - 1) {
				int marginBottom = line.marginBottom;
				line = tooltip.lines.get(i + 1);
				height += calculateMargin(marginBottom, line.marginTop);
			}
		}
		contentSize = new Vec2f(width, height);
		if (icon != null) {
			Vec2f size = icon.getCachedSize();
			width += size.x + 3;
			height = Math.max(height, size.y);
		}
		width += padding(Direction2D.LEFT) + padding(Direction2D.RIGHT);
		height += padding(Direction2D.UP) + padding(Direction2D.DOWN);
		width = Math.max(width, 0);
		height = Math.max(height, 0);

		if (icon != null && icon.getCachedSize().y > contentSize.y) {
			setPadding(Direction2D.UP, padding(Direction2D.UP) + (int) (icon.getCachedSize().y - contentSize.y) / 2);
		}

		return new Vec2f(width, height);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		if (tooltip.isEmpty()) {
			return;
		}
		float alpha = IDisplayHelper.get().opacity();
		if (Identifiers.ROOT.equals(getTag())) {
			alpha *= IWailaConfig.get().getOverlay().getAlpha();
		}
		style.render(this, x, y, maxX - x, maxY - y, alpha);

		if (boxProgressType != null) {
			float left = style.boxProgressOffset(Direction2D.LEFT);
			float width = maxX - x - left;
			float top = maxY - y - 1 + style.boxProgressOffset(Direction2D.UP) + style.borderWidth();
			float height = 1 + style.boxProgressOffset(Direction2D.DOWN);
			float progress = boxProgress;
			if (track == null && getTag() != null) {
				track = WailaTickHandler.instance().progressTracker.createInfo(getTag(), progress, false, 0);
			}
			if (track != null) {
				progress = track.tick(1);
			}
			IDisplayHelper.get().drawGradientProgress(x + left, y + top, width, height, progress, style.boxProgressColors.get(boxProgressType), style.boxProgressColors.get(boxProgressType));
		}

		float contentLeft = x + padding(Direction2D.LEFT);
		float contentTop = y + padding(Direction2D.UP);

		if (icon != null) {
			Vec2f iconSize = icon.getCachedSize();
			Vec2f offset = icon.getTranslation();
			float offsetY = offset.y;
			float min = contentTop + padding(Direction2D.DOWN) + iconSize.y;
			if (IWailaConfig.get().getOverlay().getIconMode() == IWailaConfig.IconMode.TOP && min < y + getCachedSize().y) {
				offsetY += contentTop;
			} else {
				offsetY += (getCachedSize().y - iconSize.y) / 2;
			}
			float offsetX = contentLeft + offset.x;
			icon.render(offsetX, offsetY, offsetX + iconSize.x, offsetY + iconSize.y);
			contentLeft += iconSize.x + 3;
		}

		{
			float lineTop = contentTop;
			int lineCount = tooltip.lines.size();
			Tooltip.Line line = tooltip.lines.get(0);
			for (int i = 0; i < lineCount; i++) {
				Vec2f lineSize = line.size();
				line.render(contentLeft, lineTop, maxX - padding(Direction2D.RIGHT), lineTop + lineSize.y);
				if (i < lineCount - 1) {
					int marginBottom = line.marginBottom;
					line = tooltip.lines.get(i + 1);
					lineTop += lineSize.y + calculateMargin(marginBottom, line.marginTop);
				}
			}
		}

		if (tooltip.sneakyDetails) {
			float arrowTop = (OverlayRenderer.ticks / 5) % 8 - 2;
			if (arrowTop <= 4) {
				alpha = 1 - Math.abs(arrowTop) / 2;
				if (alpha > 0.016) {
					arrowTop += getCachedSize().y - 6;
					float arrowLeft = contentLeft + (contentSize.x - Minecraft.getInstance().textRenderer.getWidth("▾") + 1) / 2f;
					int color = IConfigOverlay.applyAlpha(IThemeHelper.get().theme().text.colors().info(), alpha);
					DisplayHelper.INSTANCE.drawText("▾", arrowLeft, y + arrowTop, color);
				}
			}
		}
	}

	@Override
	@Nullable
	public String getMessage() {
		return tooltip.isEmpty() ? null : tooltip.getMessage();
	}

	@Override
	public Tooltip getTooltip() {
		return tooltip;
	}

	@Override
	public void setBoxProgress(MessageType type, float progress) {
		boxProgress = progress;
		boxProgressType = type;
	}

	@Override
	public float getBoxProgress() {
		return boxProgressType == null ? Float.NaN : boxProgress;
	}

	@Override
	public void clearBoxProgress() {
		boxProgress = 0;
		boxProgressType = null;
	}

	@Override
	@Nullable
	public IElement getIcon() {
		return icon;
	}

	@Override
	public void setIcon(@Nullable IElement icon) {
		this.icon = icon;
	}

	public void setThemeIcon(@Nullable IElement icon, Theme theme) {
		if (!IWailaConfig.get().getOverlay().shouldShowIcon()) {
			return;
		}
		if (icon != null && theme.iconSlotSprite != null) {
			if (theme.iconSlotSpriteCache == null) {
				int[] padding = new int[4];
				Arrays.fill(padding, theme.iconSlotInflation);
				theme.iconSlotSpriteCache = new BoxElement(new Tooltip(), BoxStyle.getSprite(theme.iconSlotSprite, padding));
			}
			ITooltip tooltip1 = theme.iconSlotSpriteCache.getTooltip();
			tooltip1.clear();
			tooltip1.add(icon);
			icon = theme.iconSlotSpriteCache;
		}
		setIcon(icon);
	}

	public void updateExpectedRect(TooltipRect rect) {
		IWailaConfig.IConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		Vec2f size = getCachedSize();
		float x = DisplayHelper.guiWidth() * overlay.tryFlip(overlay.getOverlayPosX());
		float y = DisplayHelper.guiHeight() * (1.0F - overlay.getOverlayPosY());
		float width = size.x;
		float height = size.y;

		if (style.hasRoundCorner()) {
			x++;
			y++;
			width += 2;
			height += 2;
		}

		rect.scale = overlay.getOverlayScale();
		float thresholdHeight = DisplayHelper.guiHeight() * overlay.getAutoScaleThreshold();
		if (size.y * rect.scale > thresholdHeight) {
			rect.scale = Math.max(rect.scale * 0.5f, thresholdHeight / size.y);
		}

		Rect2i expectedRect = rect.expectedRect;
		expectedRect.set((int) (x - width * rect.scale * overlay.tryFlip(overlay.getAnchorX())), (int) (y - height * rect.scale * overlay.getAnchorY()), (int) (width * rect.scale), (int) (height * rect.scale));

		if (PreviewOptionsScreen.isAdjustingPosition()) {
			return;
		}

		IWailaConfig.BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		if (mode == IWailaConfig.BossBarOverlapMode.PUSH_DOWN) {
			Rect2i bossBarRect = ClientProxy.getBossBarRect();
			if (bossBarRect != null) {
				int rw = bossBarRect.getWidth();
				int rh = bossBarRect.getHeight();
				int rx = bossBarRect.getX();
				int ry = bossBarRect.getY();
				rw += rx;
				rh += ry;
				width = expectedRect.getWidth();
				height = expectedRect.getHeight();
				x = expectedRect.getX();
				y = expectedRect.getY();
				width += x;
				height += y;
				if (rw > x && rh > y && width > rx && height > ry) {
					expectedRect.y = bossBarRect.getHeight();
				}
			}
		}
	}

	public void updateRect(TooltipRect rect) {
		Rect2i src = rect.rect;
		if (src.getWidth() == 0) {
			src.set(rect.expectedRect.getX(), rect.expectedRect.getY(), rect.expectedRect.getWidth(), rect.expectedRect.getHeight());
			return;
		}
		if (!Jade.CONFIG.get().getOverlay().getAnimation()) {
			src.set(rect.expectedRect.getX(), rect.expectedRect.getY(), rect.expectedRect.getWidth(), rect.expectedRect.getHeight());
			return;
		}
		float delta = Minecraft.getInstance().getTickDelta() * 2;
		src.x = chase(src.x, rect.expectedRect.x, delta);
		src.y = chase(src.y, rect.expectedRect.y, delta);
		src.width = chase(src.width, rect.expectedRect.width, delta);
		src.height = chase(src.height, rect.expectedRect.height, delta);
	}

	private static int chase(int source, int target, float delta) {
		if (source == target) {
			return source;
		}
		float diff = target - source;
		if (delta < 1) {
			diff *= delta;
		}
		if (Math.abs(diff) < 1) {
			diff = diff > 0 ? 1 : -1;
		}
		return (int) (source + diff);
	}

	@Override
	public int padding(Direction2D direction) {
		if (padding != null) {
			return padding[direction.ordinal()];
		}
		return style.padding(direction);
	}

	@Override
	public void setPadding(Direction2D direction, int value) {
		if (padding == null) {
			padding = style.padding.clone();
		}
		padding[direction.ordinal()] = value;
	}

	@Override
	public BoxStyle getStyle() {
		return style;
	}
}
