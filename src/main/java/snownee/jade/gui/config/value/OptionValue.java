package snownee.jade.gui.config.value;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.widget.JadeWidget;

import java.util.function.Consumer;

public abstract class OptionValue<T> extends OptionsList.Entry {

	private static final Text SERVER_FEATURE = new LiteralText("*").setStyle(new net.minecraft.text.Style().setColor(Formatting.GRAY));
	protected final Consumer<T> setter;
	private final Text title;
	public boolean serverFeature;
	protected T value;
	protected int indent;
	private int x;

	public OptionValue(String optionName, Consumer<T> setter) {
		this.title = makeTitle(optionName);
		this.setter = setter;
		addMessage(title.getString());
		addMessageKey(optionName);
		String key = makeKey(optionName + "_desc");
		if (I18n.hasTranslation(key)) {
			appendDescription(I18n.translate(key));
		}
	}

	@Override
	public void render(int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, float deltaTime) {
		JadeWidget widget = getFirstWidget();
		boolean active = widget == null || widget.isActive();
		int left = rowLeft + indent + 10;
		int top = rowTop + (height / 2) - (client.textRenderer.fontHeight / 2);
		String titleText = title.getString();
		if (!active) {
			titleText = Formatting.STRIKETHROUGH.toString() + Formatting.GRAY + titleText;
		}
		client.textRenderer.draw(titleText, left, top, 16777215);
		if (serverFeature) {
			client.textRenderer.draw(SERVER_FEATURE.getFormattedString(), left + getTextWidth() + 1, top, 16777215);
		}
		super.render(index, rowTop, rowLeft, width, height, mouseX, mouseY, deltaTime);
		this.x = rowLeft;
	}

	public void save() {
		setter.accept(value);
	}

	public Text getTitle() {
		return title;
	}

	public void appendDescription(String description) {
		if (this.description == null) {
			this.description = description;
		} else {
			this.description += '\n' + description;
		}
		addMessage(description);
	}

	public int getX() {
		return x;
	}

	@Override
	public int getTextX(int width) {
		return getX() + indent + 10;
	}

	@Override
	public int getTextWidth() {
		return client.textRenderer.getWidth(getTitle().getString());
	}

	public boolean isValidValue() {
		return true;
	}

	@Override
	public OptionsList.Entry parent(OptionsList.Entry parent) {
		super.parent(parent);
		if (parent instanceof OptionValue) {
			indent = ((OptionValue<?>) parent).indent + 12;
		}
		return this;
	}

	public abstract void setValue(T value);

}
