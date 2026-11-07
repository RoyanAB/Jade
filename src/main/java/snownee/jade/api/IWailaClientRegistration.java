package snownee.jade.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.callback.*;
import snownee.jade.api.platform.PlatformWailaClientRegistration;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IWailaClientRegistration extends PlatformWailaClientRegistration {

	/**
	 * Register a namespaced config key to be accessed within data providers.
	 */
	void addConfig(Identifier key, boolean defaultValue);

	void addConfig(Identifier key, Enum<?> defaultValue);

	void addConfig(Identifier key, String defaultValue, Predicate<String> validator);

	void addConfig(Identifier key, int defaultValue, int min, int max, boolean slider);

	void addConfig(Identifier key, float defaultValue, float min, float max, boolean slider);

	void addConfigListener(Identifier key, Consumer<Identifier> listener);

	void setConfigCategoryOverride(Identifier key, Text override);

	void setConfigCategoryOverride(Identifier key, List<Text> override);

	void registerBlockIcon(IBlockComponentProvider provider, Class<? extends Block> block);

	void registerBlockComponent(IBlockComponentProvider provider, Class<? extends Block> block);

	void registerEntityIcon(IEntityComponentProvider provider, Class<? extends Entity> entity);

	void registerEntityComponent(IEntityComponentProvider provider, Class<? extends Entity> entity);

	void hideTarget(Block block);

	void hideTarget(Class<? extends Entity> entityType);

	void usePickedResult(Block block);

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

	ItemStack getBlockCamouflage(net.minecraft.world.World level, net.minecraft.util.math.BlockPos pos);

	void markAsClientFeature(Identifier uid);

	void markAsServerFeature(Identifier uid);

	boolean isClientFeature(Identifier uid);

	<T extends Accessor<?>> void registerAccessorHandler(Class<T> clazz, AccessorClientHandler<T> handler);

	AccessorClientHandler<Accessor<?>> getAccessorHandler(Class<? extends Accessor<?>> clazz);

}
