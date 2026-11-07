package snownee.jade.gui.config;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.text.Text;
import snownee.jade.gui.config.widget.JadeWidget;

public class NotUglyEditBox extends TextFieldWidget implements JadeWidget {

	private final TextRenderer textRenderer;
	private final int width;
	private final int height;
	public int paddingLeft;
	public int paddingRight;
	public int paddingTop;
	@Nullable
	public Consumer<String> responder;
	@Nullable
	private Text hint;
	private boolean bordered = false;
	private boolean editable = true;
	private int textColor = 0xE0E0E0;
	private int textColorUneditable = 0x707070;
	private long focusedTime;
	private boolean mouseOverCross;
	private boolean visible = true;

	public NotUglyEditBox(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable NotUglyEditBox previous, Text hint) {
		super(0, textRenderer, x, y, width, height);
		this.textRenderer = textRenderer;
		this.width = width;
		this.height = height;
		this.hint = hint;
		setHasBorder(false);
		setMaxLength(100);
		if (previous != null) {
			setText(previous.getText());
		}
	}

	@Override
	public void onTextChanged(int id, String text) {
		super.onTextChanged(id, text);
		if (responder != null) {
			responder.accept(text);
		}
	}

	public void setHint(Text hint) {
		this.hint = hint;
	}

	public void setBordered(boolean bordered) {
		this.bordered = bordered;
		setHasBorder(bordered);
	}

	public void setTextColor(int color) {
		this.textColor = color;
	}

	public void setTextColorUneditable(int color) {
		this.textColorUneditable = color;
	}

	@Override
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		this.editable = editable;
	}

	@Override
	public void setFocused(boolean focused) {
		boolean wasFocused = isFocused();
		super.setFocused(focused);
		if (focused && !wasFocused) {
			focusedTime = Minecraft.getTime();
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
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setWidth(int width) {
	}

	private int innerWidth() {
		return width - paddingLeft - paddingRight;
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		if (!isVisible()) {
			return;
		}
		mouseOverCross = false;
		if (bordered) {
			GuiElement.fill(x - 1, y - 1, x + width + 1, y + height + 1, -6250336);
			GuiElement.fill(x, y, x + width, y + height, -16777216);
		} else {
			GuiElement.fill(x, y, x + width, y + height, 0xC0000000);
		}
		String text = getText();
		int color = editable ? textColor : textColorUneditable;
		int cursor = Math.min(getCursor(), text.length());
		int selectionEnd = Math.min(getSelectionEnd(), text.length());
		String shown = textRenderer.trim(text, innerWidth());
		int textX = x + paddingLeft;
		int textY = y + paddingTop;
		if (!shown.isEmpty()) {
			textRenderer.draw(shown, textX, textY, color);
		}
		boolean focused = isFocused();
		boolean blink = focused && (Minecraft.getTime() - focusedTime) / 300L % 2L == 0L;
		if (!shown.isEmpty() || focused) {
			int cursorX = textX + textRenderer.getWidth(text.substring(0, Math.min(cursor, shown.length())));
			if (selectionEnd != cursor) {
				int start = Math.min(cursor, selectionEnd);
				int end = Math.max(cursor, selectionEnd);
				int x1 = textX + textRenderer.getWidth(text.substring(0, start));
				int x2 = textX + textRenderer.getWidth(text.substring(0, end));
				GuiElement.fill(x1, textY - 1, x2, textY + textRenderer.fontHeight, 0x800000FF);
			}
			if (blink) {
				GuiElement.fill(cursorX, textY - 1, cursorX + 1, textY + textRenderer.fontHeight, 0xFFD0D0D0);
			}
		}
		if (hint != null && text.isEmpty() && !focused) {
			textRenderer.draw(hint.getString(), textX, textY, 0x808080);
		}
		if (editable && !text.isEmpty()) {
			mouseOverCross = mouseX >= x + width - 12 && mouseX < x + width - 2 && mouseY >= y + paddingTop && mouseY < y + paddingTop + 10;
			int crossColor = mouseOverCross ? textColor : textColorUneditable;
			textRenderer.draw("×", x + width - 10, y + paddingTop + 1, crossColor);
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (!isVisible() || !editable || button != 0) {
			return false;
		}
		if (mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
			return false;
		}
		if (mouseOverCross) {
			setText("");
			setFocused(true);
			return true;
		}
		setFocused(true);
		int relative = mouseX - x - paddingLeft;
		String text = getText();
		String shown = textRenderer.trim(text, innerWidth());
		int cursor = 0;
		for (int i = 1; i <= shown.length(); i++) {
			if (textRenderer.getWidth(text.substring(0, i)) > relative) {
				break;
			}
			cursor = i;
		}
		setCursor(cursor);
		setSelectionEnd(cursor);
		return true;
	}

	@Override
	public boolean keyPressed(char chr, int keyCode) {
		if (!isFocused()) {
			return false;
		}
		boolean handled = super.keyPressed(chr, keyCode);
		if (handled) {
			focusedTime = Minecraft.getTime();
		}
		return handled;
	}

	@Override
	public boolean mouseDragged(int mouseX, int mouseY) {
		return false;
	}

	@Override
	public void setActive(boolean active) {
		setEditable(active);
	}

	@Override
	public boolean isActive() {
		return editable;
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
