package snownee.jade.api.ui;

import java.text.Format;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import snownee.jade.Internals;

public interface IDisplayHelper {

	static IDisplayHelper get() {
		return Internals.getDisplayHelper();
	}

	void drawItem(float x, float y, ItemStack stack, float scale, @Nullable String text);

	void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor);

	void drawBorder(float minX, float minY, float maxX, float maxY, float width, int color, boolean corner);

	String humanReadableNumber(double number, String unit, boolean milli);

	String humanReadableNumber(double number, String unit, boolean milli, @Nullable Format formatter);

	void drawText(String text, float x, float y, int color);

	void drawText(Text text, float x, float y, int color);

	Text stripColor(Text text);

	void blitSprite(Identifier sprite, int x, int y, int width, int height);

	void drawTexture(Identifier texture, float x, float y, float width, float height, float alpha);

	void drawGradientProgress(float x, float y, float w, float h, float progress, int color1, int color2);

	float opacity();
}
