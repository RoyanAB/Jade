package snownee.jade.impl.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.Jade;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.entry.ConfigEntry;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JsonConfig;
import snownee.jade.util.ModIdentification;

public class PluginConfig implements IPluginConfig {

	public static final PluginConfig INSTANCE = new PluginConfig();
	public static final String CLIENT_FILE = Jade.MODID + "/plugins.json";
	public static final String SERVER_FILE = Jade.MODID + "/server-plugin-overrides.json";

	private final Map<Identifier, ConfigEntry<Object>> configs = Maps.newHashMap();
	private final Multimap<Identifier, Text> categoryOverrides = ArrayListMultimap.create();
	private JsonObject serverConfigs;

	private PluginConfig() {
	}

	public static boolean isPrimaryKey(Identifier key) {
		return !key.getPath().contains(".");
	}

	public static Identifier getPrimaryKey(Identifier key) {
		return new Identifier(key.getNamespace(), key.getPath().substring(0, key.getPath().indexOf('.')));
	}

	@SuppressWarnings("unchecked")
	public void addConfig(ConfigEntry<?> entry) {
		Preconditions.checkArgument(StringUtils.countMatches(entry.getId().getPath(), '.') <= 1);
		Preconditions.checkArgument(!containsKey(entry.getId()), "Duplicate config key: %s", entry.getId());
		Preconditions.checkArgument(entry.isValidValue(entry.getDefaultValue()), "Default value of config %s does not pass value check", entry.getId());
		configs.put(entry.getId(), (ConfigEntry<Object>) entry);
	}

	@Override
	public Set<Identifier> getKeys(String namespace) {
		return getKeys().stream().filter(id -> id.getNamespace().equals(namespace)).collect(Collectors.toSet());
	}

	@Override
	public Set<Identifier> getKeys() {
		return configs.keySet();
	}

	@Override
	public boolean get(IToggleableProvider provider) {
		if (provider.isRequired()) {
			return true;
		}
		return get(provider.getUid());
	}

	@Override
	public boolean get(Identifier key) {
		if (CommonProxy.isPhysicallyClient()) {
			return (Boolean) getEntry(key).getValue();
		} else {
			return Optional.ofNullable(serverConfigs)
					.map($ -> $.getAsJsonObject(key.getNamespace()))
					.map($ -> $.get(key.getPath()))
					.map(JsonElement::getAsBoolean)
					.orElse(false);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnum(Identifier key) {
		return (T) getEntry(key).getValue();
	}

	@Override
	public int getInt(Identifier key) {
		return (Integer) getEntry(key).getValue();
	}

	@Override
	public float getFloat(Identifier key) {
		return (Float) getEntry(key).getValue();
	}

	@Override
	public String getString(Identifier key) {
		return (String) getEntry(key).getValue();
	}

	public ConfigEntry<?> getEntry(Identifier key) {
		return configs.get(key);
	}

	public boolean set(Identifier key, Object value) {
		Objects.requireNonNull(value);
		ConfigEntry<?> entry = getEntry(key);
		if (entry == null) {
			Jade.LOGGER.warn("Skip setting value for unknown option: {}, {}", key, value);
			return false;
		}
		if (!entry.isValidValue(value)) {
			Jade.LOGGER.warn("Skip setting illegal value for option: {}, {}", key, value);
			return false;
		}
		entry.setValue(value);
		return true;
	}

	public File getFile() {
		boolean client = CommonProxy.isPhysicallyClient();
		return new File(CommonProxy.getConfigDirectory(), client ? CLIENT_FILE : SERVER_FILE);
	}

	@SuppressWarnings("unchecked")
	public void reload() {
		boolean client = CommonProxy.isPhysicallyClient();
		File configFile = getFile();

		if (client)
			configs.values().forEach($ -> $.setSynced(false));

		if (!configFile.exists()) {
			writeConfig(configFile, true);
		}

		if (client) {
			Map<String, Map<String, Object>> config;
			try (FileReader reader = new FileReader(configFile)) {
				config = JsonConfig.DEFAULT_GSON.fromJson(reader, new TypeToken<Map<String, Map<String, Object>>>() {
				}.getType());
			} catch (Exception e) {
				e.printStackTrace();
				config = Maps.newHashMap();
			}
			if (config == null) {
				config = Maps.newHashMap();
			}

			MutableBoolean saveFlag = new MutableBoolean();
			Set<Identifier> found = Sets.newHashSet();
			config.forEach((namespace, subMap) -> subMap.forEach((path, value) -> {
				Identifier id = new Identifier(namespace, path);
				if (!configs.containsKey(id)) {
					return;
				}
				if (!set(id, value))
					saveFlag.setTrue();
				found.add(id);
			}));

			Set<Identifier> allKeys = getKeys();
			for (Identifier id : allKeys) {
				if (!found.contains(id)) {
					set(id, getEntry(id).getDefaultValue());
					saveFlag.setTrue();
				}
			}

			if (saveFlag.isTrue())
				save();
		} else {
			try (FileReader reader = new FileReader(configFile)) {
				serverConfigs = JsonConfig.DEFAULT_GSON.fromJson(reader, JsonObject.class);
			} catch (Exception e) {
				e.printStackTrace();
				serverConfigs = null;
			}
		}
	}

	public void save() {
		writeConfig(getFile(), false);
	}

	private void writeConfig(File file, boolean reset) {
		boolean client = CommonProxy.isPhysicallyClient();
		String json;
		if (client) {
			Map<String, Map<String, Object>> config = Maps.newHashMap();
			configs.values().forEach(e -> {
				Map<String, Object> modConfig = config.computeIfAbsent(e.getId().getNamespace(), k -> Maps.newHashMap());
				if (reset)
					e.setValue(e.getDefaultValue());
				modConfig.put(e.getId().getPath(), e.getValue());
			});
			json = JsonConfig.DEFAULT_GSON.toJson(config);
		} else {
			json = "{}";
		}
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void applyServerConfigs(JsonObject json) {
		json.keySet().forEach(namespace -> {
			json.getAsJsonObject(namespace).entrySet().forEach(entry -> {
				Identifier key = new Identifier(namespace, entry.getKey());
				ConfigEntry<?> configEntry = getEntry(key);
				if (configEntry != null) {
					JsonPrimitive primitive = entry.getValue().getAsJsonPrimitive();
					Object v;
					if (primitive.isBoolean())
						v = primitive.getAsBoolean();
					else if (primitive.isNumber())
						v = primitive.getAsNumber();
					else if (primitive.isString())
						v = primitive.getAsString();
					else
						return;
					if (configEntry.isValidValue(v)) {
						configEntry.setValue(v);
						configEntry.setSynced(true);
					}
				}
			});
		});
	}

	public String getServerConfigs() {
		return serverConfigs == null ? "" : serverConfigs.toString();
	}

	public boolean containsKey(Identifier uid) {
		return configs.containsKey(uid);
	}

	public void addConfigListener(Identifier key, Consumer<Identifier> listener) {
		Preconditions.checkArgument(containsKey(key));
		configs.get(key).addListener(listener);
	}

	public void setCategoryOverride(Identifier key, Text override) {
		Preconditions.checkArgument(containsKey(key), "Unknown config key: %s", key);
		Preconditions.checkArgument(isPrimaryKey(key), "Only primary config key can be overridden");
		categoryOverrides.put(key, override);
	}

	public void setCategoryOverride(Identifier key, List<Text> overrides) {
		Preconditions.checkArgument(containsKey(key), "Unknown config key: %s", key);
		Preconditions.checkArgument(isPrimaryKey(key), "Only primary config key can be overridden");
		categoryOverrides.putAll(key, overrides);
	}

	public List<Category> getListView() {
		Multimap<String, ConfigEntry<?>> categoryMap = ArrayListMultimap.create();
		categoryOverrides.forEach((key, component) -> categoryMap.put(component.getString(), getEntry(key)));
		configs.forEach((key, entry) -> {
			if (categoryOverrides.containsKey(key)) {
				return;
			}
			if (!isPrimaryKey(key)) {
				Identifier primaryKey = getPrimaryKey(key);
				Collection<Text> components = categoryOverrides.get(primaryKey);
				if (!components.isEmpty()) {
					for (Text component : components) {
						categoryMap.put(component.getString(), entry);
					}
					return;
				}
			}
			String namespace = key.getNamespace();
			Optional<String> modName = ModIdentification.getModName(namespace);
			if (!Jade.MODID.equals(namespace) && modName.isPresent()) {
				categoryMap.put(modName.get(), entry);
			} else {
				categoryMap.put(I18n.translate(OptionsList.Entry.makeKey("plugin_" + namespace)), entry);
			}
		});

		List<Category> list = new ArrayList<>();
		for (Map.Entry<String, Collection<ConfigEntry<?>>> e : categoryMap.asMap().entrySet()) {
			List<ConfigEntry<?>> entries = new ArrayList<>(e.getValue());
			entries.sort(Comparator.comparingInt($ -> WailaCommonRegistration.instance().priorities.getSortedList().indexOf($.getId())));
			list.add(new Category(new LiteralText(e.getKey()), entries));
		}
		list.sort(Comparator.comparingInt(specialOrder()).thenComparing($ -> $.title.getString()));
		return list;
	}

	private static ToIntFunction<Category> specialOrder() {
		String core = I18n.translate(OptionsList.Entry.makeKey("plugin_" + Jade.MODID));
		String debug = I18n.translate(OptionsList.Entry.makeKey("plugin_" + Jade.MODID + ".debug"));
		return category -> {
			String title = category.title.getString();
			if (core.equals(title)) {
				return -1;
			}
			if (debug.equals(title)) {
				return 1;
			}
			return 0;
		};
	}

	public static class Category {
		private final Text title;
		private final List<ConfigEntry<?>> entries;

		public Category(Text title, List<ConfigEntry<?>> entries) {
			this.title = title;
			this.entries = entries;
		}

		public Text title() {
			return title;
		}

		public List<ConfigEntry<?>> entries() {
			return entries;
		}
	}

}
