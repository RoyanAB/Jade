package snownee.jade.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.lwjgl.input.Mouse;
import snownee.jade.gui.config.NotUglyEditBox;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.OptionsNav;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.gui.config.widget.JadeButton;
import snownee.jade.gui.config.widget.JadeWidget;

import java.util.Objects;

public abstract class BaseOptionsScreen extends Screen {

	protected final Screen parent;
	public JadeButton saveButton;
	protected Runnable saver;
	protected Runnable canceller;
	protected OptionsList options;
	protected OptionsNav optionsNav;
	private NotUglyEditBox searchBox;
	private JadeButton cancelButton;
	protected final Text titleText;

	public BaseOptionsScreen(Screen parent, Text title) {
		this.parent = parent;
		this.titleText = title;
	}

	@Override
	public void init() {
		Objects.requireNonNull(minecraft);
		super.init();
		buttons.clear();
		double scroll = options == null ? 0 : options.getScrollAmountF();
		if (options != null) {
			options.removed();
		}
		options = createOptions();
		options.setX(120);
		optionsNav = new OptionsNav(options, 120, height - 32, 18, 18);
		searchBox = new NotUglyEditBox(textRenderer, 0, 0, 120, 18, searchBox, new TranslatableText("gui.jade.search"));
		searchBox.setBordered(false);
		searchBox.setHint(new TranslatableText("gui.jade.search.hint"));
		searchBox.responder = s -> {
			options.updateSearch(s);
			optionsNav.refresh();
		};
		searchBox.paddingLeft = 12;
		searchBox.paddingTop = 6;
		searchBox.paddingRight = 18;
		optionsNav.refresh();
		searchBox.responder.accept(searchBox.getText());
		options.setScrollAmount(scroll);

		saveButton = new JadeButton(width - 100, height - 25, 90, 20, I18n.translate("gui.jade.save_and_quit")).textColor(0xB9F6CA);
		addButton(saveButton);
		if (canceller != null) {
			cancelButton = new JadeButton(saveButton.getX() - 95, height - 25, 90, 20, I18n.translate("gui.cancel"));
			addButton(cancelButton);
		}

		options.updateSaveState();
	}

	public abstract OptionsList createOptions();

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		optionsNav.render(mouseX, mouseY, partialTicks);
		searchBox.render(mouseX, mouseY, partialTicks);
		options.render(mouseX, mouseY, partialTicks);
		super.render(mouseX, mouseY, partialTicks);
	}

	protected void renderHoverTooltips(int mouseX, int mouseY) {
		if (options == null) {
			return;
		}
		OptionsList.Entry entry = null;
		if (options.isMouseOver(mouseX, mouseY)) {
			int index = options.getEntryAt(mouseX, mouseY);
			if (index != -1) {
				entry = options.getEntry(index);
			}
		}
		if (entry == null) {
			return;
		}
		String description = entry.getDescription();
		if (description != null && !description.isEmpty()) {
			int valueX = entry.getTextX(options.getRowWidth());
			if (mouseX >= valueX && mouseX < valueX + entry.getTextWidth()) {
				renderDescriptionTooltip(entry, description, mouseX, mouseY);
			}
		}
		if (entry instanceof OptionValue && ((OptionValue<?>) entry).serverFeature) {
			int x = entry.getTextX(options.getRowWidth()) + entry.getTextWidth() + 1;
			int rowTop = options.getRowTop(options.children().indexOf(entry)) + 7;
			if (mouseX >= x && mouseX < x + 4 && mouseY >= rowTop && mouseY < rowTop + 4) {
				renderTooltip(I18n.translate("gui.jade.server_feature"), mouseX, mouseY);
			}
		}
		JadeWidget hovered = entry.getHoveredWidget(mouseX, mouseY);
		if (hovered != null && hovered.getTooltip() != null) {
			renderTooltip(hovered.getTooltip().getString(), mouseX, mouseY);
		}
	}

	private void renderDescriptionTooltip(OptionsList.Entry entry, String description, int mouseX, int mouseY) {
		java.util.List<String> lines = java.util.Arrays.asList(description.split("\n"));
		int tooltipWidth = 0;
		for (String line : lines) {
			tooltipWidth = Math.max(tooltipWidth, textRenderer.getWidth(line));
		}
		int tooltipHeight = 8;
		if (lines.size() > 1) {
			tooltipHeight += 2 + (lines.size() - 1) * 10;
		}
		int index = options.children().indexOf(entry);
		int x = entry.getTextX(options.getRowWidth());
		int y;
		if (index == -1) {
			x = mouseX + 3;
			y = mouseY + 3;
		} else {
			y = options.getRowBottom(index) + 1;
			if (y + tooltipHeight > height) {
				y = options.getRowTop(index) - tooltipHeight - 1;
			}
			int rowLeft = options.getRowLeft();
			if (x + tooltipWidth > width) {
				x = Math.max(rowLeft + options.getRowWidth() - tooltipWidth, 4);
			}
		}
		drawTooltipAt(lines, x, y, tooltipWidth, tooltipHeight);
	}

	private void drawTooltipAt(java.util.List<String> lines, int x, int y, int tooltipWidth, int tooltipHeight) {
		GlStateManager.disableRescaleNormal();
		Lighting.turnOff();
		GlStateManager.disableLighting();
		GlStateManager.disableDepthTest();
		int border1 = -267386864;
		int border2 = 1347420415;
		int border3 = 1344798847;
		this.drawOffset = 300.0F;
		this.itemRenderer.zOffset = 300.0F;
		this.fillGradient(x - 3, y - 4, x + tooltipWidth + 3, y - 3, border1, border1);
		this.fillGradient(x - 3, y + tooltipHeight + 3, x + tooltipWidth + 3, y + tooltipHeight + 4, border1, border1);
		this.fillGradient(x - 3, y - 3, x + tooltipWidth + 3, y + tooltipHeight + 3, border1, border1);
		this.fillGradient(x - 4, y - 3, x - 3, y + tooltipHeight + 3, border1, border1);
		this.fillGradient(x + tooltipWidth + 3, y - 3, x + tooltipWidth + 4, y + tooltipHeight + 3, border1, border1);
		this.fillGradient(x - 3, y - 3 + 1, x - 3 + 1, y + tooltipHeight + 3 - 1, border2, border3);
		this.fillGradient(x + tooltipWidth + 2, y - 3 + 1, x + tooltipWidth + 3, y + tooltipHeight + 3 - 1, border2, border3);
		this.fillGradient(x - 3, y - 3, x + tooltipWidth + 3, y - 3 + 1, border2, border2);
		this.fillGradient(x - 3, y + tooltipHeight + 2, x + tooltipWidth + 3, y + tooltipHeight + 3, border3, border3);
		int lineY = y;
		for (int i = 0; i < lines.size(); i++) {
			this.textRenderer.drawWithShadow(lines.get(i), x, lineY, -1);
			if (i == 0) {
				lineY += 2;
			}
			lineY += 10;
		}
		this.drawOffset = 0.0F;
		this.itemRenderer.zOffset = 0.0F;
		GlStateManager.enableLighting();
		GlStateManager.enableDepthTest();
		Lighting.turnOn();
		GlStateManager.enableRescaleNormal();
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		boolean adjusting = this instanceof PreviewOptionsScreen && ((PreviewOptionsScreen) this).adjustingPosition;
		int wheel = Mouse.getEventDWheel();
		if (adjusting) {
			return;
		}
		if (wheel != 0) {
			int mouseX = Mouse.getX() * width / minecraft.width;
			int mouseY = height - Mouse.getY() * height / minecraft.height - 1;
			if (mouseX < 120 && mouseY >= 18 && mouseY < height - 14) {
				optionsNav.scroll(wheel > 0 ? -optionsNav.getEntryHeight() / 2 : optionsNav.getEntryHeight() / 2);
			} else {
				options.scrollSmooth(wheel);
			}
		} else if (!options.isWidgetInteracting()) {
			options.handleMouse();
			optionsNav.handleMouse();
			if (Mouse.isButtonDown(0) && options.isScrollingByMouse()) {
				options.syncScrollTarget();
			}
		} else {
			optionsNav.handleMouse();
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if (options != null) {
			options.clearTextFocus();
		}
		if (searchBox != null && searchBox.mouseClicked(mouseX, mouseY, button)) {
			return;
		}
		searchBox.setFocused(false);
		if (mouseY >= 0 && mouseY < height - 32 && mouseX >= 120 && options.mouseClicked(mouseX, mouseY, button)) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		if (options != null) {
			options.mouseReleased(mouseX, mouseY, button);
		}
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (options != null && options.mouseDragged(mouseX, mouseY, button, time)) {
			return;
		}
		super.mouseDragged(mouseX, mouseY, button, time);
	}

	@Override
	protected void keyPressed(char chr, int keyCode) {
		if (searchBox != null && searchBox.isFocused() && searchBox.keyPressed(chr, keyCode)) {
			return;
		}
		if (options != null && options.keyPressed(chr, keyCode)) {
			return;
		}
		if (keyCode == 1) {
			close();
			return;
		}
		super.keyPressed(chr, keyCode);
	}

	public void close() {
		if (canceller != null) {
			canceller.run();
		}
		Objects.requireNonNull(minecraft).openScreen(parent);
	}

	@Override
	public void removed() {
		if (options != null) {
			options.removed();
		}
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		if (button == saveButton) {
			options.save();
			if (saver != null) {
				saver.run();
			}
			minecraft.openScreen(parent);
		} else if (button == cancelButton) {
			close();
		}
	}

}
