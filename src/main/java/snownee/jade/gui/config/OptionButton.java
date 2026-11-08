package snownee.jade.gui.config;

import net.minecraft.text.Text;
import snownee.jade.gui.config.widget.JadeWidget;

public class OptionButton extends OptionsList.Entry {

	protected final Text title;

	public OptionButton(String titleKey, JadeWidget button) {
		this(makeTitle(titleKey), button);
	}

	public OptionButton(Text title, JadeWidget button) {
		this.title = title;
		addMessage(title.getString());
		if (button != null) {
			if (button instanceof snownee.jade.gui.config.widget.JadeButton) {
				String buttonMessage = ((snownee.jade.gui.config.widget.JadeButton) button).message;
				if (buttonMessage != null && !buttonMessage.isEmpty()) {
					addMessage(buttonMessage);
				}
			}
			addWidget(button, 0);
		}
	}

	@Override
	public void render(int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, float deltaTime) {
		client.textRenderer.draw(title.getFormattedString(), rowLeft + 10, rowTop + (height / 2) - (client.textRenderer.fontHeight / 2), 16777215, true);
		super.render(index, rowTop, rowLeft, width, height, mouseX, mouseY, deltaTime);
	}

}
