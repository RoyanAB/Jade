package snownee.jade.util;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameMode;
import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.client.ClientConnectionEvents;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import net.ornithemc.osl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Jade;
import snownee.jade.JadeClient;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.Identifiers;
import snownee.jade.api.ui.Rect2i;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.EntityAccessorImpl;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.theme.ThemeHelper;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;
import snownee.jade.overlay.WailaTickHandler;

import java.util.Optional;

public final class ClientProxy implements ClientModInitializer {

	private static long lastFrameSampleNanos;
	private static float cachedFrameDelta;

	public static boolean hasJEI = false;
	public static boolean hasREI = false;
	public static boolean hasFastScroll = CommonProxy.isModLoaded("fastscroll");
	public static boolean maybeLowVisionUser = CommonProxy.isModLoaded("minecraft_access");

	public static Optional<String> getModName(String namespace) {
		String modMenuKey = "modmenu.nameTranslation." + namespace;
		if (I18n.hasTranslation(modMenuKey)) {
			return Optional.of(I18n.translate(modMenuKey));
		}
		return FabricLoader.getInstance().getModContainer(namespace)
			.map(ModContainer::getMetadata)
			.map(ModMetadata::getName)
			.filter(name -> !Strings.isNullOrEmpty(name));
	}

	public static void onRenderTick() {
		try {
			OverlayRenderer.renderOverlay478757();
		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	private static void onClientTick(Minecraft mc) {
		try {
			WailaTickHandler.instance().tickClient();
			JadeClient.onKeyPressed(1);

		} catch (Throwable e) {
			WailaExceptionHandler.handleErr(e, null, null);
		}
	}

	public static boolean shouldRegisterRecipeViewerKeys() {
		return hasJEI || hasREI;
	}

	public static void requestBlockData(BlockAccessor accessor) {
		PacketBuffer buf = Networking.buffer();
		new BlockAccessorImpl.SyncData(accessor).write(buf);
		ClientPlayNetworking.send(Networking.channel(Identifiers.PACKET_REQUEST_TILE), buf);
	}

	public static void requestEntityData(EntityAccessor accessor) {
		PacketBuffer buf = Networking.buffer();
		new EntityAccessorImpl.SyncData(accessor).write(buf);
		ClientPlayNetworking.send(Networking.channel(Identifiers.PACKET_REQUEST_ENTITY), buf);
	}

	@Nullable
	public static Rect2i getBossBarRect() {
		Minecraft mc = Minecraft.getInstance();
		if (!(mc.gui.getBossEventOverlay() instanceof snownee.jade.mixin.BossEventOverlayAccess)) {
			return null;
		}
		int size = ((snownee.jade.mixin.BossEventOverlayAccess) mc.gui.getBossEventOverlay()).getBossEvents().size();
		if (size == 0) {
			return null;
		}
		int width = DisplayHelper.guiWidth();
		int height = DisplayHelper.guiHeight();
		int barHeight = 10 + mc.textRenderer.fontHeight;
		size = Math.min(size, (height / 3 - 12) / barHeight + 1);
		return new Rect2i(width / 2 - 91, 12, 182, barHeight * size);
	}

	public static boolean isShowDetailsPressed() {
		return Screen.isShiftDown() || (JadeClient.showDetails != null && org.lwjgl.input.Keyboard.isKeyDown(JadeClient.showDetails.getKeyCode()));
	}

	public static boolean shouldShowWithOverlay(Minecraft mc, @Nullable Screen screen) {
		return screen == null || screen instanceof BaseOptionsScreen || screen instanceof ChatScreen;
	}

	public static GameMode getGameMode() {
		if (Minecraft.getInstance().player == null) {
			return GameMode.SURVIVAL;
		}
		if (Minecraft.getInstance().player.isCreative()) {
			return GameMode.CREATIVE;
		}
		if (Minecraft.getInstance().player.isSpectator()) {
			return GameMode.SPECTATOR;
		}
		return GameMode.SURVIVAL;
	}

	@Override
	public void initClient() {
		KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> {
			JadeClient.openConfig = registry.register("key.jade.config", org.lwjgl.input.Keyboard.KEY_NUMPAD0, "Jade");
			JadeClient.showOverlay = registry.register("key.jade.show_overlay", org.lwjgl.input.Keyboard.KEY_NUMPAD1, "Jade");
			JadeClient.toggleLiquid = registry.register("key.jade.toggle_liquid", org.lwjgl.input.Keyboard.KEY_NUMPAD2, "Jade");
			JadeClient.narrate = registry.register("key.jade.narrate", org.lwjgl.input.Keyboard.KEY_NUMPAD5, "Jade");
			JadeClient.showDetails = registry.register("key.jade.show_details_alternative", org.lwjgl.input.Keyboard.KEY_NONE, "Jade");
		});
		MinecraftClientEvents.READY.register(mc -> {
			CommonProxy.loadComplete();
			ThemeHelper.INSTANCE.reload();
		});
		MinecraftClientEvents.TICK_END.register(ClientProxy::onClientTick);
		ClientConnectionEvents.DISCONNECT.register(context -> ObjectDataCenter.serverConnected = false);

		ClientPlayNetworking.registerListener(Networking.channel(Identifiers.PACKET_RECEIVE_DATA), (ctx, buf) -> {
			ctx.ensureOnMainThread();
			NbtCompound nbt = buf.readNbtCompound();
			ObjectDataCenter.setServerData(nbt);
		});
		ClientPlayNetworking.registerListener(Networking.channel(Identifiers.PACKET_SERVER_PING), (ctx, buf) -> {
			ctx.ensureOnMainThread();
			String s = buf.readString(32767);
			JsonObject json;
			try {
				json = s.isEmpty() ? null : JsonConfig.DEFAULT_GSON.fromJson(s, JsonObject.class);
			} catch (Throwable e) {
				Jade.LOGGER.error("Received malformed config from the server: {}", s);
				return;
			}
			final JsonObject json0 = json;
			ObjectDataCenter.serverConnected = true;
			PluginConfig.INSTANCE.reload();
			if (json0 != null && !json0.keySet().isEmpty())
				PluginConfig.INSTANCE.applyServerConfigs(json0);
			Jade.LOGGER.info("Received config from the server: {}", s);
		});
		ClientPlayNetworking.registerListener(Networking.channel(Identifiers.PACKET_SHOW_OVERLAY), (ctx, buf) -> {
			ctx.ensureOnMainThread();
			boolean show = buf.readBoolean();
			Jade.LOGGER.info("Received request from the server to {} overlay", show ? "show" : "hide");
			Jade.CONFIG.get().getGeneral().setDisplayTooltip(show);
			Jade.CONFIG.save();
		});

		ClientResourceLoaderEvents.END_RESOURCE_RELOAD.register((manager, context) -> {
			ThemeHelper.INSTANCE.reload();
			snownee.jade.addon.harvest.HarvestToolProvider.clearCache();
		});
	}
}
