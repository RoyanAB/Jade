package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.LivingEntity;
import net.minecraft.resource.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.ArmorElement;

public enum EntityArmorProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		LivingEntity living = (LivingEntity) accessor.getEntity();
		float armor = living.getArmorProtection();
		if (armor == 0) {
			return;
		}
		tooltip.add(new ArmorElement(armor));
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ENTITY_ARMOR;
	}

	@Override
	public int getDefaultPriority() {
		return -4500;
	}
}
