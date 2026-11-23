package snownee.jade.addon.vanilla;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.passive.animal.HorseBaseEntity;
import net.minecraft.entity.living.mob.passive.animal.tameable.Tameable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.UsernameCache;

import java.util.UUID;

public enum AnimalOwnerProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	@Nullable
	public static UUID getOwnerUUID(Entity entity) {
		if (entity instanceof Tameable) {
			return ((Tameable) entity).getOwnerUuid();
		}
		if (entity instanceof HorseBaseEntity) {
			return ((HorseBaseEntity) entity).getOwnerUuid();
		}
		return null;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("OwnerName")) {
			tooltip.add(new TranslatableText("jade.owner", accessor.getServerData().getString("OwnerName")));
			return;
		}
		UUID ownerUUID = getOwnerUUID(accessor.getEntity());
		if (ownerUUID == null) {
			return;
		}
		String name = UsernameCache.lookupUsername(ownerUUID);
		if (name == null) {
			return;
		}
		tooltip.add(new TranslatableText("jade.owner", name));
	}

	@Override
	public void appendServerData(NbtCompound data, EntityAccessor accessor) {
		UUID ownerUUID = getOwnerUUID(accessor.getEntity());
		if (ownerUUID == null) {
			return;
		}
		String name = UsernameCache.getLastKnownUsername(ownerUUID);
		if (name == null) {
			MinecraftServer server = accessor.getLevel().getServer();
			if (server != null) {
				GameProfile profile = server.getGameProfileCache().get(ownerUUID);
				if (profile != null && profile.getName() != null && UsernameCache.isValidName(profile.getName())) {
					name = profile.getName();
					UsernameCache.setUsername(ownerUUID, name);
				}
			}
		}
		if (name != null) {
			data.putString("OwnerName", name);
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ANIMAL_OWNER;
	}
}
