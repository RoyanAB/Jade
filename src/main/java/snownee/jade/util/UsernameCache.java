package snownee.jade.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Jade;
import snownee.jade.api.Identifiers;
import snownee.jade.impl.config.PluginConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public final class UsernameCache {

	public static final String DUMMY_NAME = "???";

	private static final int CACHE_SIZE = 1024;
	private static final Object2ObjectLinkedOpenHashMap<UUID, String> map = new Object2ObjectLinkedOpenHashMap<>(CACHE_SIZE);
	private static final Set<UUID> downloadingList = Collections.synchronizedSet(new HashSet<>());
	private static final Set<UUID> failedList = Collections.synchronizedSet(new HashSet<>());

	private static final Path saveFile = new File(CommonProxy.getConfigDirectory(), Jade.MODID + "/usernamecache.json").toPath();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static boolean loading = false;

	private UsernameCache() {
	}

	/**
	 * Set a player's current username
	 *
	 * @param uuid     the player's {@link java.util.UUID UUID}
	 * @param username the player's username
	 */
	public static void setUsername(UUID uuid, String username) {
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(username);

		if (!isValidName(username))
			return;

		if (!map.containsKey(uuid) && map.size() >= CACHE_SIZE) {
			map.remove(map.firstKey());
		}

		String prev = map.put(uuid, username);
		if (!loading && !Objects.equals(prev, username)) {
			save();
		}
	}

	public static boolean isValidName(String name) {
		return !name.isEmpty() && !name.contains("§");
	}

	/**
	 * Remove a player's username from the cache
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return if the cache contained the user
	 */
	public static boolean removeUsername(UUID uuid) {
		Objects.requireNonNull(uuid);

		if (map.remove(uuid) != null) {
			save();
			return true;
		}

		return false;
	}

	/**
	 * Get the player's last known username
	 * <p>
	 * <b>May be <code>null</code></b>
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return the player's last known username, or <code>null</code> if the
	 * cache doesn't have a record of the last username
	 */
	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		Objects.requireNonNull(uuid);
		String name = map.get(uuid);
		if (name == null && CommonProxy.isPhysicallyClient() && PluginConfig.INSTANCE.get(Identifiers.MC_ANIMAL_OWNER_FETCH_NAMES)) {
			download(uuid);
		}
		return name;
	}

	/**
	 * Get a player's name for display, asking for it when it is not cached yet.
	 * <p>
	 * This never blocks. The lookup runs on its own thread, so the caller gets
	 * <code>null</code> until it finishes and can simply leave the line out for now.
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return the cached name, {@code "???"} once the lookup has failed, or
	 * <code>null</code> while the lookup is still running
	 */
	@Nullable
	public static String lookupUsername(UUID uuid) {
		String name = getLastKnownUsername(uuid);
		if (name == null && failedList.contains(uuid)) {
			return DUMMY_NAME;
		}
		return name;
	}

	/**
	 * Check if the cache contains the given player's username
	 *
	 * @param uuid the player's {@link java.util.UUID UUID}
	 * @return if the cache contains a username for the given player
	 */
	public static boolean containsUUID(UUID uuid) {
		Objects.requireNonNull(uuid);
		return map.containsKey(uuid);
	}

	/**
	 * Get an immutable copy of the cache's underlying map
	 *
	 * @return the map
	 */
	public static Map<UUID, String> getMap() {
		return ImmutableMap.copyOf(map);
	}

	/**
	 * Save the cache to file
	 */
	public static void save() {
		try {
			new SaveThread(gson.toJson(map)).start();
		} catch (Exception e) {
			Jade.LOGGER.error("Failed to save username cache to file!", e);
		}
	}

	/**
	 * Load the cache from file
	 */
	public static void load() {
		if (!Files.exists(saveFile))
			return;

		loading = true;
		try (final BufferedReader reader = Files.newBufferedReader(saveFile, StandardCharsets.UTF_8)) {
			@SuppressWarnings("serial")
			Type type = new TypeToken<Map<UUID, String>>() {
			}.getType();
			Map<UUID, String> tempMap = gson.fromJson(reader, type);
			if (tempMap != null) {
				map.clear();
				tempMap.forEach(UsernameCache::setUsername);
			}
		} catch (Exception e) {
			Jade.LOGGER.error("Could not parse username cache file as valid json, deleting file {}", saveFile, e);
			try {
				Files.delete(saveFile);
			} catch (IOException e1) {
				Jade.LOGGER.error("Could not delete file {}", saveFile, e1);
			}
		} finally {
			loading = false;
		}
	}

	/**
	 * Downloads a Username
	 * This function can be called repeatedly
	 * It should only attempt one Download
	 */
	private static void download(UUID uuid) {
		if (!downloadingList.add(uuid)) {
			return;
		}
		new DownloadThread(uuid).start();
	}

	/**
	 * Downloads GameProfile by UUID then saves them to disk
	 * representation of the cache to disk
	 */
	private static class DownloadThread extends Thread {
		private final UUID uuid;

		public DownloadThread(UUID uuid) {
			this.uuid = uuid;
		}

		@Override
		public void run() {
			try {
				GameProfile profile = Minecraft.getInstance().getSessionService().fillProfileProperties(new GameProfile(uuid, null), true);
				String name = profile == null ? null : profile.getName();
				if (name == null || name.equals(DUMMY_NAME) || !isValidName(name)) {
					//remember the failure so the tooltip can show "???" instead of nothing
					failedList.add(uuid);
					return;
				}
				//only leave the list on success, a failed lookup stays in it so it is never retried.
				//cache under the uuid we were asked about: setUsername drops invalid names, and then
				//removing the entry here would start a new thread on every frame
				setUsername(uuid, name);
				downloadingList.remove(uuid);
			} catch (Exception e) {
				failedList.add(uuid);
			}
		}
	}

	/**
	 * Used for saving the {@link com.google.gson.Gson#toJson(Object) Gson}
	 * representation of the cache to disk
	 */
	private static class SaveThread extends Thread {

		/**
		 * The data that will be saved to disk
		 */
		private final String data;

		public SaveThread(String data) {
			this.data = data;
		}

		@Override
		public void run() {
			Path temp = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
			try {
				synchronized (saveFile) {
					Path parent = saveFile.getParent();
					if (parent != null) {
						Files.createDirectories(parent);
					}
					Files.write(temp, data.getBytes(StandardCharsets.UTF_8));
					try {
						Files.move(temp, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
					} catch (AtomicMoveNotSupportedException e) {
						Files.move(temp, saveFile, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			} catch (Throwable e) {
				Jade.LOGGER.error("Failed to save username cache to file!", e);
				try {
					Files.deleteIfExists(temp);
				} catch (Throwable ignored) {
				}
			}
		}
	}
}
