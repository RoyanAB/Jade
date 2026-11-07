package snownee.jade.addon.debug;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.util.CommonProxy;

public enum RegistryNameProvider implements IBlockComponentProvider, IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (append(tooltip, CommonProxy.getId(accessor.getBlock()).toString(), config) && config.get(Identifiers.DEBUG_SPECIAL_REGISTRY_NAME)) {
			BlockEntity blockEntity = accessor.getBlockEntity();
			if (blockEntity != null) {
				Identifier id = CommonProxy.getId(blockEntity);
				String s = I18n.translate("config.jade.plugin_jade.registry_name.special.block_entity_type", id.toString());
				tooltip.add(IWailaConfig.get().getFormatting().registryName(s), Identifiers.DEBUG_SPECIAL_REGISTRY_NAME);
			}
			// 1.12.2 has no vanilla FluidState registry; the fluid special id line is 1.13+ only
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		append(tooltip, CommonProxy.getId(accessor.getEntity()).toString(), config);
	}

	public boolean append(ITooltip tooltip, String id, IPluginConfig config) {
		Mode mode = config.getEnum(Identifiers.DEBUG_REGISTRY_NAME);
		if (mode == Mode.OFF)
			return false;
		if (mode == Mode.ADVANCED_TOOLTIPS && !Minecraft.getInstance().options.advancedItemTooltips)
			return false;
		tooltip.add(IWailaConfig.get().getFormatting().registryName(id));
		return true;
	}

	@Override
	public Identifier getUid() {
		return Identifiers.DEBUG_REGISTRY_NAME;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD + 100;
	}

	public enum Mode {
		ON, OFF, ADVANCED_TOOLTIPS
	}
}
