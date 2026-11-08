package snownee.jade.overlay;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableObject;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.Identifiers;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.TooltipRect;
import snownee.jade.gui.PreviewOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
import snownee.jade.impl.ui.BoxElement;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.ModIdentification;

public class OverlayRenderer {

	public static final MutableObject<Theme> theme = new MutableObject<>();
	public static final TooltipRect rect = new TooltipRect();
	public static float ticks;
	public static boolean shown;
	public static float alpha;
	private static BoxElement lingerTooltip;
	private static float disappearTicks;

	public static Theme theme() {
		return theme.getValue();
	}

	public static boolean shouldShow() {
		if (WailaTickHandler.instance().rootElement == null) {
			return false;
		}

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (!general.shouldDisplayTooltip())
			return false;

		if (general.getDisplayMode() == IWailaConfig.DisplayMode.HOLD_KEY && (JadeClient.showOverlay == null || !JadeClient.showOverlay.isPressed()))
			return false;

		BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		return mode != BossBarOverlapMode.HIDE_TOOLTIP || ClientProxy.getBossBarRect() == null;
	}

	public static boolean shouldShowImmediately(BoxElement box) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.world == null)
			return false;

		if (!ClientProxy.shouldShowWithOverlay(mc, mc.screen)) {
			return false;
		}

		box.updateExpectedRect(rect);
		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (mc.screen instanceof PreviewOptionsScreen) {
			PreviewOptionsScreen optionsScreen = (PreviewOptionsScreen) mc.screen;
			if (optionsScreen.forcePreviewOverlay()) {
				return true;
			}
			if (!general.previewOverlay) {
				return false;
			}
			int x = org.lwjgl.input.Mouse.getX() * DisplayHelper.guiWidth() / mc.width;
			int y = DisplayHelper.guiHeight() - org.lwjgl.input.Mouse.getY() * DisplayHelper.guiHeight() / mc.height - 1;
			if (rect.expectedRect.contains(x, y)) {
				return false;
			}
		}

		if (mc.options.hideGui)
			return false;

		if (mc.options.debugEnabled && general.shouldHideFromDebug())
			return false;

		return !(mc.gui.getPlayerTabOverlay() instanceof snownee.jade.mixin.PlayerTabOverlayAccess) || !((snownee.jade.mixin.PlayerTabOverlayAccess) mc.gui.getPlayerTabOverlay()).isVisible() || !general.shouldHideFromTabList();
	}

	/**
	 * NOTE!!!
	 * <p>
	 * Please do NOT replace the whole codes with Mixin.
	 * It will make me unable to locate bugs.
	 * A regular plugin can also realize the same features.
	 * <p>
	 * Secondly, please notice the license that Jade is using.
	 * I don't think it is compatible with some open-source licenses.
	 */
	public static void renderOverlay478757() {
		float delta = Minecraft.getInstance().getTickDelta();
		ticks += delta;
		shown = false;
		BoxElement root = WailaTickHandler.instance().rootElement;
		boolean show;
		if (root == null && PreviewOptionsScreen.isAdjustingPosition()) {
			Tooltip tooltip = new Tooltip();
			tooltip.add(IThemeHelper.get().title(new ItemStack(Blocks.GRASS).getHoverName()));
			tooltip.add(IThemeHelper.get().modName(ModIdentification.getModName(Blocks.GRASS)));
			root = new BoxElement(tooltip, IThemeHelper.get().theme().tooltipStyle);
			root.tag(Identifiers.ROOT);
			root.setThemeIcon(ItemStackElement.of(new ItemStack(Blocks.GRASS)), IThemeHelper.get().theme());
			root.updateExpectedRect(rect);
			show = true;
		} else {
			show = shouldShow();
		}
		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		if (root != null) {
			lingerTooltip = root;
		}
		if (root == null && lingerTooltip != null) {
			disappearTicks += delta;
			if (disappearTicks < overlay.getDisappearingDelay()) {
				root = lingerTooltip;
				show = true;
			}
		} else {
			disappearTicks = 0;
		}
		if (overlay.getAnimation() && lingerTooltip != null) {
			root = lingerTooltip;
			float speed = general.isDebug() ? 0.1F : 0.6F;
			alpha += (show ? speed : -speed) * delta;
			alpha = MathHelper.clamp(alpha, 0, 1);
		} else {
			alpha = show ? 1 : 0;
		}

		if (alpha < 0.1F || root == null || !shouldShowImmediately(root)) {
			if (!PreviewOptionsScreen.isAdjustingPosition()) {
				lingerTooltip = null;
				rect.rect.width = 0;
				WailaTickHandler.clearLastNarration();
				return;
			}
		}

		Minecraft.getInstance().profiler.push("Jade Overlay");
		renderOverlay478757(root);
		Minecraft.getInstance().profiler.pop();
	}

	public static void renderOverlay478757(BoxElement root) {
		root.updateRect(rect);

		for (JadeBeforeRenderCallback callback : WailaClientRegistration.instance().beforeRenderCallback.callbacks()) {
			if (callback.beforeRender(root, rect, ObjectDataCenter.get())) {
				return;
			}
		}

		GlStateManager.disableDepthTest();
		GlStateManager.pushMatrix();
		float z = Minecraft.getInstance().screen == null ? 1 : 100;
		GlStateManager.translatef(rect.rect.getX(), rect.rect.getY(), z);

		float scale = rect.scale;
		if (scale != 1) {
			GlStateManager.scalef(scale, scale, 1.0F);
		}

		GlStateManager.enableBlend();
		{
			float maxWidth = rect.rect.getWidth();
			float maxHeight = rect.rect.getHeight();
			if (root.getStyle().hasRoundCorner()) {
				maxWidth -= 2;
				maxHeight -= 2;
			}
			maxWidth = maxWidth / scale;
			maxHeight = maxHeight / scale;
			root.render(0, 0, maxWidth, maxHeight);
		}

		WailaClientRegistration.instance().afterRenderCallback.call(callback -> {
			callback.afterRender(root, rect, ObjectDataCenter.get());
		});

		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		GlStateManager.enableDepthTest();

		if (Jade.CONFIG.get().getGeneral().shouldEnableTextToSpeech()) {
			WailaTickHandler.narrate(root.getTooltip(), true);
		}

		shown = true;
	}
}
