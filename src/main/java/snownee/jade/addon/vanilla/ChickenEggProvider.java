package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.mob.passive.animal.ChickenEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum ChickenEggProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("NextEggIn")) {
			return;
		}
		tooltip.add(new LiteralText(net.minecraft.client.resource.language.I18n.translate("jade.nextEgg",
			IThemeHelper.get().seconds(accessor.getServerData().getInt("NextEggIn")).getFormattedString())));
	}

	@Override
	public void appendServerData(NbtCompound tag, EntityAccessor accessor) {
		ChickenEntity chicken = (ChickenEntity) accessor.getEntity();
		tag.putInt("NextEggIn", chicken.layEggCooldown);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_CHICKEN_EGG;
	}
}
