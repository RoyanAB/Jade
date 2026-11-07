package snownee.jade.addon.core;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

import java.util.List;
import java.util.Locale;

public enum BlockFaceProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		tooltip.replace(Identifiers.CORE_OBJECT_NAME, lists -> {
			List<IElement> lastList = lists.get(lists.size() - 1);
			lastList.add(IElementHelper.get().text(new LiteralText(I18n.translate("jade.blockFace", I18n.translate("jade." + accessor.getSide().name().toLowerCase(Locale.ROOT))))));
			return lists;
		});
	}

	@Override
	public Identifier getUid() {
		return Identifiers.CORE_BLOCK_FACE;
	}

	@Override
	public int getDefaultPriority() {
		return ObjectNameProvider.INSTANCE.getDefaultPriority() + 1;
	}

	@Override
	public boolean enabledByDefault() {
		return false;
	}

}
