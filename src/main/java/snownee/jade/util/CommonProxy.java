package snownee.jade.util;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.Entities;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import net.ornithemc.osl.lifecycle.api.server.MinecraftServerEvents;
import net.ornithemc.osl.networking.api.ChannelRegistry;
import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.server.ServerPacketListener;
import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;
import snownee.jade.Jade;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.impl.BlockAccessorImpl;
import snownee.jade.impl.EntityAccessorImpl;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;

public final class CommonProxy implements ModInitializer {

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return UsernameCache.getLastKnownUsername(uuid);
	}

	public static File getConfigDirectory() {
		return FabricLoader.getInstance().getConfigDir().toFile();
	}

	public static boolean isShears(ItemStack tool) {
		return tool.getItem() instanceof ShearsItem;
	}

	public static boolean isShearable(BlockState state) {
		return state.getBlock() == net.minecraft.block.Blocks.LEAVES || state.getBlock() == net.minecraft.block.Blocks.LEAVES2 || state.getBlock() == net.minecraft.block.Blocks.WEB;
	}

	public static boolean isCorrectToolForDrops(BlockState state, net.minecraft.entity.living.player.PlayerEntity player) {
		return state.getBlock().getMaterial(state).isToolNotRequired();
	}

	public static String getModIdFromItem(ItemStack stack) {
		NbtCompound tag = stack.getNbt();
		if (tag != null && tag.contains("id")) {
			String s = tag.getString("id");
			if (s.contains(":")) {
				return new Identifier(s).getNamespace();
			}
		}
		Identifier id = net.minecraft.item.Item.REGISTRY.getKey(stack.getItem());
		return id == null ? "minecraft" : id.getNamespace();
	}

	public static boolean isPhysicallyClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static boolean isDevEnv() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	public static float getEnchantPowerBonus(BlockState state, World world, net.minecraft.util.math.BlockPos pos) {
		if (WailaClientRegistration.instance().customEnchantPowers.containsKey(state.getBlock())) {
			return WailaClientRegistration.instance().customEnchantPowers.get(state.getBlock()).getEnchantPowerBonus(state, world, pos);
		}
		return state.getBlock() == net.minecraft.block.Blocks.BOOKSHELF ? 1 : 0;
	}

	public static Identifier getId(Block block) {
		return Block.REGISTRY.getKey(block);
	}

	public static Identifier getId(Entity entity) {
		Identifier id = Entities.getKey(entity);
		return id == null ? new Identifier("unknown") : id;
	}

	public static Identifier getId(BlockEntity blockEntity) {
		Identifier id = BlockEntity.getKey(blockEntity.getClass());
		return id == null ? new Identifier("unknown") : id;
	}

	public static String getPlatformIdentifier() {
		return "fabric";
	}

	public static boolean isBoss(Entity entity) {
		return entity instanceof net.minecraft.entity.living.mob.monster.boss.WitherEntity || entity instanceof net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity;
	}

	public static ItemStack getBlockPickedResult(BlockState state, net.minecraft.entity.living.player.PlayerEntity player, HitResult hitResult) {
		try {
			return state.getBlock().getPickItem(player.world, hitResult.getPos(), state);
		} catch (Throwable e) {
			return ItemStack.EMPTY;
		}
	}

	public static ItemStack getEntityPickedResult(Entity entity, net.minecraft.entity.living.player.PlayerEntity player, HitResult hitResult) {
		return ItemStack.EMPTY;
	}

	public static boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	public static void loadComplete() {
		if (Jade.FROZEN) {
			return;
		}
		Set<Class<?>> classes = Sets.newHashSet();
		FabricLoader.getInstance().getEntrypointContainers(Jade.MODID, IWailaPlugin.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			Jade.LOGGER.info("Start loading plugin from {}", metadata.getName());
			String className = null;
			try {
				IWailaPlugin plugin = entrypoint.getEntrypoint();
				WailaPlugin a = plugin.getClass().getDeclaredAnnotation(WailaPlugin.class);
				if (a != null && !Strings.isNullOrEmpty(a.value()) && !isModLoaded(a.value())) {
					return;
				}
				className = plugin.getClass().getName();
				if (className.startsWith("snownee.jade.") && !metadata.getId().startsWith(Jade.MODID)) {
					throw new IllegalStateException("Mod " + metadata.getName() + " is not allowed to register built-in plugins. Please contact the mod author");
				}
				if (!classes.add(plugin.getClass())) {
					throw new IllegalStateException("Duplicate plugin class " + className);
				}
				plugin.register(WailaCommonRegistration.instance());
				if (isPhysicallyClient()) {
					plugin.registerClient(WailaClientRegistration.instance());
				}
			} catch (Throwable e) {
				Jade.LOGGER.error("Error loading plugin at {}", className, e);
				Throwables.throwIfInstanceOf(e, IllegalStateException.class);
			}
		});
		Jade.loadComplete();
	}

	public static boolean isMultipartEntity(Entity target) {
		return target instanceof net.minecraft.entity.living.mob.monster.boss.EnderDragonPart;
	}

	public static Entity wrapPartEntityParent(Entity target) {
		if (target instanceof net.minecraft.entity.living.mob.monster.boss.EnderDragonPart) {
			return ((net.minecraft.entity.living.mob.monster.boss.EnderDragonPart) target).dragon instanceof net.minecraft.entity.Entity ? (net.minecraft.entity.Entity) ((net.minecraft.entity.living.mob.monster.boss.EnderDragonPart) target).dragon : target;
		}
		return target;
	}

	public static int getPartEntityIndex(Entity entity) {
		if (!(entity instanceof net.minecraft.entity.living.mob.monster.boss.EnderDragonPart)) {
			return -1;
		}
		net.minecraft.entity.living.mob.monster.boss.EnderDragonPart part = (net.minecraft.entity.living.mob.monster.boss.EnderDragonPart) entity;
		if (!(part.dragon instanceof net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity)) {
			return -1;
		}
		net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity dragon = (net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity) part.dragon;
		for (int i = 0; i < dragon.parts.length; i++) {
			if (dragon.parts[i] == part) {
				return i;
			}
		}
		return -1;
	}

	public static Entity getPartEntity(Entity parent, int index) {
		if (parent == null) {
			return null;
		}
		if (index < 0) {
			return parent;
		}
		if (parent instanceof net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity) {
			net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity dragon = (net.minecraft.entity.living.mob.monster.boss.EnderDragonEntity) parent;
			if (index < dragon.parts.length) {
				return dragon.parts[index];
			}
		}
		return parent;
	}

	public static void openFile(File file) {
		try {
			java.awt.Desktop.getDesktop().open(file);
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to open " + file, e);
		}
	}

	@Override
	public void init() {
		ChannelRegistry.register(Networking.channel(Identifiers.PACKET_REQUEST_ENTITY), false, true);
		ChannelRegistry.register(Networking.channel(Identifiers.PACKET_REQUEST_TILE), false, true);
		ChannelRegistry.register(Networking.channel(Identifiers.PACKET_RECEIVE_DATA), true, false);
		ChannelRegistry.register(Networking.channel(Identifiers.PACKET_SERVER_PING), true, false);
		ChannelRegistry.register(Networking.channel(Identifiers.PACKET_SHOW_OVERLAY), true, false);
		ServerPlayNetworking.registerListener(Networking.channel(Identifiers.PACKET_REQUEST_ENTITY), (ServerPacketListener.Buffer) (ctx, data) -> {
			EntityAccessorImpl.SyncData syncData = new EntityAccessorImpl.SyncData(data);
			ctx.ensureOnMainThread();
			EntityAccessorImpl.handleRequest(syncData, ctx.player(), task -> task.run(), tag -> {
				PacketBuffer buf = Networking.buffer();
				buf.writeNbtCompound(tag);
				ServerPlayNetworking.send(ctx.player(), Networking.channel(Identifiers.PACKET_RECEIVE_DATA), buf);
			});
		});
		ServerPlayNetworking.registerListener(Networking.channel(Identifiers.PACKET_REQUEST_TILE), (ServerPacketListener.Buffer) (ctx, data) -> {
			BlockAccessorImpl.SyncData syncData = new BlockAccessorImpl.SyncData(data);
			ctx.ensureOnMainThread();
			BlockAccessorImpl.handleRequest(syncData, ctx.player(), task -> task.run(), tag -> {
				PacketBuffer buf = Networking.buffer();
				buf.writeNbtCompound(tag);
				ServerPlayNetworking.send(ctx.player(), Networking.channel(Identifiers.PACKET_RECEIVE_DATA), buf);
			});
		});
		UsernameCache.load();
		MinecraftServerEvents.START.register(server -> {
			if (server.isDedicated()) {
				loadComplete();
			}
		});
	}

	@SuppressWarnings("unused")
	private static void sendShowOverlay(MinecraftServer server, Iterable<net.minecraft.server.entity.living.player.ServerPlayerEntity> players, boolean show) {
		for (net.minecraft.server.entity.living.player.ServerPlayerEntity player : players) {
			PacketBuffer buf = Networking.buffer();
			buf.writeBoolean(show);
			ServerPlayNetworking.send(player, Networking.channel(Identifiers.PACKET_SHOW_OVERLAY), buf);
		}
	}
}
