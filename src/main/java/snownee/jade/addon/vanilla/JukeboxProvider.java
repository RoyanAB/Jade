package snownee.jade.addon.vanilla;

import net.minecraft.block.JukeboxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import snownee.jade.Jade;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;

public enum JukeboxProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("Record")) {
			if (!accessor.getBlockState().get(JukeboxBlock.HAS_RECORD)) {
				tooltip.add(new TranslatableText("tooltip.jade.empty"));
			}
			return;
		}
		try {
			ItemStack stack = new ItemStack(accessor.getServerData().getCompound("Record"));
			if (stack.isEmpty()) {
				tooltip.add(new TranslatableText("tooltip.jade.empty"));
				return;
			}
			tooltip.add(new TranslatableText("record.nowPlaying", IDisplayHelper.get().stripColor(new LiteralText(stack.getHoverName()))));
		} catch (Exception e) {
			Jade.LOGGER.error("", e);
		}
	}

	@Override
	public void appendServerData(NbtCompound data, BlockAccessor accessor) {
		ItemStack stack = ((JukeboxBlock.JukeboxBlockEntity) accessor.getBlockEntity()).getRecord();
		data.put("Record", stack.writeNbt(new NbtCompound()));
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_JUKEBOX;
	}
}
