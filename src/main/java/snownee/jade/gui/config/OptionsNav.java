package snownee.jade.gui.config;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.ListWidget;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.SmoothChasingValue;

public class OptionsNav extends ListWidget {

	private final OptionsList options;
	private final SmoothChasingValue anchor = new SmoothChasingValue();
	private final List<OptionsList.Title> entries = Lists.newArrayList();

	public OptionsNav(OptionsList options, int width, int height, int top, int itemHeight) {
		super(Minecraft.getInstance(), width, height, top, top + height, itemHeight);
		this.options = options;
		setRenderSelectionHighlight(false);
		this.minX = 0;
		this.maxX = width;
	}

	@Override
	protected int size() {
		return entries.size();
	}

	@Override
	protected void entryClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
		if (index >= 0 && index < entries.size()) {
			options.showOnTop(entries.get(index));
		}
	}

	@Override
	protected boolean isEntrySelected(int index) {
		return index >= 0 && index < entries.size() && entries.get(index) == options.currentTitle;
	}

	@Override
	protected void renderBackground() {
		GuiElement.fill(0, minY, maxX, maxY, 0xAA0A0A0A);
	}

	@Override
	public int getRowWidth() {
		return width;
	}

	@Override
	protected int getScrollbarPosition() {
		return maxX - 8;
	}

	@Override
	protected void renderEntry(int index, int x, int y, int height, int mouseX, int mouseY, float tickDelta) {
		if (index < 0 || index >= entries.size()) {
			return;
		}
		OptionsList.Title title = entries.get(index);
		if (title == options.currentTitle) {
			anchor.target(index);
			if (!isMouseInList(mouseY)) {
				if (y < minY + 4) {
					scroll(y - (minY + 4));
				} else if (y + height > maxY - 4) {
					scroll(y + height - (maxY - 4));
				}
			}
		}
		Minecraft.getInstance().textRenderer.draw(title.getTitle().getString(), x + 10, y + (height / 2) - (Minecraft.getInstance().textRenderer.fontHeight / 2), 0xFFFFFF);
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		if (!visible) {
			return;
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		capScrolling();
		Minecraft mc = Minecraft.getInstance();
		net.minecraft.client.render.platform.GlStateManager.disableLighting();
		net.minecraft.client.render.platform.GlStateManager.disableFog();
		mc.getTextureManager().bind(GuiElement.BACKGROUND_LOCATION);
		net.minecraft.client.render.platform.GlStateManager.color4f(0.125F, 0.125F, 0.125F, 1.0F);
		net.minecraft.client.render.vertex.Tesselator tesselator = net.minecraft.client.render.vertex.Tesselator.getInstance();
		net.minecraft.client.render.vertex.BufferBuilder buffer = tesselator.getBuffer();
		buffer.begin(7, net.minecraft.client.render.vertex.DefaultVertexFormat.POSITION_TEX);
		float f = 32.0F;
		float u = maxX / f;
		float v = (maxY + (int) scrollAmount) / f;
		float u2 = u + width / f;
		float v2 = v + (maxY - minY) / f;
		buffer.vertex(minX, maxY, 0.0).texture(u, v2).nextVertex();
		buffer.vertex(maxX, maxY, 0.0).texture(u2, v2).nextVertex();
		buffer.vertex(maxX, minY, 0.0).texture(u2, v).nextVertex();
		buffer.vertex(minX, minY, 0.0).texture(u, v).nextVertex();
		tesselator.end();
		net.minecraft.client.render.platform.GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int rowLeft = minX + width / 2 - getRowWidth() / 2 + 2;
		int rowTop = minY + 4 - (int) scrollAmount;
		DisplayHelper.pushScissor(minX, minY, maxX - minX, maxY - minY);
		renderList(rowLeft, rowTop, mouseX, mouseY, tickDelta);
		anchor.tick(tickDelta);
		if (!entries.isEmpty()) {
			int top = (int) (minY + 4 - scrollAmount + anchor.value * entryHeight + headerHeight);
			int left = minX + width / 2 - getRowWidth() / 2 + 2;
			GuiElement.fill(left, top, left + 2, top + entryHeight - 4, 0xFFFFFFFF);
		}
		DisplayHelper.popScissor();
		int maxScroll = getMaxScroll();
		if (maxScroll > 0) {
			int scrollbarX = getScrollbarPosition();
			int thumbHeight = (maxY - minY) * (maxY - minY) / Math.max(1, getHeight());
			thumbHeight = net.minecraft.util.math.MathHelper.clamp(thumbHeight, 32, maxY - minY - 8);
			int thumbY = (int) scrollAmount * (maxY - minY - thumbHeight) / maxScroll + minY;
			GuiElement.fill(scrollbarX, minY, scrollbarX + 6, maxY, 0xFF000000);
			GuiElement.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFF808080);
		}
	}

	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		int index = getEntryAt(mouseX, mouseY);
		if (index == -1) {
			return false;
		}
		options.showOnTop(entries.get(index));
		return true;
	}

	public void refresh() {
		entries.clear();
		if (options.children().size() <= 1) {
			return;
		}
		for (OptionsList.Entry entry : options.children()) {
			if (entry instanceof OptionsList.Title) {
				entries.add((OptionsList.Title) entry);
			}
		}
		capScrolling();
	}
}
