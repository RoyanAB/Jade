package snownee.jade.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.addon.core.CorePlugin;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.theme.Theme;
import snownee.jade.gui.config.OptionButton;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.jade.gui.config.widget.CycleButtonWidget;
import snownee.jade.gui.config.widget.JadeButton;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.impl.config.WailaConfig.ConfigOverlay;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WailaConfigScreen extends PreviewOptionsScreen {

	private OptionValue<Boolean> squareEntry;
	private OptionValue<Float> opacityEntry;
	private final Map<KeyBinding, Integer> keyMap;

	public WailaConfigScreen(Screen parent) {
		super(parent, new TranslatableText("gui.jade.jade_settings"));
		saver = Jade.CONFIG::save;
		keyMap = Maps.newHashMap();
		for (KeyBinding keyBinding : Minecraft.getInstance().options.keyBindings) {
			if (JadeClient.openConfig.getCategory().equals(keyBinding.getCategory())) {
				keyMap.put(keyBinding, keyBinding.getKeyCode());
			}
		}
		canceller = () -> {
			Jade.CONFIG.invalidate();
			keyMap.forEach(KeyBinding::setKeyCode);
			KeyBinding.resetMapping();
			Minecraft.getInstance().options.save();
		};
	}

	public static OptionsList.Entry editBlocklist(OptionsList.Entry entry, String fileName, Runnable defaultCreator) {
		if (entry.getFirstWidget() != null) {
			entry.getFirstWidget().setWidth(79);
		}
		JadeButton button = new JadeButton(0, 0, 20, 20, "☰");
		button.tooltip(new TranslatableText(OptionsList.Entry.makeKey("edit_blocklist")));
		entry.addWidget(new ButtonWrapper(button, () -> {
			File file = new File(CommonProxy.getConfigDirectory(), "jade/" + fileName + ".json");
			if (!file.exists()) {
				defaultCreator.run();
			}
			CommonProxy.openFile(file);
		}), 80);
		return entry;
	}

	@Override
	public OptionsList createOptions() {
		Objects.requireNonNull(minecraft);
		OptionsList options = new OptionsList(this, minecraft, width - 120, height - 32, 0, 26, Jade.CONFIG::save);

		ConfigGeneral general = Jade.CONFIG.get().getGeneral();
		options.title("general");
		if (CommonProxy.isDevEnv())
			options.choices("debug_mode", general.isDebug(), general::setDebug);
		options.choices("display_tooltip", general.shouldDisplayTooltip(), general::setDisplayTooltip);
		OptionsList.Entry entry = options.choices("display_entities", general.getDisplayEntities(), general::setDisplayEntities);
		editBlocklist(entry, "hide-entities", () -> CorePlugin.createEntityBlocklist().get());
		options.choices("display_bosses", general.getDisplayBosses(), general::setDisplayBosses).parent(entry);
		entry = options.choices("display_blocks", general.getDisplayBlocks(), general::setDisplayBlocks);
		editBlocklist(entry, "hide-blocks", () -> CorePlugin.createBlockBlocklist().get());
		options.choices("display_fluids", general.shouldDisplayFluids(), general::setDisplayFluids).parent(entry);
		options.choices("display_mode", general.getDisplayMode(), general::setDisplayMode, builder -> {
			builder.withTooltip(mode -> {
				String key = "display_mode_" + mode.name().toLowerCase(Locale.ENGLISH) + "_desc";
				if (mode == IWailaConfig.DisplayMode.LITE && "fabric".equals(CommonProxy.getPlatformIdentifier())) {
					key += ".fabric";
				}
				return OptionsList.Entry.makeTitle(key);
			});
		});
		OptionValue<?> value = options.choices("item_mod_name", general.showItemModNameTooltip(), general::setItemModNameTooltip);
		if (!ConfigGeneral.itemModNameTooltipDisabledByMods.isEmpty()) {
			value.setDisabled(true);
			value.appendDescription(I18n.translate("gui.jade.disabled_by_mods"));
			ConfigGeneral.itemModNameTooltipDisabledByMods.forEach(value::appendDescription);
			if (value.getFirstWidget() != null && value.getDescription() != null) {
				String description = value.getDescription();
				if (value.getFirstWidget() instanceof CycleButtonWidget) {
					((CycleButtonWidget<?>) value.getFirstWidget()).tooltipProvider = v -> new LiteralText(description);
				}
			}
		}
		options.choices("hide_from_debug", general.shouldHideFromDebug(), general::setHideFromDebug);
		options.choices("hide_from_tab_list", general.shouldHideFromTabList(), general::setHideFromTabList);
		options.choices("boss_bar_overlap", general.getBossBarOverlapMode(), general::setBossBarOverlapMode);
		options.slider("reach_distance", general.getReachDistance(), general::setReachDistance, 0, 20, f -> MathHelper.floor(f * 2) / 2F);

		ConfigOverlay overlay = Jade.CONFIG.get().getOverlay();
		options.title("overlay");
		List<Identifier> themes = IThemeHelper.get().getThemes().stream().filter($ -> !$.hidden).map($ -> $.id).collect(Collectors.toList());
		options.choices("overlay_theme", overlay.getTheme().id, themes, id -> {
			if (Objects.equals(id, overlay.getTheme().id))
				return;
			overlay.applyTheme(id);
			Theme theme = overlay.getTheme();
			if (theme.changeRoundCorner != null)
				squareEntry.setValue(theme.changeRoundCorner);
			if (theme.changeOpacity != 0)
				opacityEntry.setValue(theme.changeOpacity);
		}, id -> new TranslatableText("jade.theme." + id.getNamespace() + "." + id.getPath()));
		squareEntry = options.choices("overlay_square", overlay.getSquare(), overlay::setSquare);
		opacityEntry = options.slider("overlay_alpha", overlay.getAlpha(), overlay::setAlpha);
		options.forcePreview.add(options.slider("overlay_scale", overlay.getOverlayScale(), overlay::setOverlayScale, 0.2f, 2, FloatUnaryOperator.identity()));
		JadeButton adjustButton = new JadeButton(0, 0, 100, 20, I18n.translate(OptionsList.Entry.makeKey("overlay_pos.adjust")));
		adjustButton.setActive(true);
		options.add(new OptionButton(OptionsList.Entry.makeTitle("overlay_pos"), new ButtonWrapper(adjustButton, () -> adjustingPosition = true)));
		options.choices("display_item", overlay.getIconMode(), overlay::setIconMode);
		options.choices("animation", overlay.getAnimation(), overlay::setAnimation);

		options.title("key_binds");
		options.keybind(JadeClient.openConfig);
		options.keybind(JadeClient.showOverlay);
		options.keybind(JadeClient.toggleLiquid);
		if (ClientProxy.shouldRegisterRecipeViewerKeys()) {
			options.keybind(JadeClient.showRecipes);
			options.keybind(JadeClient.showUses);
		}
		options.keybind(JadeClient.narrate);
		options.keybind(JadeClient.showDetails);

		options.title("accessibility");
		options.choices("flip_main_hand", overlay.getFlipMainHand(), overlay::setFlipMainHand);
		options.choices("tts_mode", general.getTTSMode(), general::setTTSMode);

		options.title("danger_zone").setStyle(new Style().setColor(Formatting.RED));
		JadeButton resetButton = new JadeButton(0, 0, 100, 20, I18n.translate("controls.reset")).textColor(0xFF5555);
		resetButton.setActive(true);
		Text title = OptionsList.Entry.makeTitle("reset_settings");
		title.setStyle(new Style().setColor(Formatting.RED));
		options.add(new OptionButton(title, new ButtonWrapper(resetButton, () -> minecraft.openScreen(new ConfirmScreen((bl, confirmId) -> {
			if (bl) {
				for (KeyBinding keyBinding : minecraft.options.keyBindings) {
					if (JadeClient.openConfig.getCategory().equals(keyBinding.getCategory())) {
						keyBinding.setKeyCode(keyBinding.getDefaultKeyCode());
					}
				}
				//port: rebuild the 1.12 keyCode -> KeyBinding map, otherwise the defaults only apply after a restart
				KeyBinding.resetMapping();
				minecraft.options.save();
				try {
					int themesHash = Jade.CONFIG.get().getOverlay().themesHash;
					Preconditions.checkState(Jade.CONFIG.getFile().delete());
					Preconditions.checkState(PluginConfig.INSTANCE.getFile().delete());
					Jade.CONFIG.invalidate();
					Jade.CONFIG.get().getOverlay().themesHash = themesHash;
					Jade.CONFIG.save();
					PluginConfig.INSTANCE.reload();
				} catch (Throwable e) {
					Jade.LOGGER.error("", e);
				}
			}
			minecraft.openScreen(WailaConfigScreen.this);
			WailaConfigScreen.this.options.setScrollAmount(WailaConfigScreen.this.options.getMaxScroll());
		}, Formatting.RED + title.getString(), I18n.translate(OptionsList.Entry.makeKey("reset_settings.confirm")), Formatting.RED + I18n.translate("controls.reset"), I18n.translate("gui.cancel"), 0)))));

		return options;
	}

	public static class ButtonWrapper extends snownee.jade.gui.config.KeybindOptionButton.JadeButtonWrapper {
		public ButtonWrapper(JadeButton button, Runnable onClick) {
			super(button, onClick);
		}
	}
}
