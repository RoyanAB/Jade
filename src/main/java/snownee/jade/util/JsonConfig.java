package snownee.jade.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Jade;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.function.Supplier;

public class JsonConfig<T> {

	public static final Gson DEFAULT_GSON = new GsonBuilder()
		.setPrettyPrinting()
		.serializeNulls()
		.enableComplexMapKeySerialization()
		.registerTypeAdapter(Identifier.class, new Identifier.Serializer())
		.registerTypeAdapter(Style.class, new Style.Serializer())
		.setLenient()
		.create();

	private final File configFile;
	private final CachedSupplier<T> configGetter;
	private Gson gson = DEFAULT_GSON;

	public JsonConfig(String fileName, Type configClass, @Nullable Runnable onUpdate, Supplier<T> defaultFactory) {
		this.configFile = new File(CommonProxy.getConfigDirectory(), fileName + (fileName.endsWith(".json") ? "" : ".json"));
		this.configGetter = new CachedSupplier<>(() -> {
			if (!configFile.exists()) {
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
			try (BufferedReader reader = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
				T ret = gson.fromJson(reader, configClass);
				if (ret == null) {
					ret = defaultFactory.get();
					write(ret, false);
				}
				return ret;
			} catch (Throwable e) {
				Jade.LOGGER.error("Failed to read config file {}", configFile, e);
				backUpInvalid(configFile);
				T def = defaultFactory.get();
				write(def, false);
				return def;
			}
		});
		configGetter.onUpdate = onUpdate;
		mkdirs(configFile);
	}

	public JsonConfig(String fileName, Class<T> configClass, @Nullable Runnable onUpdate) {
		this(fileName, configClass, onUpdate, () -> {
			try {
				return configClass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Failed to create new config instance", e);
			}
		});
	}

	public JsonConfig<T> withGson(Gson gson) {
		this.gson = gson;
		return this;
	}

	public T get() {
		return configGetter.get();
	}

	public void save() {
		write(get(), false);
	}

	public void write(T t, boolean invalidate) {
		mkdirs(configFile);
		Path target = configFile.toPath();
		Path temp = target.resolveSibling(configFile.getName() + ".tmp");

		try {
			String json = gson.toJson(t);
			Files.write(temp, json.getBytes(StandardCharsets.UTF_8));
			try {
				Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
			}
			if (invalidate)
				invalidate();
		} catch (Throwable e) {
			Jade.LOGGER.error("Failed to write config file {}", configFile, e);
			try {
				Files.deleteIfExists(temp);
			} catch (Throwable ignored) {
			}
		}
	}

	/**
	 * Move a broken config file aside so it is not silently lost when the defaults are written back
	 */
	public static void backUpInvalid(File file) {
		if (file.length() <= 0) {
			return;
		}
		try {
			Files.move(file.toPath(),
				file.toPath().resolveSibling(file.getName() + ".invalid"),
				StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception backupError) {
			Jade.LOGGER.error("Failed to back up config file {}", file, backupError);
		}
	}

	private void mkdirs(File file) {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
	}

	public void invalidate() {
		configGetter.invalidate();
	}

	public File getFile() {
		return configFile;
	}

	static class CachedSupplier<T> {

		private final Supplier<T> supplier;
		private volatile T value;
		private Runnable onUpdate;

		public CachedSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		public T get() {
			T v = value;
			if (v == null) {
				synchronized (this) {
					v = value;
					if (v == null) {
						v = supplier.get();
						Objects.requireNonNull(v);
						if (onUpdate != null) {
							onUpdate.run();
						}
						value = v;
					}
				}
			}
			return v;
		}

		public void invalidate() {
			this.value = null;
		}
	}
}
