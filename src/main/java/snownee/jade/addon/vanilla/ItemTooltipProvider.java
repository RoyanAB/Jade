package snownee.jade.addon.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipFlag;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.JadeClient;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.util.ModIdentification;

import java.util.List;
import java.util.Objects;

public enum ItemTooltipProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		ItemStack stack = ((ItemEntity) accessor.getEntity()).getItem();
		JadeClient.hideModName = true;
		List<String> lines = stack.getTooltip(null, TooltipFlag.Default.NORMAL);
		JadeClient.hideModName = false;
		if (lines.isEmpty()) {
			return;
		}
		String modName = ModIdentification.getModName(stack);
		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			if (Objects.equals(net.minecraft.text.Formatting.strip(line), modName)) {
				continue;
			}
			if (Minecraft.getInstance().textRenderer.getWidth(line) > 250) {
				line = Minecraft.getInstance().textRenderer.trim(line, 245) + "..";
			}
			tooltip.add(new LiteralText(line));
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ITEM_TOOLTIP;
	}
}
