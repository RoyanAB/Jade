package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.mob.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MobGrowthProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("GrowingTime")) {
			return;
		}
		int time = accessor.getServerData().getInt("GrowingTime");
		if (time > 0) {
			tooltip.add(new LiteralText(net.minecraft.client.resource.language.I18n.translate("jade.mobgrowth.time",
				IThemeHelper.get().seconds(time).getFormattedString())));
		}
	}

	@Override
	public void appendServerData(NbtCompound tag, EntityAccessor accessor) {
		if (accessor.getEntity() instanceof PassiveEntity) {
			int time = -((PassiveEntity) accessor.getEntity()).getBreedingAge();
			if (time > 0) {
				tag.putInt("GrowingTime", time);
			}
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_MOB_GROWTH;
	}
}
