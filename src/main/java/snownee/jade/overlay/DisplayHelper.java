package snownee.jade.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.ui.Color;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.util.RgbText;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

public class DisplayHelper implements IDisplayHelper {

	public static final DisplayHelper INSTANCE = new DisplayHelper();

	public static int guiWidth() {
		return new Window(Minecraft.getInstance()).getWidth();
	}

	public static void pushScissor(int x, int y, int width, int height) {
		Minecraft mc = Minecraft.getInstance();
		int scale = new Window(mc).getScale();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x * scale, mc.height - (y + height) * scale, width * scale, height * scale);
	}

	public static void popScissor() {
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

	public static int guiHeight() {
		return new Window(Minecraft.getInstance()).getHeight();
	}

	public static DecimalFormat dfCommas = new DecimalFormat("0.##");
	public static final DecimalFormat[] dfCommasArray = new DecimalFormat[]{dfCommas, new DecimalFormat("0.#"), new DecimalFormat("0")};

	static {
		for (DecimalFormat format : dfCommasArray) {
			format.setRoundingMode(RoundingMode.DOWN);
		}
	}

	public static void fill(float minX, float minY, float maxX, float maxY, int color) {
		if (minX > maxX) {
			float i = minX;
			minX = maxX;
			maxX = i;
		}
		if (minY > maxY) {
			float j = minY;
			minY = maxY;
			maxY = j;
		}

		float a = (color >> 24 & 255) / 255.0F * OverlayRenderer.alpha;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuffer();

		boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(minX, maxY, 0.0).color(r, g, b, a).nextVertex();
		buffer.vertex(maxX, maxY, 0.0).color(r, g, b, a).nextVertex();
		buffer.vertex(maxX, minY, 0.0).color(r, g, b, a).nextVertex();
		buffer.vertex(minX, minY, 0.0).color(r, g, b, a).nextVertex();
		tesselator.end();

		if (texture)
			GlStateManager.enableTexture();
		else
			GlStateManager.disableTexture();
		if (blend)
			GlStateManager.enableBlend();
		else
			GlStateManager.disableBlend();
	}

	@Override
	public void drawItem(float x, float y, ItemStack stack, float scale, @Nullable String text) {
		if (opacity() < 0.5F || stack.isEmpty()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();

		boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);

		GlStateManager.pushMatrix();
		GlStateManager.translatef(x, y, 0);
		GlStateManager.scalef(scale, scale, scale);
		GlStateManager.enableBlend();
		GlStateManager.enableTexture();
		Lighting.turnOnGui();
		GlStateManager.enableDepthTest();
		mc.getItemRenderer().renderGuiItem(stack, 0, 0);
		mc.getItemRenderer().renderGuiItemDecorations(mc.textRenderer, stack, 0, 0, text);
		Lighting.turnOff();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();

		if (depth)
			GlStateManager.enableDepthTest();
		else
			GlStateManager.disableDepthTest();
		if (blend)
			GlStateManager.enableBlend();
		else
			GlStateManager.disableBlend();
	}

	@Override
	public void drawGradientRect(float left, float top, float width, float height, int startColor, int endColor) {
		drawGradientRect(left, top, width, height, startColor, endColor, false);
	}

	public void drawGradientRect(float left, float top, float width, float height, int startColor, int endColor, boolean horizontal) {
		if (startColor == -1 && endColor == -1) {
			return;
		}

		float a1 = (startColor >> 24 & 255) / 255.0F * opacity();
		float r1 = (startColor >> 16 & 255) / 255.0F;
		float g1 = (startColor >> 8 & 255) / 255.0F;
		float b1 = (startColor & 255) / 255.0F;
		float a2 = (endColor >> 24 & 255) / 255.0F * opacity();
		float r2 = (endColor >> 16 & 255) / 255.0F;
		float g2 = (endColor >> 8 & 255) / 255.0F;
		float b2 = (endColor & 255) / 255.0F;

		boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);

		GlStateManager.enableBlend();
		GlStateManager.disableTexture();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
		if (horizontal) {
			buffer.vertex(left + width, top, 0.0).color(r2, g2, b2, a2).nextVertex();
			buffer.vertex(left, top, 0.0).color(r1, g1, b1, a1).nextVertex();
			buffer.vertex(left, top + height, 0.0).color(r1, g1, b1, a1).nextVertex();
			buffer.vertex(left + width, top + height, 0.0).color(r2, g2, b2, a2).nextVertex();
		} else {
			buffer.vertex(left + width, top, 0.0).color(r1, g1, b1, a1).nextVertex();
			buffer.vertex(left, top, 0.0).color(r1, g1, b1, a1).nextVertex();
			buffer.vertex(left, top + height, 0.0).color(r2, g2, b2, a2).nextVertex();
			buffer.vertex(left + width, top + height, 0.0).color(r2, g2, b2, a2).nextVertex();
		}
		tesselator.end();

		GlStateManager.shadeModel(shadeModel);
		if (texture)
			GlStateManager.enableTexture();
		else
			GlStateManager.disableTexture();
		if (blend)
			GlStateManager.enableBlend();
		else
			GlStateManager.disableBlend();
	}

	@Override
	public void drawBorder(float minX, float minY, float maxX, float maxY, float width, int color, boolean corner) {
		fill(minX + width, minY, maxX - width, minY + width, color);
		fill(minX + width, maxY - width, maxX - width, maxY, color);
		if (corner) {
			fill(minX, minY, minX + width, maxY, color);
			fill(maxX - width, minY, maxX, maxY, color);
		} else {
			fill(minX, minY + width, minX + width, maxY - width, color);
			fill(maxX - width, minY + width, maxX, maxY - width, color);
		}
	}

	@Override
	public String humanReadableNumber(double number, String unit, boolean milli) {
		return humanReadableNumber(number, unit, milli, dfCommas);
	}

	@Override
	public String humanReadableNumber(double number, String unit, boolean milli, @Nullable Format formatter) {
		StringBuilder sb = new StringBuilder();
		boolean n = number < 0;
		if (n) {
			number = -number;
			sb.append('-');
		}
		if (milli && number >= 1000) {
			number /= 1000;
			milli = false;
		}
		int exp = 0;
		if (number < 1000 || formatter == null && number < 10000) {
			formatter = dfCommasArray[2];
		} else {
			exp = (int) Math.log10(number) / 3;
			if (exp > 7)
				exp = 7;
			if (exp > 0)
				number /= Math.pow(1000, exp);
			if (formatter == null) {
				if (number < 10) {
					formatter = dfCommasArray[0];
				} else if (number < 100) {
					formatter = dfCommasArray[1];
				} else {
					formatter = dfCommasArray[2];
				}
			}
		}
		if (formatter instanceof NumberFormat) {
			sb.append(((NumberFormat) formatter).format(number));
		} else {
			sb.append(formatter.format(new Object[]{number}));
		}
		if (exp == 0) {
			if (milli && number != 0) {
				sb.append('m');
			}
		} else {
			char pre = "kMGTPEZ".charAt(exp - 1);
			sb.append(pre);
		}
		sb.append(unit);
		return sb.toString();
	}

	@Override
	public void drawText(String text, float x, float y, int color) {
		drawText(new LiteralText(text), x, y, color);
	}

	@Override
	public void drawText(Text text, float x, float y, int color) {
		boolean shadow = Jade.CONFIG.get().getOverlay().getTheme().text.shadow();
		float alpha = opacity();
		if (alpha != 1) {
			color = IConfigOverlay.applyAlpha(color, alpha);
		}
		TextRenderer font = Minecraft.getInstance().textRenderer;
		//port: 1.12's TextRenderer#draw already takes an arbitrary ARGB color, the only limitation is
		//that Style can only hold the 16 Formatting colors. Walking the text tree lets us hand the
		//exact color of RgbText nodes to the renderer, nested components included.
		if (text instanceof RgbText) {
			// fast path: a single colored run is by far the most common case
			String content = text.getContent();
			if (!content.isEmpty()) {
				font.draw(text.getStyle().asString() + content, x, y, ((RgbText) text).getColor(), shadow);
			}
			return;
		}
		float curX = x;
		for (Text node : text) {
			String content = node.getContent();
			if (content.isEmpty()) {
				continue;
			}
			if (node instanceof RgbText) {
				// a styled run keeps its own color, exactly like 1.20 where the per-glyph color wins
				// over the base color (so it does not take part in the tooltip fade either)
				font.draw(node.getStyle().asString() + content, curX, y, ((RgbText) node).getColor(), shadow);
			} else {
				font.draw(node.getStyle().asString() + content, curX, y, color, shadow);
			}
			curX += font.getWidth(content);
		}
	}

	@Override
	public Text stripColor(Text text) {
		return new LiteralText(Formatting.strip(text.getFormattedString()));
	}

	@Override
	public void blitSprite(Identifier sprite, int spriteWidth, int spriteHeight, int x, int y, int width, int height) {
		float u1 = spriteWidth <= 0 ? 1 : Math.min(1F, (float) width / spriteWidth);
		float v1 = spriteHeight <= 0 ? 1 : Math.min(1F, (float) height / spriteHeight);
		drawTextureRegion(resolveSprite(sprite), x, y, width, height, 0, 0, u1, v1, opacity());
	}

	public static Identifier resolveSprite(Identifier sprite) {
		if (sprite.getPath().endsWith(".png")) {
			return sprite;
		}
		return new Identifier(sprite.getNamespace(), "textures/gui/sprites/" + sprite.getPath() + ".png");
	}

	@Override
	public void drawTexture(Identifier texture, float x, float y, float width, float height, float alpha) {
		drawTextureRegion(resolveSprite(texture), x, y, width, height, 0, 0, 1, 1, alpha);
	}

	private void drawTextureRegion(Identifier texture, float x, float y, float width, float height, float u1, float v1, float u2, float v2, float alpha) {
		Minecraft mc = Minecraft.getInstance();
		mc.getTextureManager().bind(texture);

		boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
		boolean alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);

		GlStateManager.enableBlend();
		GlStateManager.disableAlphaTest();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1, 1, 1, alpha);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex(x, y + height, 0.0).texture(u1, v2).nextVertex();
		buffer.vertex(x + width, y + height, 0.0).texture(u2, v2).nextVertex();
		buffer.vertex(x + width, y, 0.0).texture(u2, v1).nextVertex();
		buffer.vertex(x, y, 0.0).texture(u1, v1).nextVertex();
		tesselator.end();
		GlStateManager.color4f(1, 1, 1, 1);

		if (alphaTest)
			GlStateManager.enableAlphaTest();
		else
			GlStateManager.disableAlphaTest();
		if (blend)
			GlStateManager.enableBlend();
		else
			GlStateManager.disableBlend();
	}

	@Override
	public void drawGradientProgress(float left, float top, float width, float height, float progress, int progressColor1, int progressColor2) {
		Color color = Color.rgb(progressColor1);
		Color highlight = Color.hsl(color.getHue(), color.getSaturation(), Math.min(color.getLightness() + 0.2, 1), color.getOpacity());
		if (progress < 0.1F) {
			drawGradientRect(left, top, width * progress, height, progressColor1, highlight.toInt(), true);
		} else {
			float hlWidth = width * 0.1F;
			float normalWidth = width * progress - hlWidth;
			fill(left, top, left + normalWidth, top + height, progressColor1);
			drawGradientRect(left + normalWidth, top, hlWidth, height, progressColor1, highlight.toInt(), true);
		}
	}

	@Override
	public float opacity() {
		return OverlayRenderer.alpha;
	}
}
