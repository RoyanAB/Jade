package snownee.jade.addon.vanilla;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.passive.animal.tameable.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.CommonProxy;

import java.util.UUID;

public enum AnimalOwnerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		String name;
		if (accessor.getServerData().contains("OwnerName")) {
			name = accessor.getServerData().getString("OwnerName");
		} else {
			Entity entity = accessor.getEntity();
			UUID ownerUUID = entity instanceof TameableEntity ? ((TameableEntity) entity).getOwnerUuid() : null;
			if (ownerUUID == null) {
				return;
			}
			name = CommonProxy.getLastKnownUsername(ownerUUID);
			if (name == null) {
				name = "???";
			}
		}
		tooltip.add(new LiteralText(I18n.translate("jade.owner", name)));
	}

	@Override
	public void appendServerData(NbtCompound data, EntityAccessor accessor) {
		MinecraftServer server = accessor.getLevel().getServer();
		Entity entity = accessor.getEntity();
		if (server != null && server.isSingleplayer() && entity instanceof TameableEntity) {
			return;
		}
		UUID ownerUUID = entity instanceof TameableEntity ? ((TameableEntity) entity).getOwnerUuid() : null;
		if (ownerUUID != null) {
			String name = CommonProxy.getLastKnownUsername(ownerUUID);
			if (name != null) {
				data.putString("OwnerName", name);
			}
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ANIMAL_OWNER;
	}
}
