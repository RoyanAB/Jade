package snownee.jade.impl;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Identifier;
import snownee.jade.Jade;
import snownee.jade.api.*;
import snownee.jade.impl.config.PluginConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WailaCommonRegistration implements IWailaCommonRegistration {

	private static final WailaCommonRegistration INSTANCE = new WailaCommonRegistration();

	public final HierarchyLookup<IServerDataProvider<BlockAccessor>> blockDataProviders;
	public final HierarchyLookup<IServerDataProvider<EntityAccessor>> entityDataProviders;
	public final PriorityStore<Identifier, IJadeProvider> priorities;

	WailaCommonRegistration() {
		blockDataProviders = new HierarchyLookup<>(BlockEntity.class);
		entityDataProviders = new HierarchyLookup<>(Entity.class);
		priorities = new PriorityStore<>(IJadeProvider::getDefaultPriority, IJadeProvider::getUid);
		priorities.setSortingFunction((store, allKeys) -> {
			List<Identifier> keys = allKeys.stream()
				.filter(PluginConfig::isPrimaryKey)
				.sorted(Comparator.comparingInt(store::byKey))
				.collect(Collectors.toCollection(ArrayList::new));
			allKeys.stream().filter($ -> !PluginConfig.isPrimaryKey($)).forEach($ -> {
				int index = keys.indexOf(PluginConfig.getPrimaryKey($));
				keys.add(index + 1, $);
			});
			return keys;
		});
		priorities.setConfigFile(Jade.MODID + "/sort-order");
	}

	public static WailaCommonRegistration instance() {
		return INSTANCE;
	}

	@Override
	public void registerBlockDataProvider(IServerDataProvider<BlockAccessor> dataProvider, Class<? extends BlockEntity> block) {
		blockDataProviders.register(block, dataProvider);
	}

	@Override
	public void registerEntityDataProvider(IServerDataProvider<EntityAccessor> dataProvider, Class<? extends Entity> entity) {
		entityDataProviders.register(entity, dataProvider);
	}

	public List<IServerDataProvider<BlockAccessor>> getBlockNBTProviders(BlockEntity block) {
		return blockDataProviders.get(block);
	}

	public List<IServerDataProvider<EntityAccessor>> getEntityNBTProviders(Entity entity) {
		return entityDataProviders.get(entity);
	}

	public void loadComplete() {
		blockDataProviders.loadComplete(priorities);
		entityDataProviders.loadComplete(priorities);
	}

}
