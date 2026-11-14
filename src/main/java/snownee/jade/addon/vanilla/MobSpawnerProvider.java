package snownee.jade.addon.vanilla;

import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.mixin.SpawnerMinecartEntityAccess;

public enum MobSpawnerProvider implements IBlockComponentProvider, IEntityComponentProvider {

	INSTANCE;

	public static void appendTooltip(ITooltip tooltip, Entity displayEntity, Text name) {
		if (displayEntity == null) {
			return;
		}
		Text text = new TranslatableText("jade.spawner", name, displayEntity.getDisplayName());
		tooltip.replace(Identifiers.CORE_OBJECT_NAME, IThemeHelper.get().title(text));
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Text name = new LiteralText(accessor.getBlock().getName());
		if (accessor.getBlockEntity() instanceof MobSpawnerBlockEntity) {
			MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) accessor.getBlockEntity();
			appendTooltip(tooltip, spawner.getSpawner().getDisplayEntity(), name);
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		SpawnerMinecartEntity spawner = (SpawnerMinecartEntity) accessor.getEntity();
		Text name = ObjectNameProvider.getEntityName(spawner);
		//port: 1.12 keeps the spawner logic in a private field, exposed through an accessor mixin
		appendTooltip(tooltip, ((SpawnerMinecartEntityAccess) spawner).jade$getSpawner().getDisplayEntity(), name);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_MOB_SPAWNER;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.INSTANCE.getDefaultPriority() + 10;
	}
}
