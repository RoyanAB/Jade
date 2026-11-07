package snownee.jade.gui.config;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.util.math.MathHelper;
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
	private int displayPos;
	private boolean visible = true;

	public NotUglyEditBox(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable NotUglyEditBox previous, Text hint) {
		super(0, textRenderer, x, y, width, height);
		this.textRenderer = textRenderer;
		this.width = width;
		this.height = height;
		this.hint = hint;
		setHasBorder(false);
		setMaxLength(32);
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
		int length = text.length();
		int cursor = MathHelper.clamp(getCursor(), 0, length);
		int selectionEnd = MathHelper.clamp(getSelectionEnd(), 0, length);
		int inner = innerWidth();
		if (displayPos > length) {
			displayPos = length;
		}
		if (cursor < displayPos) {
			displayPos = cursor;
		}
		while (displayPos > 0 && textRenderer.getWidth(text.substring(displayPos - 1)) <= inner) {
			displayPos--;
		}
		while (displayPos < cursor && textRenderer.getWidth(text.substring(displayPos, cursor)) > inner) {
			displayPos++;
		}
		String shown = textRenderer.trim(text.substring(displayPos), inner);
		int shownEnd = displayPos + shown.length();
		while (cursor > shownEnd && displayPos < cursor) {
			displayPos++;
			shown = textRenderer.trim(text.substring(displayPos), inner);
			shownEnd = displayPos + shown.length();
		}
		int textX = x + paddingLeft;
		int textY = y + paddingTop;
		if (!shown.isEmpty()) {
			textRenderer.draw(shown, textX, textY, color);
		}
		boolean focused = isFocused();
		int cursorIndex = MathHelper.clamp(cursor, displayPos, shownEnd);
		int cursorX = textX + textRenderer.getWidth(shown.substring(0, Math.max(0, Math.min(cursorIndex - displayPos, shown.length()))));
		if (selectionEnd != cursor) {
			int start = Math.max(Math.min(cursor, selectionEnd), displayPos);
			int end = Math.min(Math.max(cursor, selectionEnd), shownEnd);
			if (start < end) {
				int x1 = textX + textRenderer.getWidth(shown.substring(0, start - displayPos));
				int x2 = textX + textRenderer.getWidth(shown.substring(0, end - displayPos));
				GuiElement.fill(x1, textY - 1, x2, textY + textRenderer.fontHeight, 0xFF0000FF);
			}
		}
		if (focused && (Minecraft.getTime() - focusedTime) / 300L % 2L == 0L) {
			GuiElement.fill(cursorX, textY - 1, cursorX + 1, textY + textRenderer.fontHeight, 0xFFD0D0D0);
		}
		if (hint != null && text.isEmpty() && !focused) {
			textRenderer.draw(hint.getString(), textX, textY, 0x808080);
		}
		if (editable && !text.isEmpty()) {
			mouseOverCross = mouseX >= x + width - 14 && mouseX < x + width - 2 && mouseY >= y + paddingTop && mouseY < y + paddingTop + 10;
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
			Minecraft.getInstance().getSoundManager().play(net.minecraft.client.sound.instance.SimpleSoundInstance.of(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
			return true;
		}
		setFocused(true);
		int relative = mouseX - x - paddingLeft;
		String text = getText();
		String shown = textRenderer.trim(text.substring(displayPos), innerWidth());
		int cursor = 0;
		for (int i = 1; i <= shown.length(); i++) {
			if (textRenderer.getWidth(shown.substring(0, i)) > relative) {
				break;
			}
			cursor = i;
		}
		setCursor(displayPos + cursor);
		setSelectionEnd(displayPos + cursor);
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
