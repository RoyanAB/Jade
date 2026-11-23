package snownee.jade.command;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Jade;
import snownee.jade.gui.HomeConfigScreen;
import snownee.jade.util.DumpGenerator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

public class JadeClientCommand extends AbstractCommand {

	private static final String USAGE = "/jadec <handlers|config>";
	private static final String[] SUBCOMMANDS = {"handlers", "config"};

	private static final JadeClientCommand INSTANCE = new JadeClientCommand();

	public static boolean tryRun(String message) {
		if (message == null) {
			return false;
		}
		String name = "/" + INSTANCE.getName();
		if (!message.equals(name) && !message.startsWith(name + " ")) {
			return false;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.world == null) {
			return false;
		}
		String[] args = message.length() == name.length() ? new String[0] : message.substring(name.length() + 1).split(" ");
		CommandSource source = source(mc);
		try {
			INSTANCE.run(source, args);
		} catch (CommandException e) {
			Text text = new TranslatableText(e.getMessage(), e.getArgs());
			text.getStyle().setColor(Formatting.RED);
			source.sendMessage(text);
		} catch (Throwable e) {
			source.sendMessage(new LiteralText(e.toString()));
		}
		return true;
	}

	public void run(CommandSource source, String[] args) throws CommandException {
		if (args.length < 1) {
			throw new IncorrectUsageException(USAGE);
		}
		switch (args[0]) {
			case "handlers": {
				File file = new File("jade_handlers.md");
				try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
					writer.write(DumpGenerator.generateInfoDump());
					source.sendMessage(new LiteralText("Jade handler dump written to " + file.getAbsolutePath()));
				} catch (IOException e) {
					source.sendMessage(new LiteralText("Failed to write " + file.getAbsolutePath() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage()));
				}
				break;
			}
			case "config": {
				openConfig = true;
				break;
			}
			default:
				throw new IncorrectUsageException(USAGE);
		}
	}

	private static CommandSource source(Minecraft mc) {
		return new CommandSource() {
			@Override
			public String getName() {
				return mc.player == null ? "Player" : mc.player.getName();
			}

			@Override
			public void sendMessage(Text message) {
				mc.gui.getChat().addMessage(message);
			}

			@Override
			public boolean canUseCommand(int permissionLevel, String command) {
				return true;
			}

			@Override
			public World getCommandSourceWorld() {
				return mc.world;
			}

			@Override
			public MinecraftServer getServer() {
				return mc.getServer();
			}
		};
	}

	@Override
	public String getName() {
		return Jade.MODID + "c";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getUsage(CommandSource source) {
		return USAGE;
	}

	@Override
	public void run(MinecraftServer server, CommandSource source, String[] args) throws CommandException {

	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource source, String[] args, @Nullable BlockPos pos) {
		return args.length == 1 ? suggestMatching(args, SUBCOMMANDS) : Collections.emptyList();
	}

	private static boolean openConfig;

	static {
		MinecraftClientEvents.TICK_END.register(JadeClientCommand::tick);
	}

	private static void tick(Minecraft mc) {
		if (!openConfig) return;

		openConfig = false;

		if (mc.player == null || mc.world == null) return;

		Jade.CONFIG.invalidate();
		mc.openScreen(new HomeConfigScreen(null));
	}
}
