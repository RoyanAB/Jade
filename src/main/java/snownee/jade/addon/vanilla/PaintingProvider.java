package snownee.jade.addon.vanilla;

import net.minecraft.entity.decoration.PaintingEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.TranslatableText;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

import java.util.Locale;

public enum PaintingProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		String name = ((PaintingEntity) accessor.getEntity()).motive.name.toLowerCase(Locale.ENGLISH);
		tooltip.add(IThemeHelper.get().warning(new TranslatableText("painting." + name + ".title")));
		tooltip.add(new TranslatableText("painting." + name + ".author"));
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_PAINTING;
	}
}
