package snownee.jade.gui.config.widget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.TextRenderer;

import java.util.function.Consumer;

public class JadeTextField extends TextFieldWidget implements JadeWidget {

	private final int w;
	private final int h;
	private boolean active = true;
	private boolean visible = true;
	public Consumer<String> responder;
	public int paddingLeft;
	public int paddingTop;

	public JadeTextField(TextRenderer textRenderer, int x, int y, int width, int height) {
		super(0, textRenderer, x, y, width, height);
		this.w = width;
		this.h = height;
	}

	@Override
	public void onTextChanged(int id, String text) {
		super.onTextChanged(id, text);
		if (responder != null) {
			responder.accept(text);
		}
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
		return w;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public void setWidth(int width) {
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		render();
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (!active) {
			return false;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(int mouseX, int mouseY) {
		return false;
	}

	@Override
	public boolean keyPressed(char chr, int keyCode) {
		return super.keyPressed(chr, keyCode);
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;
		setEditable(active);
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
}
