package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;

public enum ArmorStandProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ArmorStandEntity entity = (ArmorStandEntity) accessor.getEntity();
		boolean empty = true;
		for (ItemStack stack : entity.getArmor()) {
			if (stack.isEmpty()) {
				continue;
			}
			tooltip.add(IElementHelper.get().smallItem(stack));
			tooltip.append(IDisplayHelper.get().stripColor(new LiteralText(stack.getHoverName())));
			tooltip.setLineMargin(-1, Direction2D.DOWN, -1);
			empty = false;
		}
		if (!empty) {
			tooltip.setLineMargin(-1, Direction2D.DOWN, 1);
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ARMOR_STAND;
	}
}
