package snownee.jade.addon.vanilla;

import net.minecraft.block.TntBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum TNTStabilityProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (state.get(TntBlock.EXPLODE)) {
			tooltip.add(IThemeHelper.get().danger(I18n.translate("jade.tnt.unstable")));
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_TNT_STABILITY;
	}
}
