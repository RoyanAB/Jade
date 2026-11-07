package snownee.jade.impl;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.jade.api.Accessor;
import snownee.jade.api.AccessorClientHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IToggleableProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.callback.JadeAfterRenderCallback;
import snownee.jade.api.callback.JadeBeforeRenderCallback;
import snownee.jade.api.callback.JadeBeforeTooltipCollectCallback;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.platform.CustomEnchantPower;
import snownee.jade.gui.PluginsConfigScreen;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.entry.BooleanConfigEntry;
import snownee.jade.impl.config.entry.EnumConfigEntry;
import snownee.jade.impl.config.entry.FloatConfigEntry;
import snownee.jade.impl.config.entry.IntConfigEntry;
import snownee.jade.impl.config.entry.StringConfigEntry;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.ModIdentification;

public class WailaClientRegistration implements IWailaClientRegistration {

	private static final WailaClientRegistration INSTANCE = new WailaClientRegistration();

	public final HierarchyLookup<IBlockComponentProvider> blockIconProviders;
	public final HierarchyLookup<IBlockComponentProvider> blockComponentProviders;

	public final HierarchyLookup<IEntityComponentProvider> entityIconProviders;
	public final HierarchyLookup<IEntityComponentProvider> entityComponentProviders;

	public final Set<Block> hideBlocks = Sets.newHashSet();
	public final Set<Class<? extends Entity>> hideEntities = Sets.newHashSet();
	public final Set<Block> pickBlocks = Sets.newHashSet();
	public final Set<Class<? extends Entity>> pickEntities = Sets.newHashSet();

	public final CallbackContainer<JadeAfterRenderCallback> afterRenderCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeBeforeRenderCallback> beforeRenderCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeRayTraceCallback> rayTraceCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeTooltipCollectedCallback> tooltipCollectedCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeItemModNameCallback> itemModNameCallback = new CallbackContainer<>();
	public final CallbackContainer<JadeBeforeTooltipCollectCallback> beforeTooltipCollectCallback = new CallbackContainer<>();

	public final Map<Block, CustomEnchantPower> customEnchantPowers = Maps.newHashMap();
	public final Set<Identifier> clientFeatures = Sets.newHashSet();

	public final Map<Class<Accessor<?>>, AccessorClientHandler<Accessor<?>>> accessorHandlers = new IdentityHashMap<>();

	WailaClientRegistration() {
		blockIconProviders = new HierarchyLookup<>(Block.class);
		blockComponentProviders = new HierarchyLookup<>(Block.class);

		entityIconProviders = new HierarchyLookup<>(Entity.class);
		entityComponentProviders = new HierarchyLookup<>(Entity.class);
	}

	public static WailaClientRegistration instance() {
		return INSTANCE;
	}

	@Override
	public void registerBlockIcon(IBlockComponentProvider provider, Class<? extends Block> block) {
		blockIconProviders.register(block, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerBlockComponent(IBlockComponentProvider provider, Class<? extends Block> block) {
		blockComponentProviders.register(block, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerEntityIcon(IEntityComponentProvider provider, Class<? extends Entity> entity) {
		entityIconProviders.register(entity, provider);
		tryAddConfig(provider);
	}

	@Override
	public void registerEntityComponent(IEntityComponentProvider provider, Class<? extends Entity> entity) {
		entityComponentProviders.register(entity, provider);
		tryAddConfig(provider);
	}

	public List<IBlockComponentProvider> getBlockProviders(Block block, Predicate<IBlockComponentProvider> filter) {
		return blockComponentProviders.get(block).stream().filter(filter).collect(Collectors.toList());
	}

	public List<IBlockComponentProvider> getBlockIconProviders(Block block, Predicate<IBlockComponentProvider> filter) {
		return blockIconProviders.get(block).stream().filter(filter).collect(Collectors.toList());
	}

	public List<IEntityComponentProvider> getEntityProviders(Entity entity, Predicate<IEntityComponentProvider> filter) {
		return entityComponentProviders.get(entity).stream().filter(filter).collect(Collectors.toList());
	}

	public List<IEntityComponentProvider> getEntityIconProviders(Entity entity, Predicate<IEntityComponentProvider> filter) {
		return entityIconProviders.get(entity).stream().filter(filter).collect(Collectors.toList());
	}

	@Override
	public void hideTarget(Block block) {
		Objects.requireNonNull(block);
		hideBlocks.add(block);
	}

	@Override
	public void hideTarget(Class<? extends Entity> entityType) {
		Objects.requireNonNull(entityType);
		hideEntities.add(entityType);
	}

	@Override
	public void usePickedResult(Block block) {
		Objects.requireNonNull(block);
		pickBlocks.add(block);
	}

	@Override
	public void usePickedResult(Class<? extends Entity> entityType) {
		Objects.requireNonNull(entityType);
		pickEntities.add(entityType);
	}

	@Override
	public boolean shouldHide(BlockState state) {
		return hideBlocks.contains(state.getBlock());
	}

	@Override
	public boolean shouldPick(BlockState state) {
		return pickBlocks.contains(state.getBlock());
	}

	@Override
	public boolean shouldHide(Entity entity) {
		for (Class<? extends Entity> clazz : hideEntities) {
			if (clazz.isInstance(entity)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean shouldPick(Entity entity) {
		for (Class<? extends Entity> clazz : pickEntities) {
			if (clazz.isInstance(entity)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void addConfig(Identifier key, boolean defaultValue) {
		PluginConfig.INSTANCE.addConfig(new BooleanConfigEntry(key, defaultValue));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void addConfig(Identifier key, Enum<?> defaultValue) {
		Objects.requireNonNull(defaultValue);
		PluginConfig.INSTANCE.addConfig(new EnumConfigEntry(key, defaultValue));
	}

	@Override
	public void addConfig(Identifier key, String defaultValue, Predicate<String> validator) {
		Objects.requireNonNull(defaultValue);
		Objects.requireNonNull(validator);
		PluginConfig.INSTANCE.addConfig(new StringConfigEntry(key, defaultValue, validator));
	}

	@Override
	public void addConfig(Identifier key, int defaultValue, int min, int max, boolean slider) {
		PluginConfig.INSTANCE.addConfig(new IntConfigEntry(key, defaultValue, min, max, slider));
	}

	@Override
	public void addConfig(Identifier key, float defaultValue, float min, float max, boolean slider) {
		PluginConfig.INSTANCE.addConfig(new FloatConfigEntry(key, defaultValue, min, max, slider));
	}

	@Override
	public void addConfigListener(Identifier key, Consumer<Identifier> listener) {
		PluginConfig.INSTANCE.addConfigListener(key, listener);
	}

	@Override
	public void setConfigCategoryOverride(Identifier key, Text override) {
		PluginConfig.INSTANCE.setCategoryOverride(key, override);
	}

	@Override
	public void setConfigCategoryOverride(Identifier key, List<Text> override) {
		PluginConfig.INSTANCE.setCategoryOverride(key, override);
	}

	private void tryAddConfig(IToggleableProvider provider) {
		if (!provider.isRequired() && !PluginConfig.INSTANCE.containsKey(provider.getUid())) {
			addConfig(provider.getUid(), provider.enabledByDefault());
		}
	}

	public void loadComplete() {
		PriorityStore<Identifier, snownee.jade.api.IJadeProvider> priorities = WailaCommonRegistration.instance().priorities;
		blockComponentProviders.loadComplete(priorities);
		blockIconProviders.loadComplete(priorities);
		entityComponentProviders.loadComplete(priorities);
		entityIconProviders.loadComplete(priorities);
		Stream.of(afterRenderCallback, beforeRenderCallback, rayTraceCallback, tooltipCollectedCallback, itemModNameCallback, beforeTooltipCollectCallback).forEach(CallbackContainer::sort);
	}

	@Override
	public void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback) {
		afterRenderCallback.add(priority, callback);
	}

	@Override
	public void addBeforeRenderCallback(int priority, JadeBeforeRenderCallback callback) {
		beforeRenderCallback.add(priority, callback);
	}

	@Override
	public void addRayTraceCallback(int priority, JadeRayTraceCallback callback) {
		rayTraceCallback.add(priority, callback);
	}

	@Override
	public void addTooltipCollectedCallback(int priority, JadeTooltipCollectedCallback callback) {
		tooltipCollectedCallback.add(priority, callback);
	}

	@Override
	public void addItemModNameCallback(int priority, JadeItemModNameCallback callback) {
		itemModNameCallback.add(priority, callback);
	}

	@Override
	public void addBeforeTooltipCollectCallback(int priority, JadeBeforeTooltipCollectCallback callback) {
		beforeTooltipCollectCallback.add(priority, callback);
	}

	@Override
	public BlockAccessor.Builder blockAccessor() {
		Minecraft mc = Minecraft.getInstance();
		return new BlockAccessorImpl.Builder()
				.level(mc.world)
				.player(mc.player)
				.serverConnected(isServerConnected())
				.serverData(getServerData())
				.showDetails(isShowDetailsPressed());
	}

	@Override
	public EntityAccessor.Builder entityAccessor() {
		Minecraft mc = Minecraft.getInstance();
		return new EntityAccessorImpl.Builder()
				.level(mc.world)
				.player(mc.player)
				.serverConnected(isServerConnected())
				.serverData(getServerData())
				.showDetails(isShowDetailsPressed());
	}

	@Override
	public void registerCustomEnchantPower(Block block, CustomEnchantPower customEnchantPower) {
		customEnchantPowers.put(block, customEnchantPower);
	}

	@Deprecated
	@Override
	public Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable String namespace) {
		return createPluginConfigScreen(parent, ModIdentification.getModName(namespace).map(LiteralText::new).orElse(null));
	}

	@Override
	public Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable Text jumpToCategory) {
		Function<OptionsList, OptionsList.Entry> jumpTo = null;
		if (jumpToCategory != null) {
			String title = jumpToCategory.getString();
			jumpTo = options -> {
				for (OptionsList.Entry entry : options.children()) {
					if (entry instanceof OptionsList.Title && ((OptionsList.Title) entry).getTitle().getString().equals(title)) {
						return entry;
					}
				}
				return null;
			};
		}
		return PluginsConfigScreen.createPluginConfigScreen(parent, jumpTo, false);
	}

	@Override
	public boolean isServerConnected() {
		return ObjectDataCenter.serverConnected;
	}

	@Override
	public boolean isShowDetailsPressed() {
		return ClientProxy.isShowDetailsPressed();
	}

	@Override
	public NbtCompound getServerData() {
		return ObjectDataCenter.getServerData();
	}

	@Override
	public void setServerData(NbtCompound tag) {
		ObjectDataCenter.setServerData(tag);
	}

	@Override
	public ItemStack getBlockCamouflage(World level, BlockPos pos) {
		return ItemStack.EMPTY;
	}

	@Override
	public void markAsClientFeature(Identifier uid) {
		clientFeatures.add(uid);
	}

	@Override
	public void markAsServerFeature(Identifier uid) {
		clientFeatures.remove(uid);
	}

	@Override
	public boolean isClientFeature(Identifier uid) {
		return clientFeatures.contains(uid);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Accessor<?>> void registerAccessorHandler(Class<T> clazz, AccessorClientHandler<T> handler) {
		accessorHandlers.put((Class<Accessor<?>>) clazz, (AccessorClientHandler<Accessor<?>>) handler);
	}

	@Override
	public AccessorClientHandler<Accessor<?>> getAccessorHandler(Class<? extends Accessor<?>> clazz) {
		return Objects.requireNonNull(accessorHandlers.get(clazz));
	}

	@Override
	public boolean maybeLowVisionUser() {
		return ClientProxy.maybeLowVisionUser || IWailaConfig.get().getGeneral().shouldEnableTextToSpeech();
	}

}
