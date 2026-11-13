package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.mob.monster.ZombieVillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.TranslatableText;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.mixin.ZombieVillagerEntityAccess;

public enum ZombieVillagerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("ConversionTime")) {
			return;
		}
		int time = accessor.getServerData().getInt("ConversionTime");
		if (time > 0) {
			tooltip.add(new TranslatableText("jade.zombieConversion.time", IThemeHelper.get().seconds(time)));
		}
	}

	@Override
	public void appendServerData(NbtCompound tag, EntityAccessor accessor) {
		ZombieVillagerEntity entity = (ZombieVillagerEntity) accessor.getEntity();
		//port: 1.12 keeps the conversion time in a private field, exposed through an accessor mixin
		int conversionTime = ((ZombieVillagerEntityAccess) entity).jade$getConversionTicks();
		if (entity.isConverting() && conversionTime > 0) {
			tag.putInt("ConversionTime", conversionTime);
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ZOMBIE_VILLAGER;
	}

}
