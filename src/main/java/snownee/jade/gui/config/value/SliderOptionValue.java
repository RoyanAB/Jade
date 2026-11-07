package snownee.jade.gui.config.value;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.util.math.MathHelper;
import snownee.jade.gui.config.widget.JadeButton;

public class SliderOptionValue extends OptionValue<Float> {

	private static final DecimalFormat FMT = new DecimalFormat("##.##");
	private final Slider slider;
	private final float min;
	private final float max;
	private final FloatUnaryOperator aligner;

	public SliderOptionValue(String optionName, float value, Consumer<Float> save, float min, float max, FloatUnaryOperator aligner) {
		super(optionName, save);
		this.value = value;
		this.min = min;
		this.max = max;
		this.aligner = aligner;
		slider = new Slider(this, 0, 0, 100, 20);
		addWidget(slider, 0);
	}

	@Override
	public void setValue(Float value) {
		this.value = value;
		slider.setValue(value);
	}

	public static class Slider extends JadeButton {
		private final SliderOptionValue parent;
		private float value;
		public boolean dragging;

		public Slider(SliderOptionValue parent, int x, int y, int width, int height) {
			super(x, y, width, height, "");
			this.parent = parent;
			this.value = (float) fromScaled(parent.value, parent.min, parent.max);
			updateMessage();
		}

		public static double fromScaled(float f, float min, float max) {
			return MathHelper.clamp((f - min) / (max - min), 0, 1);
		}

		public float toScaled() {
			float f = parent.aligner.apply(parent.min + (parent.max - parent.min) * value);
			String s = FMT.format(f);
			try {
				return FMT.parse(s).floatValue();
			} catch (ParseException e) {
				return f;
			}
		}

		public void setValue(float value) {
			this.value = (float) fromScaled(value, parent.min, parent.max);
			updateMessage();
			parent.value = toScaled();
			parent.save();
		}

		private void updateMessage() {
			this.message = FMT.format(toScaled());
		}

		private void updateFromMouse(int mouseX) {
			float newValue = MathHelper.clamp((float) (mouseX - (x + 4)) / (float) (width - 8), 0, 1);
			if (Math.abs(newValue - value) < 1.0E-5F) {
				return;
			}
			value = newValue;
			updateMessage();
			parent.value = toScaled();
			parent.save();
		}

		@Override
		public void render(int mouseX, int mouseY, float tickDelta) {
			if (!visible) {
				return;
			}
			if (dragging && !org.lwjgl.input.Mouse.isButtonDown(0)) {
				dragging = false;
			}
			if (dragging) {
				updateFromMouse(mouseX);
			}
			boolean hover = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bind(WIDGETS_LOCATION);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			drawTexture(x, y, 0, 46, width / 2, height);
			drawTexture(x + width / 2, y, 200 - width / 2, 46, width / 2, height);
			int handle = x + (int) (value * (width - 8));
			drawTexture(handle, y, 0, 66, 4, height);
			drawTexture(handle + 4, y, 196, 66, 4, height);
			int color = !active ? 10526880 : 16777215;
			mc.textRenderer.draw(message, x + width / 2 - mc.textRenderer.getWidth(message) / 2, y + (height - 8) / 2, color);
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			if (!active || !visible || button != 0 || mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + height) {
				return false;
			}
			dragging = true;
			updateFromMouse(mouseX);
			return true;
		}

		@Override
		public boolean mouseClicked(Minecraft mc, int mouseX, int mouseY) {
			return mouseClicked(mouseX, mouseY, 0);
		}

		@Override
		public boolean mouseDragged(int mouseX, int mouseY) {
			if (!active || !visible) {
				return false;
			}
			updateFromMouse(mouseX);
			return true;
		}

		public void stopDragging() {
			dragging = false;
		}
	}
}
