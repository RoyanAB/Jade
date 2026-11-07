package snownee.jade.api.ui;

import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Internals;
import snownee.jade.api.ITooltip;

public interface IElementHelper {

	static IElementHelper get() {
		return Internals.getElementHelper();
	}

	ITextElement text(Text text);

	IElement spacer(int x, int y);

	IElement item(ItemStack itemStack);

	IElement item(ItemStack itemStack, float scale);

	IElement item(ItemStack itemStack, float scale, @Nullable String text);

	IElement smallItem(ItemStack itemStack);

	IElement progress(float progress, @Nullable Text text, ProgressStyle style, BoxStyle boxStyle, boolean canDecrease);

	IElement progress(float progress);

	IElement progress(float progress, Identifier baseSprite, Identifier progressSprite, int width, int height, boolean canDecrease);

	/**
	 * Display a nested tooltip
	 */
	IBoxElement box(ITooltip tooltip, BoxStyle boxStyle);

	/**
	 * Create an empty tooltip. Used by the {@code box} method.
	 */
	ITooltip tooltip();

	ProgressStyle progressStyle();

	IElement sprite(Identifier sprite, int width, int height);
}
