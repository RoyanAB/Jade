package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.mob.passive.animal.AnimalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
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
			tooltip.add(new LiteralText(net.minecraft.client.resource.language.I18n.translate("jade.mobbreeding.time",
					IThemeHelper.get().seconds(time).getFormattedString())));
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
