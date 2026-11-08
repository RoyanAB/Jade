package snownee.jade.gui.config.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class JadeButton extends ButtonWidget implements JadeWidget {

	@Nullable
	private Text tooltip;
	private Integer customTextColor;

	public JadeButton(int x, int y, int width, int height, String message) {
		super(0, x, y, width, height, message);
	}

	public JadeButton textColor(int color) {
		this.customTextColor = color;
		return this;
	}

	@Override
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		render(Minecraft.getInstance(), mouseX, mouseY, tickDelta);
	}

	@Override
	public void render(Minecraft mc, int mouseX, int mouseY, float tickDelta) {
		if (!visible) {
			return;
		}
		mc.getTextureManager().bind(WIDGETS_LOCATION);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		int yImage = getYImage(this.hovered);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		drawTexture(x, y, 0, 46 + yImage * 20, width / 2, height);
		drawTexture(x + width / 2, y, 200 - width / 2, 46 + yImage * 20, width / 2, height);
		drawMessage(mc);
	}

	protected void drawMessage(Minecraft mc) {
		int color = getMessageColor();
		String text = message == null ? "" : message;
		mc.textRenderer.draw(text, x + width / 2 - mc.textRenderer.getWidth(text) / 2, y + (height - 8) / 2, color, true);
	}

	/**
	 * Mirrors 1.20's {@code Button#getMessageColor}: grayed out when inactive, highlighted on hover.
	 * A custom text color (e.g. the green save button) is constant, just like a styled component.
	 */
	protected int getMessageColor() {
		if (!active) {
			return 10526880;
		}
		if (customTextColor != null) {
			return customTextColor;
		}
		return hovered ? 16777120 : 14737632;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (button != 0 || !active || !visible) {
			return false;
		}
		boolean clicked = mouseClicked(Minecraft.getInstance(), mouseX, mouseY);
		if (clicked) {
			playClickSound(Minecraft.getInstance().getSoundManager());
		}
		return clicked;
	}

	@Override
	public boolean mouseDragged(int mouseX, int mouseY) {
		return false;
	}

	@Override
	public boolean keyPressed(char chr, int keyCode) {
		return false;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public JadeButton tooltip(@Nullable Text tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	@Override
	@Nullable
	public Text getTooltip() {
		return tooltip;
	}
}
