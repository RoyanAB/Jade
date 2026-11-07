package snownee.jade.addon.vanilla;

import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.addon.core.ObjectNameProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum MobSpawnerProvider implements IBlockComponentProvider {

	INSTANCE;

	public static void appendTooltip(ITooltip tooltip, Entity displayEntity, Text name) {
		if (displayEntity == null) {
			return;
		}
		Text text = new LiteralText(net.minecraft.client.resource.language.I18n.translate("jade.spawner",
				name.getFormattedString(), displayEntity.getDisplayName().getFormattedString()));
		tooltip.replace(Identifiers.CORE_OBJECT_NAME, IThemeHelper.get().title(text));
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Text name = new LiteralText(accessor.getBlock().getTranslationKey());
		if (accessor.getBlockEntity() instanceof MobSpawnerBlockEntity) {
			MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) accessor.getBlockEntity();
			appendTooltip(tooltip, spawner.getSpawner().getDisplayEntity(), name);
		}
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
