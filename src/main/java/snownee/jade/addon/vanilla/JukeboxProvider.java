package snownee.jade.addon.vanilla;

import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.Jade;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

public enum JukeboxProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		if (state.get(JukeboxBlock.HAS_RECORD) && accessor.getServerData().contains("Record")) {
			try {
				ItemStack stack = new ItemStack(accessor.getServerData().getCompound("Record"));
				tooltip.add(new LiteralText(I18n.translate("record.nowPlaying",
					IDisplayHelper.get().stripColor(new LiteralText(stack.getHoverName())).getFormattedString())));
			} catch (Exception e) {
				Jade.LOGGER.error("", e);
			}
		} else {
			tooltip.add(new LiteralText(I18n.translate("tooltip.jade.empty")));
		}
	}

	@Override
	public void appendServerData(NbtCompound data, BlockAccessor accessor) {
		ItemStack stack = ((JukeboxBlock.JukeboxBlockEntity) accessor.getBlockEntity()).getRecord();
		if (!stack.isEmpty()) {
			data.put("Record", stack.writeNbt(new NbtCompound()));
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_JUKEBOX;
	}
}
