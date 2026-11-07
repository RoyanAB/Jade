package snownee.jade.impl.ui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.Identifiers;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.overlay.DisplayHelper;

public class HealthElement extends Element {

	private static final int ICON_SIZE = 9;

	private final float maxHealth;
	private final float health;
	private final String text;

	public HealthElement(float maxHealth, float health) {
		if (!PluginConfig.INSTANCE.get(Identifiers.MC_ENTITY_HEALTH_SHOW_FRACTIONS)) {
			maxHealth = MathHelper.ceil(maxHealth);
			health = MathHelper.ceil(health);
		}
		this.maxHealth = maxHealth;
		this.health = health;
		text = String.format("  %s/%s", DisplayHelper.dfCommas.format(health), DisplayHelper.dfCommas.format(maxHealth));
	}

	@Override
	public Vec2f getSize() {
		if (maxHealth > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER)) {
			int width = Minecraft.getInstance().textRenderer.getWidth(text);
			return new Vec2f(ICON_SIZE + width, 10);
		}
		float maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
		float healthHearts = this.maxHealth * 0.5F;
		int heartsPerLine = (int) Math.min(maxHearts, Math.ceil(healthHearts));
		int lineCount = (int) Math.ceil(healthHearts / maxHearts);
		return new Vec2f(ICON_SIZE * heartsPerLine, 10 * lineCount);
	}

	@Override
	public void render(float x, float y, float maxX, float maxY) {
		float maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
		int maxHeartsForRender = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER);
		boolean showNumbers = maxHealth > maxHeartsForRender;
		int heartCount = showNumbers ? 1 : MathHelper.ceil(maxHealth * 0.5F);
		float health = showNumbers ? 1 : this.health * 0.5F;
		int heartsPerLine = (int) Math.min(maxHearts, Math.ceil(maxHealth));

		int xOffset = 0;
		for (int i = 1; i <= heartCount; i++) {
			drawIcon(x + xOffset, y, 16, 0);
			if (i <= MathHelper.floor(health)) {
				drawIcon(x + xOffset, y, 52, 0);
			}
			if (i > health && i < health + 1) {
				drawIcon(x + xOffset, y, 61, 0);
			}
			xOffset += ICON_SIZE;
			if (!showNumbers && i % heartsPerLine == 0) {
				y += 10;
				xOffset = 0;
			}
		}

		if (showNumbers) {
			IDisplayHelper.get().drawText(text, x + ICON_SIZE, y, IThemeHelper.get().getNormalColor());
		}
	}

	private static void drawIcon(float x, float y, int u, int v) {
		Minecraft.getInstance().getTextureManager().bind(GuiElement.ICONS_LOCATION);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuffer();
		buffer.begin(7, DefaultVertexFormat.POSITION_TEX);
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
