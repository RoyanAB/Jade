package snownee.jade.addon.core;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Identifier;
import snownee.jade.Jade;
import snownee.jade.api.*;
import snownee.jade.api.config.TargetBlocklist;
import snownee.jade.impl.BlockAccessorClientHandler;
import snownee.jade.impl.EntityAccessorClientHandler;
import snownee.jade.util.JsonConfig;

import java.util.Arrays;
import java.util.Collections;

@WailaPlugin
public class CorePlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ObjectNameProvider.INSTANCE, BlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerAccessorHandler(BlockAccessor.class, new BlockAccessorClientHandler());
		registration.registerAccessorHandler(EntityAccessor.class, new EntityAccessorClientHandler());

		registration.addConfig(Identifiers.CORE_DISTANCE, false);
		registration.addConfig(Identifiers.CORE_COORDINATES, false);
		registration.addConfig(Identifiers.CORE_REL_COORDINATES, false);

		registration.registerBlockComponent(ObjectNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(ModNameProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(DistanceProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(BlockFaceProvider.INSTANCE, Block.class);

		registration.registerEntityComponent(ObjectNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(ModNameProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(DistanceProvider.INSTANCE, Entity.class);

		registration.markAsClientFeature(Identifiers.CORE_DISTANCE);
		registration.markAsClientFeature(Identifiers.CORE_COORDINATES);
		registration.markAsClientFeature(Identifiers.CORE_REL_COORDINATES);
		registration.markAsClientFeature(Identifiers.CORE_MOD_NAME);
		registration.markAsClientFeature(Identifiers.CORE_BLOCK_FACE);

		for (String id : createBlockBlocklist().get().values) {
			Identifier identifier = new Identifier(id);
			if (Block.REGISTRY.containsKey(identifier)) {
				registration.hideTarget(Block.REGISTRY.get(identifier));
			}
		}
	}

	public static JsonConfig<TargetBlocklist> createEntityBlocklist() {
		return new JsonConfig<>(Jade.MODID + "/hide-entities", TargetBlocklist.class, null, TargetBlocklist::new);
	}

	public static JsonConfig<TargetBlocklist> createBlockBlocklist() {
		return new JsonConfig<>(Jade.MODID + "/hide-blocks", TargetBlocklist.class, null, () -> {
			TargetBlocklist blocklist = new TargetBlocklist();
			blocklist.values = Collections.singletonList("minecraft:barrier");
			return blocklist;
		});
	}
}
