package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.mob.passive.animal.AnimalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.TranslatableText;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MobBreedingProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("BreedingCD")) {
			return;
		}
		int time = accessor.getServerData().getInt("BreedingCD");
		if (time > 0) {
			tooltip.add(new TranslatableText("jade.mobbreeding.time", IThemeHelper.get().seconds(time)));
		}
	}

	@Override
	public void appendServerData(NbtCompound tag, EntityAccessor accessor) {
		int time = ((AnimalEntity) accessor.getEntity()).getBreedingAge();
		if (time > 0) {
			tag.putInt("BreedingCD", time);
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_MOB_BREEDING;
	}
}
