package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.ArmorStandEntity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.resource.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.impl.ui.HealthElement;

public enum EntityHealthProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (accessor.getEntity() instanceof ArmorStandEntity) {
			return;
		}
		LivingEntity living = (LivingEntity) accessor.getEntity();
		tooltip.add(new HealthElement(living.getMaxHealth(), living.getHealth()));
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ENTITY_HEALTH;
	}

	@Override
	public int getDefaultPriority() {
		return -4501;
	}
}
