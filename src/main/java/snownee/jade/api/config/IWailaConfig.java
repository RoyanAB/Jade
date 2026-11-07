package snownee.jade.api.config;

import net.minecraft.resource.Identifier;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import snownee.jade.Internals;
import snownee.jade.api.theme.Theme;

public interface IWailaConfig {

	static IWailaConfig get() {
		return Internals.getWailaConfig();
	}

	IConfigGeneral getGeneral();

	IConfigOverlay getOverlay();

	IConfigFormatting getFormatting();

	IPluginConfig getPlugin();

	enum IconMode {
		TOP, CENTERED, HIDE
	}

	enum TTSMode {
		TOGGLE, PRESS
	}

	enum DisplayMode {
		HOLD_KEY, TOGGLE, LITE
	}

	enum FluidMode {
		NONE, ANY, SOURCE_ONLY
	}

	enum BossBarOverlapMode {
		NO_OPERATION, HIDE_BOSS_BAR, HIDE_TOOLTIP, PUSH_DOWN
	}

	interface IConfigGeneral {

		void setDisplayTooltip(boolean displayTooltip);

		boolean getDisplayEntities();

		void setDisplayEntities(boolean displayEntities);

		boolean getDisplayBosses();

		void setDisplayBosses(boolean displayBosses);

		boolean getDisplayBlocks();

		void setDisplayBlocks(boolean displayBlocks);

		void setHideFromDebug(boolean hideFromDebug);

		void setHideFromTabList(boolean hideFromTabList);

		void toggleTTS();

		void setItemModNameTooltip(boolean itemModNameTooltip);

		boolean shouldDisplayTooltip();

		DisplayMode getDisplayMode();

		void setDisplayMode(DisplayMode displayMode);

		boolean shouldHideFromDebug();

		boolean shouldHideFromTabList();

		boolean shouldEnableTextToSpeech();

		TTSMode getTTSMode();

		void setTTSMode(TTSMode ttsMode);

		boolean shouldDisplayFluids();

		FluidMode getDisplayFluids();

		void setDisplayFluids(boolean displayFluids);

		void setDisplayFluids(FluidMode displayFluids);

		boolean showItemModNameTooltip();

		float getReachDistance();

		void setReachDistance(float reachDistance);

		BossBarOverlapMode getBossBarOverlapMode();

		void setBossBarOverlapMode(BossBarOverlapMode mode);

		boolean isDebug();

		void setDebug(boolean debug);

		boolean getBuiltinCamouflage();

		void setBuiltinCamouflage(boolean builtinCamouflage);
	}

	interface IConfigOverlay {

		static int applyAlpha(int color, float alpha) {
			int prevAlphaChannel = (color >> 24) & 0xFF;
			if (prevAlphaChannel > 0)
				alpha *= prevAlphaChannel / 256f;
			int alphaChannel = MathHelper.clamp((int) (0xFF * alpha), 4, 255);
			return (color & 0xFFFFFF) | alphaChannel << 24;
		}

		float getOverlayPosX();

		void setOverlayPosX(float overlayPosX);

		float getOverlayPosY();

		void setOverlayPosY(float overlayPosY);

		float getOverlayScale();

		void setOverlayScale(float overlayScale);

		float getAnchorX();

		void setAnchorX(float overlayAnchorX);

		float getAnchorY();

		void setAnchorY(float overlayAnchorY);

		boolean getFlipMainHand();

		void setFlipMainHand(boolean overlaySquare);

		float tryFlip(float f);

		boolean getSquare();

		void setSquare(boolean overlaySquare);

		float getAutoScaleThreshold();

		float getAlpha();

		void setAlpha(float alpha);

		Theme getTheme();

		void applyTheme(Identifier id);

		boolean shouldShowIcon();

		IconMode getIconMode();

		void setIconMode(IconMode iconMode);

		boolean getAnimation();

		void setAnimation(boolean animation);

		float getDisappearingDelay();

		void setDisappearingDelay(float delay);
	}

	interface IConfigFormatting {

		Style getItemModNameStyle();

		void setItemModNameStyle(Style itemModNameStyle);

		Text registryName(String name);
	}
}
