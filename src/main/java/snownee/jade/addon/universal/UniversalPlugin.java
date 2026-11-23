package snownee.jade.addon.universal;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import snownee.jade.api.*;

@WailaPlugin
public class UniversalPlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(ItemStorageProvider.INSTANCE, BlockEntity.class);
		registration.registerEntityDataProvider(EntityItemStorageProvider.INSTANCE, Entity.class);
		registration.registerItemStorage(ItemStorageProvider.INSTANCE, Object.class);

		registration.registerBlockDataProvider(ProgressProvider.INSTANCE, BlockEntity.class);
		registration.registerEntityDataProvider(EntityProgressProvider.INSTANCE, Entity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(Identifiers.MC_ITEM_STORAGE_DETAILED_AMOUNT, 54, 0, 54, false);
		registration.addConfig(Identifiers.MC_ITEM_STORAGE_NORMAL_AMOUNT, 9, 0, 54, false);
		registration.addConfig(Identifiers.MC_ITEM_STORAGE_SHOW_NAME_AMOUNT, 5, 0, 9, true);
		registration.addConfig(Identifiers.MC_ITEM_STORAGE_ITEMS_PER_LINE, 9, 3, 27, true);

		registration.registerBlockComponent(ItemStorageProvider.INSTANCE, Block.class);
		registration.registerEntityComponent(EntityItemStorageProvider.INSTANCE, Entity.class);
		registration.registerItemStorageClient(ItemStorageProvider.INSTANCE);

		registration.registerBlockComponent(ProgressProvider.INSTANCE, Block.class);
		registration.registerEntityComponent(EntityProgressProvider.INSTANCE, Entity.class);

		Text category = new TranslatableText("config.jade.plugin_jade");
		registration.setConfigCategoryOverride(Identifiers.UNIVERSAL_ITEM_STORAGE, category);
	}

}
