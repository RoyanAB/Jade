package snownee.jade.addon.debug;

import net.minecraft.block.Block;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class DebugPlugin implements IWailaPlugin {

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(Identifiers.DEBUG_REGISTRY_NAME, RegistryNameProvider.Mode.OFF);
		registration.addConfig(Identifiers.DEBUG_SPECIAL_REGISTRY_NAME, false);

		registration.registerBlockComponent(BlockStatesProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(RegistryNameProvider.INSTANCE, Block.class);
		registration.registerEntityComponent(RegistryNameProvider.INSTANCE, Entity.class);

		registration.markAsClientFeature(Identifiers.DEBUG_BLOCK_STATES);
		registration.markAsClientFeature(Identifiers.DEBUG_REGISTRY_NAME);
		registration.markAsClientFeature(Identifiers.DEBUG_SPECIAL_REGISTRY_NAME);

		Text debug = new LiteralText(I18n.translate("config.jade.plugin_jade.debug"));
		registration.setConfigCategoryOverride(Identifiers.DEBUG_BLOCK_STATES, debug);
		registration.setConfigCategoryOverride(Identifiers.DEBUG_REGISTRY_NAME, debug);
	}
}
