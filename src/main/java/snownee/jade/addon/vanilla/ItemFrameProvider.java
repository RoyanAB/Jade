package snownee.jade.addon.vanilla;

import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

public enum ItemFrameProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemStack stack = ((ItemFrameEntity) accessor.getEntity()).getDisplayItem();
		if (!stack.isEmpty()) {
			tooltip.add(IDisplayHelper.get().stripColor(new LiteralText(stack.getHoverName())));
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ITEM_FRAME;
	}
}
