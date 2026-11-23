package snownee.jade.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Jade;
import snownee.jade.util.CommonProxy;

import java.util.Collections;
import java.util.List;

public class JadeCommand extends AbstractCommand {

	private static final String USAGE = "/jade <show|hide> <players>";
	private static final String SHOW_SUCCESS = "Jade overlay shown for %s player(s)";
	private static final String HIDE_SUCCESS = "Jade overlay hidden for %s player(s)";

	@Override
	public String getName() {
		return Jade.MODID;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(CommandSource source) {
		return USAGE;
	}

	@Override
	public void run(MinecraftServer server, CommandSource source, String[] args) throws CommandException {
		if (args.length < 2) {
			throw new IncorrectUsageException(USAGE);
		}
		boolean show;
		if ("show".equals(args[0])) {
			show = true;
		} else if ("hide".equals(args[0])) {
			show = false;
		} else {
			throw new IncorrectUsageException(USAGE);
		}
		List<ServerPlayerEntity> players = parsePlayers(server, source, args[1]);
		int count = CommonProxy.showOrHideFromServer(players, show);
		sendSuccess(source, this, show ? SHOW_SUCCESS : HIDE_SUCCESS, count);
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource source, String[] args, @Nullable BlockPos pos) {
		if (args.length == 1) {
			return suggestMatching(args, "show", "hide");
		}
		if (args.length == 2) {
			return suggestMatching(args, server.getPlayerNames());
		}
		return Collections.emptyList();
	}

	@Override
	public boolean hasTargetSelectorAt(String[] args, int index) {
		return index == 1;
	}

}
