package snownee.jade.util;

import com.google.common.collect.Sets;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Jade;
import snownee.jade.api.IJadeProvider;
import snownee.jade.api.ITooltip;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class WailaExceptionHandler {

	private static final Set<IJadeProvider> ERRORS = Sets.newHashSet();
	private static final File ERROR_OUTPUT = new File("logs", "JadeErrorOutput.txt");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss");

	public static void handleErr(Throwable e, @Nullable IJadeProvider provider, @Nullable ITooltip tooltip) {
		if (CommonProxy.isDevEnv()) {
			ExceptionUtils.rethrow(e);
			return;
		}
		if (!ERRORS.contains(provider)) {
			ERRORS.add(provider);

			Jade.LOGGER.error("Caught unhandled exception : [{}] {}", provider, e);
			Jade.LOGGER.error("See JadeErrorOutput.txt for more information");
			try {
				String s = DATE_FORMAT.format(new Date()) + "\n" + provider + "\n" + ExceptionUtils.getStackTrace(e) + "\n";
				Files.write(ERROR_OUTPUT.toPath(), s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			} catch (Exception what) {
				// no
			}
		}
		if (tooltip != null) {
			String modid = null;
			if (provider != null) {
				modid = provider.getUid().getNamespace();
			}
			if (modid == null || "minecraft".equals(modid)) {
				modid = Jade.MODID;
			}
			Text text = new LiteralText(String.format(I18n.translate("jade.error"), ModIdentification.getModName(modid)));
			text.setStyle(new Style().setColor(Formatting.DARK_RED));
			tooltip.add(text);
		}
	}
}
