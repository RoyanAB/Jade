package snownee.jade.impl.ui;

import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.*;
import snownee.jade.impl.Tooltip;

public class ElementHelper implements IElementHelper {
	public static final ElementHelper INSTANCE = new ElementHelper();
	public static final Identifier DEFAULT_PROGRESS = Identifiers.JADE("progress");
	public static final Identifier DEFAULT_PROGRESS_BASE = Identifiers.JADE("progress_base");
	public static final Vec2f SMALL_ITEM_SIZE = new Vec2f(10, 10);
	public static final Vec2f SMALL_ITEM_OFFSET = new Vec2f(0, -1);
	private Identifier uid;

	@Override
	public ITextElement text(Text text) {
		return new TextElement(text);
	}

	@Override
	public IElement item(ItemStack stack) {
		return ItemStackElement.of(stack);
	}

	@Override
	public IElement item(ItemStack stack, float scale) {
		return ItemStackElement.of(stack, scale);
	}

	@Override
	public IElement item(ItemStack stack, float scale, String text) {
		return ItemStackElement.of(stack, scale, text);
	}

	@Override
	public IElement smallItem(ItemStack stack) {
		return item(stack, 0.5F, "").size(SMALL_ITEM_SIZE).translate(SMALL_ITEM_OFFSET).message(null);
	}

	@Override
	public IElement spacer(int width, int height) {
		return new SpacerElement(new Vec2f(width, height));
	}

	@Override
	public IElement progress(float progress, @Nullable Text text, ProgressStyle style, BoxStyle boxStyle, boolean canDecrease) {
		return new ProgressElement(progress, text, style, boxStyle, canDecrease);
	}

	@Override
	public IElement progress(float progress) {
		return progress(progress, DEFAULT_PROGRESS_BASE, DEFAULT_PROGRESS, 22, 16, false);
	}

	@Override
	public IElement progress(float progress, Identifier baseSprite, Identifier progressSprite, int width, int height, boolean canDecrease) {
		ProgressStyle style = progressStyle().fitContentX(false).overlay(sprite(progressSprite, width, height));
		BoxStyle boxStyle = BoxStyle.getSprite(baseSprite, null);
		return progress(progress, null, style, boxStyle, canDecrease).size(new Vec2f(width, height));
	}

	@Override
	public IBoxElement box(ITooltip tooltip, BoxStyle boxStyle) {
		return new BoxElement((Tooltip) tooltip, boxStyle);
	}

	@Override
	public ITooltip tooltip() {
		return new Tooltip();
	}

	@Override
	public ProgressStyle progressStyle() {
		return new SimpleProgressStyle();
	}

	@Override
	public IElement sprite(Identifier sprite, int width, int height) {
		return new SpriteElement(sprite, width, height);
	}

	@Nullable
	public Identifier currentUid() {
		return uid;
	}

	public void setCurrentUid(Identifier uid) {
		this.uid = uid;
	}

}
