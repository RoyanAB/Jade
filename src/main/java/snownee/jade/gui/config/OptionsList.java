package snownee.jade.gui.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.gui.config.value.CycleOptionValue;
import snownee.jade.gui.config.value.InputOptionValue;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.gui.config.value.SliderOptionValue;
import snownee.jade.gui.config.widget.JadeWidget;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.ClientProxy;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class OptionsList extends ListWidget {

	public static final Text OPTION_ON = new LiteralText("ON").setStyle(new Style().setColor(Formatting.GREEN));
	public static final Text OPTION_OFF = new LiteralText("OFF").setStyle(new Style().setColor(Formatting.RED));
	public final Set<Entry> forcePreview = Sets.newIdentityHashSet();
	protected final List<Entry> entries = Lists.newArrayList();
	private final List<Entry> visibleEntries = Lists.newArrayList();
	private final Runnable diskWriter;
	public Title currentTitle;
	public net.minecraft.client.options.KeyBinding selectedKey;
	private BaseOptionsScreen owner;
	private float targetScroll;
	private Entry defaultParent;
	private int lastActiveIndex;
	private int selectedIndex = -1;
	@Nullable
	private SliderOptionValue.Slider draggingSlider;
	private boolean widgetInteracting;

	public OptionsList(BaseOptionsScreen owner, Minecraft client, int width, int height, int y0, int entryHeight, Runnable diskWriter) {
		super(client, width, height, y0, y0 + height, entryHeight);
		this.owner = owner;
		this.diskWriter = diskWriter;
		setRenderSelectionHighlight(false);
	}

	public OptionsList(BaseOptionsScreen owner, Minecraft client, int width, int height, int y0, int entryHeight) {
		this(owner, client, width, height, y0, entryHeight, null);
	}

	public void setX(int x) {
		super.setX(x);
		this.minX = x;
		this.maxX = x + width;
	}

	public void setScrollAmount(double amount) {
		this.scrollAmount = (float) amount;
		this.targetScroll = (float) amount;
		capScrolling();
	}

	public float getScrollAmountF() {
		return scrollAmount;
	}

	@Override
	protected int size() {
		return visibleEntries.size();
	}

	@Override
	public int getRowWidth() {
		return Math.min(width, 300);
	}

	@Override
	protected int getScrollbarPosition() {
		return owner == null ? maxX - 6 : owner.width - 6;
	}

	public List<Entry> children() {
		return visibleEntries;
	}

	public List<Entry> getAllEntries() {
		return entries;
	}

	public Entry getEntry(int index) {
		return visibleEntries.get(index);
	}

	public int getRowTop(int index) {
		return minY + 4 - (int) scrollAmount + index * entryHeight;
	}

	public int getRowBottom(int index) {
		return getRowTop(index) + entryHeight;
	}

	public int getRowLeft() {
		return minX + width / 2 - getRowWidth() / 2 + 2;
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= minX && mouseX < maxX && mouseY >= minY && mouseY < maxY;
	}

	public int getEntryAt(int mouseX, int mouseY) {
		int rowLeft = getRowLeft();
		int rowRight = rowLeft + getRowWidth();
		if (mouseX < rowLeft || mouseX > rowRight || mouseY < minY || mouseY >= maxY) {
			return -1;
		}
		int k = mouseY - minY - headerHeight + (int) scrollAmount - 4;
		int l = k / entryHeight;
		return l >= 0 && k >= 0 && l < size() ? l : -1;
	}

	@Override
	protected void entryClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
	}

	@Override
	protected boolean isEntrySelected(int index) {
		return index == selectedIndex && !PreviewOptionsScreen.isAdjustingPosition();
	}

	@Override
	protected void renderBackground() {
	}

	@Override
	protected void renderEntry(int index, int x, int y, int height, int mouseX, int mouseY, float tickDelta) {
		if (index < 0 || index >= visibleEntries.size()) {
			return;
		}
		if (y + entryHeight < minY || y > maxY) {
			return;
		}
		Entry entry = visibleEntries.get(index);
		if (isEntrySelected(index) && isMouseInList(mouseY)) {
			GuiElement.fill(minX, y - 2, maxX, y + entryHeight + 2, 0x33FFFFFF);
		}
		entry.render(index, y, x, getRowWidth(), entryHeight, mouseX, mouseY, tickDelta);
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		int maxScroll = getMaxScroll();
		targetScroll = MathHelper.clamp(targetScroll, 0, maxScroll);
		float diff = targetScroll - scrollAmount;
		if (Math.abs(diff) > 0.0003F) {
			scrollAmount += diff * tickDelta;
		}
		capScrolling();

		if (!PreviewOptionsScreen.isAdjustingPosition()) {
			int hoveredIndex = getEntryAt(mouseX, mouseY);
			Entry hovered = hoveredIndex == -1 ? null : visibleEntries.get(hoveredIndex);
			if (hovered instanceof Title) {
				selectedIndex = -1;
			} else {
				selectedIndex = hoveredIndex;
			}
			int activeIndex = hovered != null ? hoveredIndex : MathHelper.clamp((int) scrollAmount / entryHeight, 0, Math.max(0, visibleEntries.size() - 1));
			if (activeIndex >= 0 && activeIndex != lastActiveIndex) {
				lastActiveIndex = activeIndex;
				Entry entry = activeIndex < visibleEntries.size() ? visibleEntries.get(activeIndex) : null;
				while (entry != null) {
					if (entry instanceof Title) {
						currentTitle = (Title) entry;
						break;
					}
					entry = entry.parent;
				}
			}
		}

		if (!org.lwjgl.input.Mouse.isButtonDown(0) && widgetInteracting) {
			if (draggingSlider != null) {
				draggingSlider.stopDragging();
			}
			draggingSlider = null;
			widgetInteracting = false;
		}
		if (visible) {
			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			int rowLeft = getRowLeft();
			int rowTop = minY + 4 - (int) scrollAmount;
			DisplayHelper.pushScissor(minX, minY, maxX - minX, maxY - minY);
			renderList(rowLeft, rowTop, mouseX, mouseY, tickDelta);
			DisplayHelper.popScissor();
			if (maxScroll > 0) {
				int scrollbarX = getScrollbarPosition();
				int thumbHeight = (maxY - minY) * (maxY - minY) / Math.max(1, getHeight());
				thumbHeight = MathHelper.clamp(thumbHeight, 32, maxY - minY - 8);
				int thumbY = (int) scrollAmount * (maxY - minY - thumbHeight) / maxScroll + minY;
				GuiElement.fill(scrollbarX, minY, scrollbarX + 6, maxY, 0xFF000000);
				GuiElement.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFF808080);
				GuiElement.fill(scrollbarX, thumbY, scrollbarX + 1, thumbY + thumbHeight, 0xFFC0C0C0);
				GuiElement.fill(scrollbarX + 5, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFFC0C0C0);
				GuiElement.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + 1, 0xFFC0C0C0);
				GuiElement.fill(scrollbarX, thumbY + thumbHeight - 1, scrollbarX + 6, thumbY + thumbHeight, 0xFFC0C0C0);
			}
			renderDecorations(mouseX, mouseY);
		}

		if (owner != null) {
			int top = owner.height - 32;
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bind(GuiElement.BACKGROUND_LOCATION);
			GlStateManager.color4f(0.35F, 0.35F, 0.35F, 1.0F);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder buffer = tesselator.getBuffer();
			buffer.begin(7, DefaultVertexFormat.POSITION_TEX);
			float f = 32.0F;
			float u = owner.width / f;
			float v = 1.0F;
			float u2 = u + owner.width / f;
			buffer.vertex(0.0, owner.height, 0.0).texture(u, v + 1).nextVertex();
			buffer.vertex(owner.width, owner.height, 0.0).texture(u2, v + 1).nextVertex();
			buffer.vertex(owner.width, top, 0.0).texture(u2, v).nextVertex();
			buffer.vertex(0.0, top, 0.0).texture(u, v).nextVertex();
			tesselator.end();
			net.minecraft.client.render.platform.GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawFooterGradient(0, top - 4, owner.width, 4, 0x00000000, 0xEE000000);
		}
	}

	/**
	 * Unlike {@link DisplayHelper#drawGradientRect}, this is independent of the overlay fade alpha.
	 */
	private void drawFooterGradient(int left, int top, int width, int height, int startColor, int endColor) {
		float a1 = (startColor >> 24 & 255) / 255.0F;
		float r1 = (startColor >> 16 & 255) / 255.0F;
		float g1 = (startColor >> 8 & 255) / 255.0F;
		float b1 = (startColor & 255) / 255.0F;
		float a2 = (endColor >> 24 & 255) / 255.0F;
		float r2 = (endColor >> 16 & 255) / 255.0F;
		float g2 = (endColor >> 8 & 255) / 255.0F;
		float b2 = (endColor & 255) / 255.0F;
		GlStateManager.disableTexture();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.shadeModel(7425);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuffer();
		buffer.begin(7, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(left + width, top, 0.0).color(r1, g1, b1, a1).nextVertex();
		buffer.vertex(left, top, 0.0).color(r1, g1, b1, a1).nextVertex();
		buffer.vertex(left, top + height, 0.0).color(r2, g2, b2, a2).nextVertex();
		buffer.vertex(left + width, top + height, 0.0).color(r2, g2, b2, a2).nextVertex();
		tesselator.end();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
	}

	public void syncScrollTarget() {
		targetScroll = scrollAmount;
	}

	public boolean isWidgetInteracting() {
		return widgetInteracting;
	}

	public boolean isScrollingByMouse() {
		return mouseYStart >= 0;
	}

	public void scrollSmooth(int wheel) {
		float speed = !ClientProxy.hasFastScroll && Screen.isControlDown() ? 4.5F : 1.5F;
		targetScroll = scrollAmount - (wheel > 0 ? 1 : -1) * entryHeight * speed;
	}

	public void save() {
		for (Entry entry : visibleEntries) {
			if (entry instanceof OptionValue) {
				((OptionValue<?>) entry).save();
			}
		}
		if (diskWriter != null)
			diskWriter.run();
	}

	public <T extends Entry> T add(T entry) {
		entries.add(entry);
		if (entry instanceof Title) {
			setDefaultParent(entry);
		} else if (defaultParent != null) {
			entry.parent(defaultParent);
		}
		return entry;
	}

	@Nullable
	public Entry getSelected() {
		return selectedIndex >= 0 && selectedIndex < visibleEntries.size() ? visibleEntries.get(selectedIndex) : null;
	}

	public void setSelected(@Nullable Entry entry) {
		selectedIndex = entry == null ? -1 : visibleEntries.indexOf(entry);
	}

	public void setDefaultParent(Entry defaultParent) {
		this.defaultParent = defaultParent;
	}

	public Text title(String string) {
		return add(new Title(string)).getTitle();
	}

	public OptionValue<Float> slider(String optionName, float value, Consumer<Float> setter) {
		return slider(optionName, value, setter, 0, 1, FloatUnaryOperator.identity());
	}

	public OptionValue<Float> slider(String optionName, float value, Consumer<Float> setter, float min, float max, FloatUnaryOperator aligner) {
		return add(new SliderOptionValue(optionName, value, setter, min, max, aligner));
	}

	public <T> OptionValue<T> input(String optionName, T value, Consumer<T> setter, Predicate<String> validator) {
		return add(new InputOptionValue<>(this::updateSaveState, optionName, value, setter, validator));
	}

	public <T> OptionValue<T> input(String optionName, T value, Consumer<T> setter) {
		return input(optionName, value, setter, Predicates.alwaysTrue());
	}

	public OptionValue<Boolean> choices(String optionName, boolean value, BooleanConsumer setter) {
		return choices(optionName, value, setter, null);
	}

	public OptionValue<Boolean> choices(String optionName, boolean value, BooleanConsumer setter, @Nullable Consumer<CycleOptionValue.Builder<Boolean>> builderConsumer) {
		CycleOptionValue.Builder<Boolean> builder = new CycleOptionValue.Builder<>(v -> v ? OPTION_ON : OPTION_OFF);
		builder.withValues(Arrays.asList(Boolean.TRUE, Boolean.FALSE));
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(optionName, builder, value, setter));
	}

	public <T extends Enum<T>> OptionValue<T> choices(String optionName, T value, Consumer<T> setter) {
		return choices(optionName, value, setter, null);
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> OptionValue<T> choices(String optionName, T value, Consumer<T> setter, @Nullable Consumer<CycleOptionValue.Builder<T>> builderConsumer) {
		List<T> values = Arrays.asList((T[]) value.getClass().getEnumConstants());
		CycleOptionValue.Builder<T> builder = new CycleOptionValue.Builder<>(v -> {
			String name = v.name().toLowerCase(Locale.ENGLISH);
			if ("on".equals(name)) {
				return OPTION_ON;
			}
			if ("off".equals(name)) {
				return OPTION_OFF;
			}
			return Entry.makeTitle(optionName + "_" + name);
		});
		builder.withValues(values);
		if (builderConsumer != null) {
			builderConsumer.accept(builder);
		}
		return add(new CycleOptionValue<>(optionName, builder, value, setter));
	}

	public <T> OptionValue<T> choices(String optionName, T value, List<T> values, Consumer<T> setter, Function<T, Text> nameProvider) {
		CycleOptionValue.Builder<T> builder = new CycleOptionValue.Builder<>(nameProvider);
		builder.withValues(values);
		return add(new CycleOptionValue<>(optionName, builder, value, setter));
	}

	public void keybind(net.minecraft.client.options.KeyBinding keybind) {
		add(new KeybindOptionButton(this, keybind));
	}

	public void removed() {
		forcePreview.clear();
		for (Entry entry : entries) {
			entry.parent = null;
			entry.children.clear();
		}
		entries.clear();
		visibleEntries.clear();
		owner = null;
	}

	public void updateSearch(String search) {
		visibleEntries.clear();
		if (search == null || search.trim().isEmpty()) {
			visibleEntries.addAll(entries);
			return;
		}
		Set<Entry> matches = Sets.newLinkedHashSet();
		String[] keywords = search.toLowerCase(Locale.ENGLISH).split("\\s+");
		for (Entry entry : entries) {
			int bingo = 0;
			for (String keyword : keywords) {
				for (String message : entry.getMessages()) {
					if (message.contains(keyword)) {
						bingo++;
						break;
					}
				}
			}
			if (bingo == keywords.length) {
				walkChildren(entry, matches::add);
				Entry parent = entry.parent;
				while (parent != null) {
					matches.add(parent);
					parent = parent.parent;
				}
			}
		}
		for (Entry entry : entries) {
			if (matches.contains(entry)) {
				visibleEntries.add(entry);
			}
		}
		if (matches.isEmpty()) {
			visibleEntries.add(new Title(new LiteralText(I18n.translate("gui.jade.no_results")).setStyle(new Style().setColor(Formatting.GRAY))));
		}
	}

	private static void walkChildren(Entry entry, Consumer<Entry> consumer) {
		consumer.accept(entry);
		for (Entry child : entry.children) {
			walkChildren(child, consumer);
		}
	}

	public void updateSaveState() {
		if (owner == null || owner.saveButton == null) {
			return;
		}
		for (Entry entry : entries) {
			if (entry instanceof OptionValue<?> && !((OptionValue<?>) entry).isValidValue()) {
				owner.saveButton.active = false;
				return;
			}
		}
		owner.saveButton.active = true;
	}

	public void showOnTop(Entry entry) {
		int index = visibleEntries.indexOf(entry);
		if (index != -1) {
			targetScroll = (float) entryHeight * index + 1;
		}
	}

	public void resetMappingAndUpdateButtons() {
		for (Entry entry : entries) {
			if (entry instanceof KeybindOptionButton) {
				((KeybindOptionButton) entry).refresh(selectedKey);
			}
		}
	}

	public void clearTextFocus() {
		for (Entry entry : entries) {
			for (JadeWidget widget : entry.getWidgets()) {
				if (widget instanceof TextFieldWidget && ((TextFieldWidget) widget).isFocused()) {
					((TextFieldWidget) widget).setFocused(false);
				}
			}
		}
	}

	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		widgetInteracting = false;
		int index = getEntryAt(mouseX, mouseY);
		if (index == -1) {
			return false;
		}
		Entry entry = visibleEntries.get(index);
		JadeWidget hovered = entry.getHoveredWidget(mouseX, mouseY);
		boolean handled = entry.mouseClicked(mouseX, mouseY, button);
		if (handled) {
			widgetInteracting = true;
			if (hovered instanceof SliderOptionValue.Slider) {
				draggingSlider = (SliderOptionValue.Slider) hovered;
			}
		}
		return handled;
	}

	public boolean mouseReleased(int mouseX, int mouseY, int button) {
		if (draggingSlider != null) {
			draggingSlider.stopDragging();
		}
		draggingSlider = null;
		widgetInteracting = false;
		return false;
	}

	public boolean mouseDragged(int mouseX, int mouseY, int button, long time) {
		if (draggingSlider != null) {
			draggingSlider.mouseDragged(mouseX, mouseY);
			return true;
		}
		return false;
	}

	public void setDraggingSlider(SliderOptionValue.Slider slider) {
		this.draggingSlider = slider;
	}

	public boolean keyPressed(char chr, int keyCode) {
		if (selectedKey != null) {
			net.minecraft.client.options.GameOptions options = Minecraft.getInstance().options;
			options.setKeyCode(selectedKey, keyCode == 1 ? org.lwjgl.input.Keyboard.KEY_NONE : keyCode);
			selectedKey = null;
			resetMappingAndUpdateButtons();
			return true;
		}
		for (Entry entry : visibleEntries) {
			if (entry.keyPressed(chr, keyCode)) {
				return true;
			}
		}
		return false;
	}

	public static class Entry {

		protected final Minecraft client;
		private final List<String> messages = Lists.newArrayList();
		protected final List<JadeWidget> widgets = Lists.newArrayList();
		private final List<int[]> widgetOffsets = Lists.newArrayList();
		@Nullable
		protected String description;
		private Entry parent;
		private final List<Entry> children = Lists.newArrayList();

		public Entry() {
			client = Minecraft.getInstance();
		}

		public static Text makeTitle(String key) {
			return new LiteralText(I18n.translate(makeKey(key)));
		}

		public static String makeKey(String key) {
			return "config.jade." + key;
		}

		@Nullable
		public JadeWidget getFirstWidget() {
			return widgets.isEmpty() ? null : widgets.get(0);
		}

		public List<JadeWidget> getWidgets() {
			return widgets;
		}

		public void addWidget(JadeWidget widget, int offsetX) {
			addWidget(widget, offsetX, -widget.getHeight() / 2);
		}

		public void addWidget(JadeWidget widget, int offsetX, int offsetY) {
			widgets.add(widget);
			widgetOffsets.add(new int[]{offsetX, offsetY});
		}

		public void render(int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, float deltaTime) {
			for (int i = 0; i < widgets.size(); i++) {
				JadeWidget widget = widgets.get(i);
				int[] offset = widgetOffsets.get(i);
				widget.setPos(rowLeft + width - 110 + offset[0], rowTop + height / 2 + offset[1]);
				widget.render(mouseX, mouseY, deltaTime);
			}
		}

		public void setDisabled(boolean b) {
			for (JadeWidget widget : widgets) {
				widget.setActive(!b);
			}
		}

		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			for (JadeWidget widget : widgets) {
				if (!widget.isActive() || !widget.isVisible()) {
					continue;
				}
				if (mouseX >= widget.getX() && mouseX < widget.getX() + widget.getWidth() && mouseY >= widget.getY() && mouseY < widget.getY() + widget.getHeight()) {
					if (widget.mouseClicked(mouseX, mouseY, button)) {
						return true;
					}
				}
			}
			return false;
		}

		@Nullable
		public JadeWidget getHoveredWidget(int mouseX, int mouseY) {
			for (JadeWidget widget : widgets) {
				if (widget.isVisible() && mouseX >= widget.getX() && mouseX < widget.getX() + widget.getWidth() && mouseY >= widget.getY() && mouseY < widget.getY() + widget.getHeight()) {
					return widget;
				}
			}
			return null;
		}

		public boolean keyPressed(char chr, int keyCode) {
			for (JadeWidget widget : widgets) {
				if (widget instanceof TextFieldWidget && ((TextFieldWidget) widget).isFocused()) {
					return widget.keyPressed(chr, keyCode);
				}
			}
			return false;
		}

		@Nullable
		public String getDescription() {
			return description;
		}

		public int getTextX(int width) {
			return 0;
		}

		public int getTextWidth() {
			return 0;
		}

		public Entry parent(Entry parent) {
			this.parent = parent;
			parent.children.add(this);
			return this;
		}

		public Entry parent() {
			return parent;
		}

		public final List<String> getMessages() {
			return messages;
		}

		public void addMessage(String message) {
			messages.add(Formatting.strip(message).toLowerCase(Locale.ENGLISH));
		}

		public void addMessageKey(String key) {
			key = makeKey(key + "_extra_msg");
			if (I18n.hasTranslation(key)) {
				addMessage(I18n.translate(key));
			}
		}
	}

	public static class Title extends Entry {

		public Text narration;
		private final Text title;
		private int x;

		public Title(String key) {
			title = makeTitle(key);
			addMessageKey(key);
			addMessage(title.getString());
			String desc = makeKey(key + "_desc");
			if (I18n.hasTranslation(desc)) {
				description = I18n.translate(desc);
				addMessage(description);
			}
			narration = title;
		}

		public Title(Text title) {
			this.title = title;
			narration = title;
		}

		public Text getTitle() {
			return title;
		}

		@Override
		public void render(int index, int rowTop, int rowLeft, int width, int height, int mouseX, int mouseY, float deltaTime) {
			x = rowLeft;
			client.textRenderer.draw(title.getFormattedString(), getTextX(width), rowTop + height - client.textRenderer.fontHeight, 16777215);
		}

		@Override
		public int getTextX(int width) {
			return x + (width - client.textRenderer.getWidth(title.getString())) / 2;
		}

		@Override
		public int getTextWidth() {
			return client.textRenderer.getWidth(title.getString());
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			return true;
		}
	}

}
