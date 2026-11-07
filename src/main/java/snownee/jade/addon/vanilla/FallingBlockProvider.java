package snownee.jade.addon.vanilla;

import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum FallingBlockProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		FallingBlockEntity entity = (FallingBlockEntity) accessor.getEntity();
		Text title = IThemeHelper.get().title(net.minecraft.client.resource.language.I18n.translate(entity.getBlock().getBlock().getTranslationKey()));
		tooltip.replace(Identifiers.CORE_OBJECT_NAME, title);
	}

	@Override
	public IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
		FallingBlockEntity entity = (FallingBlockEntity) accessor.getEntity();
		ItemStack stack = new ItemStack(entity.getBlock().getBlock());
		if (stack.isEmpty()) {
			return currentIcon;
		}
		return IElementHelper.get().item(stack);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_FALLING_BLOCK;
	}

	@Override
	public boolean isRequired() {
		return true;
	}
}
