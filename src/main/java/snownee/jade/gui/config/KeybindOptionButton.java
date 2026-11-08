package snownee.jade.gui.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import snownee.jade.gui.config.widget.JadeButton;
import snownee.jade.gui.config.widget.JadeWidget;

public class KeybindOptionButton extends OptionButton {

	private final KeyBinding keybind;

	public KeybindOptionButton(OptionsList owner, KeyBinding keybind) {
		super(new LiteralText(I18n.translate(keybind.getName())), null);
		this.keybind = keybind;
		JadeButton button = new JadeButton(0, 0, 100, 20, GameOptions.getKeyName(keybind.getKeyCode()));
		JadeButtonWrapper wrapper = new JadeButtonWrapper(button, () -> {
			owner.selectedKey = this.keybind;
			owner.resetMappingAndUpdateButtons();
		});
		addWidget(wrapper, 0);
	}

	public void refresh(KeyBinding selectedKey) {
		JadeWidget widget = getFirstWidget();
		if (!(widget instanceof JadeButtonWrapper)) {
			return;
		}
		JadeButton button = ((JadeButtonWrapper) widget).button;
		if (selectedKey == keybind) {
			button.message = Formatting.YELLOW + "> " + Formatting.WHITE + Formatting.UNDERLINE + GameOptions.getKeyName(keybind.getKeyCode()) + Formatting.RESET + Formatting.YELLOW + " <";
		} else {
			button.message = GameOptions.getKeyName(keybind.getKeyCode());
		}
	}

	public static class JadeButtonWrapper implements JadeWidget {
		public final JadeButton button;
		private final Runnable onClick;

		public JadeButtonWrapper(JadeButton button, Runnable onClick) {
			this.button = button;
			this.onClick = onClick;
		}

		@Override
		public void setPos(int x, int y) {
			button.setPos(x, y);
		}

		@Override
		public int getX() {
			return button.getX();
		}

		@Override
		public int getY() {
			return button.getY();
		}

		@Override
		public int getWidth() {
			return button.getWidth();
		}

		@Override
		public int getHeight() {
			return button.getHeight();
		}

		@Override
		public void setWidth(int width) {
			button.setWidth(width);
		}

		@Override
		public void render(int mouseX, int mouseY, float tickDelta) {
			button.render(mouseX, mouseY, tickDelta);
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			if (button == 0 && this.button.isActive() && this.button.isVisible()) {
				this.button.playClickSound(Minecraft.getInstance().getSoundManager());
				onClick.run();
				return true;
			}
			return false;
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
			button.setActive(active);
		}

		@Override
		public boolean isActive() {
			return button.isActive();
		}

		@Override
		public void setVisible(boolean visible) {
			button.setVisible(visible);
		}

		@Override
		public boolean isVisible() {
			return button.isVisible();
		}

		@Override
		@Nullable
		public Text getTooltip() {
			return button.getTooltip();
		}
	}
}
