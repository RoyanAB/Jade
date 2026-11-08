package snownee.jade.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.gui.config.widget.JadeButton;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.util.ModIdentification;
import snownee.jade.util.SmoothChasingValue;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class HomeConfigScreen extends Screen {

	private final Random random = new Random(42);
	private final Screen parent;
	private final SmoothChasingValue titleY;
	private final SmoothChasingValue creditHover;
	private final Text credit;
	private final List<TextParticle> particles = Lists.newArrayList();
	private final List<TextParticle> pendingParticles = Lists.newArrayList();
	private int creditWidth;
	private boolean hovered;
	private float ticks;
	private byte festival;
	private float nextParticleIn;

	public HomeConfigScreen(Screen parent) {
		this.parent = parent;
		titleY = new SmoothChasingValue().start(8).target(32).withSpeed(0.1F);
		creditHover = new SmoothChasingValue();

		LocalDate now = LocalDate.now();
		int month = now.getMonthValue();
		int day = now.getDayOfMonth();
		if (month == 12 && day >= 24 && day <= 26) {
			festival = 1;
		} else if (month == 6 && day == 28) {
			festival = 2;
		} else if (month <= 2 && isLunarNewYear(now)) {
			festival = 99;
		}
		LiteralText heart = new LiteralText("❤");
		heart.setStyle(new Style().setColor(Formatting.RED));
		credit = new TranslatableText("gui.jade.by", heart);
	}

	private static boolean isLunarNewYear(LocalDate now) {
		int year = now.getYear();
		int newYearMonthAndDay;
		switch (year) {
			case 2025:
				newYearMonthAndDay = 129;
				break;
			case 2026:
				newYearMonthAndDay = 217;
				break;
			case 2027:
				newYearMonthAndDay = 206;
				break;
			case 2028:
				newYearMonthAndDay = 126;
				break;
			case 2029:
				newYearMonthAndDay = 213;
				break;
			case 2030:
				newYearMonthAndDay = 203;
				break;
			case 2031:
				newYearMonthAndDay = 123;
				break;
			case 2032:
				newYearMonthAndDay = 211;
				break;
			case 2033:
				newYearMonthAndDay = 131;
				break;
			case 2034:
				newYearMonthAndDay = 219;
				break;
			case 2035:
				newYearMonthAndDay = 208;
				break;
			case 2036:
				newYearMonthAndDay = 128;
				break;
			case 2037:
				newYearMonthAndDay = 215;
				break;
			case 2038:
				newYearMonthAndDay = 204;
				break;
			case 2039:
				newYearMonthAndDay = 124;
				break;
			case 2040:
				newYearMonthAndDay = 212;
				break;
			case 2041:
				newYearMonthAndDay = 201;
				break;
			case 2042:
				newYearMonthAndDay = 122;
				break;
			case 2043:
				newYearMonthAndDay = 210;
				break;
			default:
				newYearMonthAndDay = 0;
				break;
		}
		if (newYearMonthAndDay == 0) {
			return false;
		}
		int newYearMonth = newYearMonthAndDay / 100;
		int newYearDay = newYearMonthAndDay % 100;
		LocalDate newYearDate = LocalDate.of(year, newYearMonth, newYearDay);
		int newYearDayOfYear = newYearDate.getDayOfYear();
		int dayOfYear = now.getDayOfYear();
		return dayOfYear >= newYearDayOfYear - 1 && dayOfYear <= newYearDayOfYear + 2;
	}

	private static int color(float r, float g, float b) {
		int ri = (int) (r * 255) & 0xFF;
		int gi = (int) (g * 255) & 0xFF;
		int bi = (int) (b * 255) & 0xFF;
		return 0xFF000000 | ri << 16 | gi << 8 | bi;
	}

	@Override
	public void init() {
		particles.clear();
		buttons.clear();
		creditWidth = textRenderer.getWidth(credit.getString());
		addButton(new JadeButton(width / 2 - 105, height / 2 - 10, 100, 20, I18n.translate("gui.jade.jade_settings")));
		addButton(new JadeButton(width / 2 + 5, height / 2 - 10, 100, 20, I18n.translate("gui.jade.plugin_settings")));
		addButton(new JadeButton(width / 2 - 50, height / 2 + 20, 100, 20, I18n.translate("gui.done")));
	}

	public void close() {
		Jade.CONFIG.save();
		PluginConfig.INSTANCE.save();
		minecraft.openScreen(parent);
	}

	@Override
	protected void keyPressed(char chr, int keyCode) {
		if (keyCode == 1) {
			close();
			return;
		}
		super.keyPressed(chr, keyCode);
	}

	@Override
	protected void buttonClicked(ButtonWidget button) {
		int index = buttons.indexOf(button);
		if (index == 0) {
			minecraft.openScreen(new WailaConfigScreen(this));
		} else if (index == 1) {
			minecraft.openScreen(new PluginsConfigScreen(this));
		} else {
			close();
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		ticks += partialTicks;
		if (ticks > nextParticleIn) {
			if (festival == 3) {
				nextParticleIn = ticks + 1;
				if (pendingParticles.isEmpty()) {
					festival3populateNew();
				}
				TextParticle particle = pendingParticles.remove(0);
				particle.x = mouseX - 5;
				particle.y = mouseY;
				particles.add(particle);
			} else if (festival == 1) {
				nextParticleIn = ticks + 10 + random.nextFloat() * 10;
				int color = color(1 - random.nextFloat() * 0.6F, 1, 1);
				color |= (random.nextInt(80) + 40) << 24;
				int x = 40 + random.nextInt(width + 61);
				TextParticle particle = new TextParticle("❄", x, -20, -0.3F, 0.5f, color, 2F + random.nextFloat());
				particle.gravity = 0F;
				particles.add(particle);
			}
		}
		renderBackground();
		boolean smallUI = new Window(minecraft).getScale() < 3;
		int left = width / 2 - 105;
		int top = height / 4 - 20;
		GlStateManager.pushMatrix();
		GlStateManager.translatef(left, top, 0);

		float scale = smallUI ? 2F : 1.5F;
		GlStateManager.scalef(scale, scale, scale);
		textRenderer.draw(ModIdentification.getModName(Jade.MODID).orElse("Jade"), 0, 0, 0xFFFFFF);

		GlStateManager.scalef(0.5F, 0.5F, 0.5F);
		titleY.tick(partialTicks);
		String desc2 = I18n.translate("gui.jade.configuration.desc2");
		float scaledX, scaledY;
		if (desc2.isEmpty()) {
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translatef(left, top, 0);
			scaledX = mouseX - left;
			scaledY = mouseY - top;
		} else {
			scaledX = (mouseX - left) / scale * 2;
			scaledY = (mouseY - top) / scale * 2;
		}
		drawFancyTitle(I18n.translate("gui.jade.configuration.desc1"), Math.min(titleY.value, 20F), 20F, scaledX, scaledY);
		if (!desc2.isEmpty()) {
			drawFancyTitle(desc2, Math.min(titleY.value + 3F, 32F), 32F, scaledX, scaledY);
		}
		GlStateManager.popMatrix();
		super.render(mouseX, mouseY, partialTicks);

		int creditX = (int) (width * 0.5F - creditWidth * 0.5F);
		int creditY = (int) (height * 0.9F - 5);
		boolean hover = mouseX >= creditX && mouseX < creditX + creditWidth && mouseY >= creditY && mouseY < creditY + 10;
		if (!hovered && hover) {
			creditHover.target(1);
		} else if (!hover) {
			creditHover.target(0);
		} else if (creditHover.value > 0.5) {
			creditHover.target(0);
			List<Integer> colors = Lists.newArrayList();
			String text = "❄";
			if (festival == 2) {
				festival = 3;
			} else if (festival == 99) {
				for (int i = 0; i < 11; i++) {
					colors.add(random.nextBoolean() ? 0xA80000 : 0xC01800);
				}
				text = "✐";
			} else {
				for (int i = 0; i < 11; i++) {
					colors.add(color(1 - random.nextFloat() * 0.6F, 1, 1));
				}
			}
			for (int c : colors) {
				int ox = -creditWidth / 2 + random.nextInt(creditWidth + 1);
				float x = width * 0.5F + ox;
				float y = creditY + random.nextInt(10);
				float dx = ox * 0.08F;
				float dy = -5 - random.nextFloat() * 3;
				TextParticle particle = new TextParticle(text, x, y, dx, dy, c, 0.75F + random.nextFloat() * 0.5F);
				particles.add(particle);
				if (festival == 99) {
					particle.age = 8 + random.nextFloat() * 5;
				}
			}
		}
		creditHover.tick(partialTicks);
		creditHover.value = Math.min(0.6F, creditHover.value);
		GlStateManager.pushMatrix();
		GlStateManager.translatef(width * 0.5F, creditY, 0);
		scale = 1 + creditHover.value * 0.2F;
		GlStateManager.scalef(scale, scale, scale);
		GlStateManager.translatef(creditWidth * -0.5F, 0, 0);
		int creditColor = festival != 0 && festival != 1 ? 0x55F1E3A4 : 0x55FFFFFF;
		textRenderer.draw(credit.getFormattedString(), 0, 0, creditColor);
		GlStateManager.popMatrix();
		hovered = hover;

		for (int i = particles.size() - 1; i >= 0; i--) {
			TextParticle particle = particles.get(i);
			particle.tick(partialTicks);
			if (particle.y > height + 20) {
				particles.remove(i);
				continue;
			}
			particle.render();
		}
	}

	private void festival3populateNew() {
		List<Integer> colors = Lists.newArrayList();
		String text = random.nextBoolean() ? "UwU" : "OwO";
		switch (random.nextInt(7)) {
			case 0:
				colors.add(0xE40303);
				colors.add(0xFF8C00);
				colors.add(0xFFED00);
				colors.add(0x008026);
				colors.add(0x732982);
				colors.add(0x732982);
				break;
			case 1:
				colors.add(0x5BCEFA);
				colors.add(0xF5A9B8);
				colors.add(0xFFFFFF);
				colors.add(0xF5A9B8);
				colors.add(0x5BCEFA);
				break;
			case 2:
				colors.add(0xD60270);
				colors.add(0xD60270);
				colors.add(0x9B4F96);
				colors.add(0x0038A8);
				colors.add(0x0038A8);
				break;
			case 3:
				colors.add(0xFF218C);
				colors.add(0xFF218C);
				colors.add(0xFFD800);
				colors.add(0xFFD800);
				colors.add(0x21B1FF);
				colors.add(0x21B1FF);
				break;
			case 4:
				colors.add(0x000000);
				colors.add(0xA3A3A3);
				colors.add(0xFFFFFF);
				colors.add(0x800080);
				break;
			case 5:
				colors.add(0xFF76A4);
				colors.add(0xFFFFFF);
				colors.add(0xC011D7);
				colors.add(0x000000);
				colors.add(0x2F3CBE);
				break;
			case 6:
				colors.add(0xFCF434);
				colors.add(0xFFFFFF);
				colors.add(0x9C59D1);
				colors.add(0x2C2C2C);
				break;
		}
		int ox = -creditWidth / 2 + random.nextInt(creditWidth + 1);
		float dx = ox * 0.08F;
		float dy = -5 - random.nextFloat() * 3;
		for (int color : colors) {
			for (int i = 0; i < 5; i++) {
				pendingParticles.add(new TextParticle(text, 0, 0, dx, dy, color, 1));
			}
		}
	}

	private void drawFancyTitle(String text, float y, float expectY, float mouseX, float mouseY) {
		float distY = Math.abs(y - expectY);
		if (distY >= 9) {
			return;
		}
		int color = IWailaConfig.IConfigOverlay.applyAlpha(0xAAAAAA, 1 - distY / 10F);
		JadeFont jadeFont = textRenderer instanceof JadeFont ? (JadeFont) textRenderer : null;
		if (jadeFont != null) {
			jadeFont.jade$setGlint((ticks - y / 5F) % 90 / 45 * width, mouseX);
			jadeFont.jade$setGlintStrength(1, 1 - MathHelper.clamp(Math.abs(mouseY - y) / 20F, 0, 1));
		}
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, y, 0);
		textRenderer.draw(text, 0, 0, color);
		GlStateManager.popMatrix();
		if (jadeFont != null) {
			jadeFont.jade$setGlint(Float.NaN, Float.NaN);
		}
	}

	private class TextParticle {
		private float age;
		private String text;
		private float x;
		private float y;
		private final float motionX;
		private float motionY;
		private int color;
		private final float scale;
		private float gravity = 0.98F;

		public TextParticle(String text, float x, float y, float motionX, float motionY, int color, float scale) {
			this.text = text;
			this.x = x;
			this.y = y;
			this.motionX = motionX;
			this.motionY = motionY;
			this.color = color;
			this.scale = scale;
		}

		private void tick(float partialTicks) {
			x += motionX * partialTicks;
			y += motionY * partialTicks;
			motionY += gravity * partialTicks;
			if (festival == 99) {
				boolean greaterThanZero = age > 0;
				age -= partialTicks;
				if (greaterThanZero && age <= 0) {
					text = random.nextBoolean() ? "✴" : "✳";
					color = random.nextBoolean() ? 0xFFD427 : 0xF0C415;
				}
			} else if (festival == 1) {
				age -= partialTicks;
			}
		}

		private void render() {
			if (festival == 99 && age < -4) {
				return;
			}
			GlStateManager.pushMatrix();
			GlStateManager.translatef(x, y, 0);
			GlStateManager.scalef(scale, scale, scale);
			if (festival == 1) {
				GlStateManager.rotatef(age, 0, 0, 1);
			}
			textRenderer.draw(text, 0, 0, color);
			GlStateManager.popMatrix();
		}
	}

}
