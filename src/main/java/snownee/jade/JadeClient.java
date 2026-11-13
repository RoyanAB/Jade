package snownee.jade;

import net.minecraft.block.state.BlockState;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toast.SystemToast;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipFlag;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.IConfigOverlay;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.api.ui.*;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.mixin.ClientPlayerInteractionManagerAccess;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;

import java.text.MessageFormat;
import java.util.Objects;

public final class JadeClient {

	public static KeyBinding openConfig;
	public static KeyBinding showOverlay;
	public static KeyBinding toggleLiquid;
	public static KeyBinding showDetails;
	public static KeyBinding narrate;
	public static KeyBinding showRecipes;
	public static KeyBinding showUses;
	public static boolean hideModName;
	private static float savedProgress;
	private static float progressAlpha;
	private static boolean canHarvest;

	public static void onKeyPressed(int action) {
		while (openConfig != null && openConfig.consumeClick()) {
			Jade.CONFIG.invalidate();
			Minecraft.getInstance().openScreen(new HomeConfigScreen(null));
		}

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		while (showOverlay != null && showOverlay.consumeClick()) {
			DisplayMode mode = general.getDisplayMode();
			if (mode == IWailaConfig.DisplayMode.TOGGLE) {
				general.setDisplayTooltip(!general.shouldDisplayTooltip());
				if (!general.shouldDisplayTooltip() && general.hintOverlayToggle) {
					SystemToast.addOrUpdate(Minecraft.getInstance().getToasts(), SystemToast.Type.TUTORIAL_HINT,
						new LiteralText(I18n.translate("toast.jade.toggle_hint.1")),
						new LiteralText(I18n.translate("toast.jade.toggle_hint.2", GameOptions.getKeyName(showOverlay.getKeyCode()))));
					general.hintOverlayToggle = false;
				}
				Jade.CONFIG.save();
			}
		}

		while (toggleLiquid != null && toggleLiquid.consumeClick()) {
			general.setDisplayFluids(!general.shouldDisplayFluids());
			Jade.CONFIG.save();
		}

		while (narrate != null && narrate.consumeClick()) {
			if (general.getTTSMode() == TTSMode.TOGGLE) {
				general.toggleTTS();
				if (general.shouldEnableTextToSpeech() && general.hintNarratorToggle) {
					SystemToast.addOrUpdate(Minecraft.getInstance().getToasts(), SystemToast.Type.NARRATOR_TOGGLE,
						new LiteralText(I18n.translate("toast.jade.tts_hint.1")),
						new LiteralText(I18n.translate("toast.jade.tts_hint.2", GameOptions.getKeyName(narrate.getKeyCode()))));
					general.hintNarratorToggle = false;
				}
				Jade.CONFIG.save();
			} else if (WailaTickHandler.instance().rootElement != null) {
				WailaTickHandler.narrate(WailaTickHandler.instance().rootElement.getTooltip(), false);
			}
		}
	}

	public static void onTooltip(java.util.List<String> tooltip, ItemStack stack, TooltipFlag context) {
		appendModName(tooltip, stack, context);
		if (Jade.CONFIG.get().getGeneral().isDebug() && stack.hasNbt()) {
			tooltip.add(stack.getNbt().toString());
		}
	}

	private static void appendModName(java.util.List<String> tooltip, ItemStack stack, TooltipFlag context) {
		if (hideModName || !Jade.CONFIG.get().getGeneral().showItemModNameTooltip())
			return;
		int i = 1;
		String name = ModIdentification.getModName(stack);
		for (; i < tooltip.size(); i++) {
			if (Objects.equals(Formatting.strip(tooltip.get(i)), name)) {
				break;
			}
		}
		Style style = Jade.CONFIG.get().getFormatting().getItemModNameStyle();
		StringBuilder formatted = new StringBuilder();
		if (style.getColor() != null) {
			formatted.append(style.getColor());
		}
		if (style.isItalic()) {
			formatted.append(Formatting.ITALIC);
		}
		tooltip.add(i, formatted.append(name).toString());
	}

	@Nullable
	public static Accessor<?> limitMobEffectFog(Accessor<?> accessor) {
		return accessor;
	}

	public static void drawBreakingProgress(IBoxElement rootElement, TooltipRect rect, Accessor<?> accessor) {
		if (!PluginConfig.INSTANCE.get(Identifiers.MC_BREAKING_PROGRESS)) {
			progressAlpha = 0;
			savedProgress = 0;
			return;
		}
		if (!Float.isNaN(rootElement.getBoxProgress())) {
			progressAlpha = 0;
			savedProgress = 0;
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.interactionManager == null || mc.world == null || mc.player == null) {
			return;
		}

		ClientPlayerInteractionManager manager = mc.interactionManager;
		BlockPos pos = ((ClientPlayerInteractionManagerAccess) manager).jade$getTarget();
		BlockState state = mc.world.getBlockState(pos);
		boolean destroying = manager.isMiningBlock();
		if (destroying) {
			canHarvest = CommonProxy.isCorrectToolForDrops(state, mc.player);
		} else if (progressAlpha == 0) {
			savedProgress = 0;
			return;
		}
		Theme theme = IThemeHelper.get().theme();
		ColorPalette colors = theme.tooltipStyle.boxProgressColors;
		int color = canHarvest ? colors.normal() : colors.failure();
		float top = rootElement.getCachedSize().y;
		float width = rootElement.getCachedSize().x;
		boolean roundCorner = !IWailaConfig.get().getOverlay().getSquare();
		if (roundCorner && theme.tooltipStyle instanceof BoxStyle.GradientBorder) {
			top += 1;
		}
		progressAlpha += Minecraft.getInstance().getTickDelta() * (destroying ? 0.1F : -0.1F);
		if (destroying) {
			progressAlpha = Math.min(progressAlpha, 0.6F);
			float perTick = state.getMiningSpeed(mc.player, mc.world, pos);
			float progress = ((ClientPlayerInteractionManagerAccess) manager).jade$getMiningProgress();
			if (progress + perTick >= 1) {
				progressAlpha = savedProgress = 1;
			} else {
				savedProgress = MathHelper.clamp(progress + mc.getTickDelta() * perTick, 0, 1);
			}
		} else {
			progressAlpha = Math.max(progressAlpha, 0);
			if (progressAlpha == 0) {
				savedProgress = 0;
				return;
			}
		}
		if (progressAlpha == 0) {
			savedProgress = 0;
			return;
		}
		color = IConfigOverlay.applyAlpha(color, progressAlpha);
		float offset0 = theme.tooltipStyle.boxProgressOffset(Direction2D.UP);
		float offset1 = theme.tooltipStyle.boxProgressOffset(Direction2D.RIGHT);
		float offset2 = theme.tooltipStyle.boxProgressOffset(Direction2D.DOWN);
		float offset3 = theme.tooltipStyle.boxProgressOffset(Direction2D.LEFT);
		width += offset1 - offset3;
		DisplayHelper.fill(offset3, top - 1 + offset0, offset3 + width * savedProgress, top + offset2, color);
	}

	public static String format(String s, Object... objects) {
		try {
			return MessageFormat.format(I18n.translate(s), objects);
		} catch (Exception e) {
			return I18n.translate(s);
		}
	}
}
