package snownee.jade.impl.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import snownee.jade.api.Identifiers;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.overlay.DisplayHelper;

public class ArmorElement extends Element {

	private static final int ICON_SIZE = 9;

	private final float armor;

	public ArmorElement(float armor) {
		if (!PluginConfig.INSTANCE.get(Identifiers.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
			armor = MathHelper.ceil(armor);
		}
		this.armor = armor;
	}

	@Override
	public Vec2f getSize() {
		if (armor > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_ARMOR_MAX_FOR_RENDER)) {
			String text = "  " + DisplayHelper.dfCommas.format(armor);
			int width = Minecraft.getInstance().textRenderer.getWidth(text);
			return new Vec2f(ICON_SIZE + width, 10);
		}
		int maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
		int lineCount = (int) Math.ceil(armor / maxHearts * 0.5F);
		return new Vec2f(ICON_SIZE * maxHearts, 10 * lineCount);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		if (armor > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_ARMOR_MAX_FOR_RENDER)) {
			drawIcon(x, y, 34, 9);
			String text = "  " + DisplayHelper.dfCommas.format(armor);
			IDisplayHelper.get().drawText(text, x + ICON_SIZE, y, IThemeHelper.get().getNormalColor());
		} else {
			float armor = this.armor * 0.5F;
			int maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
			int lineCount = (int) Math.ceil(armor / maxHearts);
			int armorCount = lineCount * maxHearts;
			int xOffset = 0;
			for (int i = 1; i <= armorCount; i++) {
				if (i <= MathHelper.floor(armor)) {
					drawIcon(x + xOffset, y, 34, 9);
				}
				if (i > armor && i < armor + 1) {
					drawIcon(x + xOffset, y, 25, 9);
				}
				if (i >= armor + 1) {
					drawIcon(x + xOffset, y, 16, 9);
				}
				xOffset += ICON_SIZE;
				if (i % maxHearts == 0) {
					y += 10;
					xOffset = 0;
				}
			}
		}
	}

	private static void drawIcon(float x, float y, int u, int v) {
		Minecraft.getInstance().getTextureManager().bind(GuiElement.ICONS_LOCATION);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX);
		float f = 1 / 256F;
		buffer.vertex(x, y + ICON_SIZE, 0).texture(u * f, (v + ICON_SIZE) * f).nextVertex();
		buffer.vertex(x + ICON_SIZE, y + ICON_SIZE, 0).texture((u + ICON_SIZE) * f, (v + ICON_SIZE) * f).nextVertex();
		buffer.vertex(x + ICON_SIZE, y, 0).texture((u + ICON_SIZE) * f, v * f).nextVertex();
		buffer.vertex(x, y, 0).texture(u * f, v * f).nextVertex();
		tesselator.end();
	}

	@Override
	public @Nullable String getMessage() {
		return null;
	}
}
