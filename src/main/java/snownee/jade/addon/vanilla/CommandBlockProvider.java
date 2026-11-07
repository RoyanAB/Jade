package snownee.jade.addon.vanilla;

import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public enum CommandBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("Command")) {
			return;
		}
		tooltip.add(new LiteralText("> " + accessor.getServerData().getString("Command")));
	}

	@Override
	public void appendServerData(NbtCompound tag, BlockAccessor accessor) {
		PlayerEntity player = accessor.getPlayer();
		if (!player.canUseCommand(2, "")) {
			return;
		}
		CommandBlockBlockEntity blockEntity = (CommandBlockBlockEntity) accessor.getBlockEntity();
		String command = blockEntity.getCommandExecutor().getCommand();
		if (command.isEmpty()) {
			return;
		}
		if (command.length() > 40) {
			command = command.substring(0, 37) + "...";
		}
		tag.putString("Command", command);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_COMMAND_BLOCK;
	}
}
