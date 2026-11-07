package snownee.jade.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;

public interface IWailaCommonRegistration {

	/**
	 * Register an {@link IServerDataProvider<BlockAccessor>} instance for data syncing purposes.
	 *
	 * @param dataProvider The data provider instance
	 * @param block        The highest level class to apply to
	 */
	void registerBlockDataProvider(IServerDataProvider<BlockAccessor> dataProvider, Class<? extends BlockEntity> block);

	/**
	 * Register an {@link IServerDataProvider<EntityAccessor>} instance for data syncing purposes.
	 *
	 * @param dataProvider The data provider instance
	 * @param entity       The highest level class to apply to
	 */
	void registerEntityDataProvider(IServerDataProvider<EntityAccessor> dataProvider, Class<? extends Entity> entity);

}
