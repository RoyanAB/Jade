package snownee.jade.gui;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.Rect2i;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.widget.CycleButtonWidget;
import snownee.jade.gui.config.widget.JadeButton;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.util.ClientProxy;

public abstract class PreviewOptionsScreen extends BaseOptionsScreen {

	public boolean adjustingPosition;
	private boolean adjustDragging;
	private double dragOffsetX;
	private double dragOffsetY;
	private CycleButtonWidget<Boolean> previewButton;

	public PreviewOptionsScreen(Screen parent, Text title) {
		super(parent, title);
	}

	public static boolean isAdjustingPosition() {
		Minecraft mc = Minecraft.getInstance();
		return mc.screen instanceof PreviewOptionsScreen && ((PreviewOptionsScreen) mc.screen).adjustingPosition;
	}

	private static float calculateAnchor(float center, float size) {
		float anchor = center / size;
		if (anchor < 0.25F) {
			return 0;
		} else if (anchor < 0.75F) {
			return 0.5F;
		} else {
			return 1;
		}
	}

	private static float maybeSnap(float value) {
		if (!Screen.isControlDown() && value > 0.475f && value < 0.525f) {
			return 0.5f;
		}
		return value;
	}

	@Override
	public void init() {
		Objects.requireNonNull(minecraft);
		super.init();
		previewButton = null;
		if (minecraft.world != null) {
			previewButton = new CycleButtonWidget<>(java.util.Arrays.asList(Boolean.TRUE, Boolean.FALSE), v -> v ? OptionsList.OPTION_ON : OptionsList.OPTION_OFF, Jade.CONFIG.get().getGeneral().previewOverlay, v -> {
				Jade.CONFIG.get().getGeneral().previewOverlay = v;
				if (saver != null) {
					saver.run();
				}
			}, new LiteralText(I18n.translate("gui.jade.preview")), false);
			previewButton.setPos(10, saveButton.getY());
			previewButton.setWidth(85);
			addButton(previewButton);
		}
	}

	public boolean forcePreviewOverlay() {
		Objects.requireNonNull(minecraft);
		if (adjustingPosition) {
			return true;
		}
		if (options == null || !options.isWidgetInteracting()) {
			return false;
		}
		OptionsList.Entry entry = options.getSelected();
		if (entry == null || entry.getFirstWidget() == null) {
			return false;
		}
		return options.forcePreview.contains(entry);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		if (adjustingPosition) {
			GuiElement.fill(0, 0, width, height, 0x80AAAAAA);
			drawCenteredString(textRenderer, I18n.translate("config.jade.overlay_pos.exit"), width / 2, height / 2 - 7, 0xFFFFFF);
			IWailaConfig.IConfigOverlay config = IWailaConfig.get().getOverlay();
			Rect2i rect = OverlayRenderer.rect.expectedRect;
			if (IWailaConfig.get().getGeneral().isDebug()) {
				int anchorX = (int) (rect.getX() + rect.getWidth() * config.getAnchorX());
				int anchorY = (int) (rect.getY() + rect.getHeight() * config.getAnchorY());
				GuiElement.fill(anchorX - 2, anchorY - 2, anchorX + 1, anchorY + 1, 0xFFFF0000);
			}
			if (config.getOverlayPosX() == 0.5f) {
				GuiElement.fill(width / 2, rect.getY() - 5, width / 2 + 1, rect.getY() + rect.getHeight() + 4, 0xFF0000FF);
			}
			if (config.getOverlayPosY() == 0.5f) {
				GuiElement.fill(rect.getX() - 5, height / 2, rect.getX() + rect.getWidth() + 4, height / 2 + 1, 0xFF0000FF);
			}
		}
		ClientProxy.onRenderTick();
		if (!adjustingPosition) {
			renderHoverTooltips(mouseX, mouseY);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		if (adjustingPosition) {
			Objects.requireNonNull(minecraft);
			Rect2i rect = OverlayRenderer.rect.expectedRect;
			if (rect.contains(mouseX, mouseY)) {
				adjustDragging = true;
				float centerX = rect.getX() + rect.getWidth() / 2F;
				float centerY = rect.getY() + rect.getHeight() / 2F;
				dragOffsetX = mouseX - centerX;
				dragOffsetY = mouseY - centerY;
			} else {
				adjustingPosition = false;
				adjustDragging = false;
				minecraft.getSoundManager().play(net.minecraft.client.sound.instance.SimpleSoundInstance.of(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
			return;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int button) {
		if (adjustingPosition) {
			adjustDragging = false;
			return;
		}
		super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	protected void keyPressed(char chr, int keyCode) {
		if (adjustingPosition) {
			if (keyCode == 1) {
				adjustingPosition = false;
				adjustDragging = false;
				minecraft.getSoundManager().play(net.minecraft.client.sound.instance.SimpleSoundInstance.of(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
			return;
		}
		super.keyPressed(chr, keyCode);
	}

	@Override
	protected void mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (adjustingPosition && adjustDragging) {
			float centerX = mouseX - (float) dragOffsetX;
			float centerY = mouseY - (float) dragOffsetY;
			float anchorX = calculateAnchor(centerX, width);
			float anchorY = calculateAnchor(centerY, height);
			Rect2i rect = OverlayRenderer.rect.expectedRect;
			float posX = (centerX + rect.getWidth() * (anchorX - 0.5F)) / width;
			float posY = 1 - (centerY + rect.getHeight() * (anchorY - 0.5F)) / height;
			IWailaConfig.IConfigOverlay config = IWailaConfig.get().getOverlay();
			config.setOverlayPosX(config.tryFlip(maybeSnap(posX)));
			config.setOverlayPosY(maybeSnap(posY));
			config.setAnchorX(config.tryFlip(anchorX));
			config.setAnchorY(anchorY);
			return;
		}
		super.mouseDragged(mouseX, mouseY, button, time);
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		if (button == previewButton) {
			return;
		}
		super.buttonClicked(button);
	}
}
