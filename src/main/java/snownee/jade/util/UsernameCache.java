package snownee.jade.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import snownee.jade.Jade;

public final class UsernameCache {

	private static final HashMap<UUID, String> map = new HashMap<>();
	private static final File saveFile = new File(CommonProxy.getConfigDirectory(), Jade.MODID + "/usernamecache.json");
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private static boolean loading = false;

	private UsernameCache() {
	}

	public static void setUsername(UUID uuid, String username) {
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(username);

		if (username.isEmpty())
			return;

		String prev = map.put(uuid, username);
		if (!loading && !Objects.equals(prev, username)) {
			save();
		}
	}

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return map.get(uuid);
	}

	public static void load() {
		if (!saveFile.exists()) {
			return;
		}
		loading = true;
		try (FileReader reader = new FileReader(saveFile)) {
			Type type = new TypeToken<Map<String, String>>() {
			}.getType();
			Map<String, String> loaded = gson.fromJson(reader, type);
			for (Map.Entry<String, String> entry : loaded.entrySet()) {
				try {
					map.put(UUID.fromString(entry.getKey()), entry.getValue());
				} catch (Exception e) {
					// no
				}
			}
		} catch (Exception e) {
			Jade.LOGGER.error("Failed to load username cache", e);
		} finally {
			loading = false;
		}
	}

	public static void save() {
		try {
			if (!saveFile.getParentFile().exists()) {
				saveFile.getParentFile().mkdirs();
			}
			Map<String, String> data = new HashMap<>();
			map.forEach((uuid, name) -> data.put(uuid.toString(), name));
			try (FileWriter writer = new FileWriter(saveFile)) {
				writer.write(gson.toJson(data));
			}
		} catch (Exception e) {
			Jade.LOGGER.error("Failed to save username cache", e);
		}
	}
}
