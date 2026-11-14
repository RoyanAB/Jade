package snownee.jade.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.callback.*;
import snownee.jade.api.platform.PlatformWailaClientRegistration;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IWailaClientRegistration extends PlatformWailaClientRegistration {

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 */
	void addConfig(Identifier key, boolean defaultValue);

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 *
	 * @param key          the namespaced key
	 * @param defaultValue the default value
	 */
	void addConfig(Identifier key, Enum<?> defaultValue);

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 *
	 * @param key          the namespaced key
	 * @param defaultValue the default value
	 */
	void addConfig(Identifier key, String defaultValue, Predicate<String> validator);

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 *
	 * @param key          the namespaced key
	 * @param defaultValue the default value
	 */
	void addConfig(Identifier key, int defaultValue, int min, int max, boolean slider);

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 *
	 * @param key          the namespaced key
	 * @param defaultValue the default value
	 */
	void addConfig(Identifier key, float defaultValue, float min, float max, boolean slider);

	void addConfigListener(Identifier key, Consumer<Identifier> listener);

	void registerItemStorageClient(IClientExtensionProvider<ItemStack, ItemView> provider);

	void setConfigCategoryOverride(Identifier key, Text override);

	void setConfigCategoryOverride(Identifier key, List<Text> override);

	/**
	 * Register an {@link IJadeProvider} instance to allow overriding the icon for a block via the
	 * {@link IBlockComponentProvider#getIcon(BlockAccessor, IPluginConfig, IElement)} method.
	 *
	 * @param provider The data provider instance
	 * @param block    The highest level class to apply to
	 */
	void registerBlockIcon(IBlockComponentProvider provider, Class<? extends Block> block);

	/**
	 * Register an {@link IJadeProvider} instance for appending informations to
	 * the tooltip.
	 *
	 * @param provider The data provider instance
	 * @param block    The highest level class to apply to
	 */
	void registerBlockComponent(IBlockComponentProvider provider, Class<? extends Block> block);

	/**
	 * Register an {@link IEntityComponentProvider} instance to allow overriding the icon for a entity via the
	 * {@link IEntityComponentProvider#getIcon(EntityAccessor, IPluginConfig, IElement)} method.
	 *
	 * @param provider The data provider instance
	 * @param entity   The highest level class to apply to
	 */
	void registerEntityIcon(IEntityComponentProvider provider, Class<? extends Entity> entity);

	/**
	 * Register an {@link IEntityComponentProvider} instance for appending {@link net.minecraft.network.chat.Component}
	 * to the tooltip.
	 *
	 * @param provider The data provider instance
	 * @param entity   The highest level class to apply to
	 */
	void registerEntityComponent(IEntityComponentProvider provider, Class<? extends Entity> entity);

	/**
	 * Mark a block as hidden in tooltip.
	 */
	void hideTarget(Block block);

	/**
	 * Mark a block as hidden in tooltip.
	 */
	void hideTarget(Class<? extends Entity> entityType);

	/**
	 * Mark a block to show name of the picked result, rather than block name.
	 */
	void usePickedResult(Block block);

	/**
	 * Mark a block to show name of the picked result, rather than block name.
	 */
	void usePickedResult(Class<? extends Entity> entityType);

	BlockAccessor.Builder blockAccessor();

	EntityAccessor.Builder entityAccessor();

	boolean shouldHide(Entity target);

	boolean shouldHide(BlockState state);

	boolean shouldPick(Entity entity);

	boolean shouldPick(BlockState blockState);

	default void addAfterRenderCallback(JadeAfterRenderCallback callback) {
		addAfterRenderCallback(0, callback);
	}

	void addAfterRenderCallback(int priority, JadeAfterRenderCallback callback);

	default void addBeforeRenderCallback(JadeBeforeRenderCallback callback) {
		addBeforeRenderCallback(0, callback);
	}

	void addBeforeRenderCallback(int priority, JadeBeforeRenderCallback callback);

	default void addRayTraceCallback(JadeRayTraceCallback callback) {
		addRayTraceCallback(0, callback);
	}

	void addRayTraceCallback(int priority, JadeRayTraceCallback callback);

	default void addTooltipCollectedCallback(JadeTooltipCollectedCallback callback) {
		addTooltipCollectedCallback(0, callback);
	}

	void addTooltipCollectedCallback(int priority, JadeTooltipCollectedCallback callback);

	default void addItemModNameCallback(JadeItemModNameCallback callback) {
		addItemModNameCallback(0, callback);
	}

	void addItemModNameCallback(int priority, JadeItemModNameCallback callback);

	default void addBeforeTooltipCollectCallback(JadeBeforeTooltipCollectCallback callback) {
		addBeforeTooltipCollectCallback(0, callback);
	}

	void addBeforeTooltipCollectCallback(int priority, JadeBeforeTooltipCollectCallback callback);

	@Deprecated
	Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable String namespace);

	Screen createPluginConfigScreen(@Nullable Screen parent, @Nullable Text jumpToCategory);

	boolean isServerConnected();

	boolean isShowDetailsPressed();

	boolean maybeLowVisionUser();

	NbtCompound getServerData();

	void setServerData(NbtCompound tag);

	ItemStack getBlockCamouflage(World level, BlockPos pos);

	void markAsClientFeature(Identifier uid);

	void markAsServerFeature(Identifier uid);

	boolean isClientFeature(Identifier uid);

	<T extends Accessor<?>> void registerAccessorHandler(Class<T> clazz, AccessorClientHandler<T> handler);

	AccessorClientHandler<Accessor<?>> getAccessorHandler(Class<? extends Accessor<?>> clazz);

}
