package snownee.jade.gui.config.widget;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Text;

public interface JadeWidget {

	void setPos(int x, int y);

	int getX();

	int getY();

	int getWidth();

	int getHeight();

	void setWidth(int width);

	void render(int mouseX, int mouseY, float tickDelta);

	boolean mouseClicked(int mouseX, int mouseY, int button);

	boolean mouseDragged(int mouseX, int mouseY);

	boolean keyPressed(char chr, int keyCode);

	void setActive(boolean active);

	boolean isActive();

	void setVisible(boolean visible);

	boolean isVisible();

	@Nullable
	default Text getTooltip() {
		return null;
	}
}
