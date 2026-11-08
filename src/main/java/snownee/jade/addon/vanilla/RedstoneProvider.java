package snownee.jade.addon.vanilla;

import net.minecraft.block.*;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum RedstoneProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		Block block = state.getBlock();
		IThemeHelper t = IThemeHelper.get();
		if (block instanceof LeverBlock) {
			Text info = state.get(LeverBlock.POWERED) ? t.success(I18n.translate("tooltip.jade.state_on"))
				: t.danger(I18n.translate("tooltip.jade.state_off"));
			tooltip.add(new LiteralText(I18n.translate("tooltip.jade.state", info.getFormattedString())));
			return;
		}
		if (block == Blocks.REPEATER || block == Blocks.POWERED_REPEATER) {
			int delay = state.get(RepeaterBlock.DELAY);
			tooltip.add(new LiteralText(I18n.translate("tooltip.jade.delay", t.info(delay).getFormattedString())));
			return;
		}
		if (block == Blocks.COMPARATOR || block == Blocks.POWERED_COMPARATOR) {
			ComparatorBlock.Mode mode = state.get(ComparatorBlock.MODE);
			Text modeInfo = t.info(I18n.translate("tooltip.jade.mode_" + (mode == ComparatorBlock.Mode.COMPARE ? "comparator" : "subtractor")));
			tooltip.add(new LiteralText(I18n.translate("tooltip.jade.mode", modeInfo.getFormattedString())));
			if (accessor.getServerData().contains("Signal")) {
				tooltip.add(new LiteralText(I18n.translate("tooltip.jade.power", t.info(accessor.getServerData().getInt("Signal")).getFormattedString())));
			}
			return;
		}
		if (block instanceof HopperBlock && accessor.getServerData().contains("HopperLocked")) {
			tooltip.add(t.danger(I18n.translate("jade.hopper.locked")));
		}
	}

	@Override
	public void appendServerData(NbtCompound data, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof ComparatorBlockEntity) {
			data.putInt("Signal", ((ComparatorBlockEntity) accessor.getBlockEntity()).getOutputSignal());
		} else if (accessor.getBlockEntity() instanceof HopperBlockEntity) {
			BlockState state = accessor.getBlockState();
			if (state.getBlock() == Blocks.HOPPER && !state.get(HopperBlock.ENABLED)) {
				data.putBoolean("HopperLocked", true);
			}
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_REDSTONE;
	}
}
