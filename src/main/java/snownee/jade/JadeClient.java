package snownee.jade;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toast.SystemToast;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipFlag;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import snownee.jade.api.Accessor;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.TTSMode;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.ModIdentification;

public final class JadeClient {

	public static KeyBinding openConfig;
	public static KeyBinding showOverlay;
	public static KeyBinding toggleLiquid;
	public static KeyBinding showDetails;
	public static KeyBinding narrate;
	public static KeyBinding showRecipes;
	public static KeyBinding showUses;
	public static boolean hideModName;

	public static void init() {
	}

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
			}
		}
	}

	public static void onGui(net.minecraft.client.gui.screen.Screen screen) {
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
			if (Objects.equals(tooltip.get(i), name)) {
				break;
			}
		}
		tooltip.add(i, net.minecraft.text.Formatting.BLUE.toString() + net.minecraft.text.Formatting.ITALIC.toString() + name);
	}

	@Nullable
	public static Accessor<?> limitMobEffectFog(Accessor<?> accessor) {
		return accessor;
	}

	public static String format(String s, Object... objects) {
		try {
			return String.format(I18n.translate(s), objects);
		} catch (Exception e) {
			return s;
		}
	}
}
