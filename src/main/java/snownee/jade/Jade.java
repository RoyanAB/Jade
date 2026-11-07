package snownee.jade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.config.WailaConfig;
import snownee.jade.impl.config.WailaConfig.ConfigGeneral;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.JsonConfig;

public class Jade {
	public static final String MODID = "jade";
	public static final Logger LOGGER = LogManager.getLogger("Jade");
	/**
	 * addons: Use {@link IWailaConfig#get()}
	 */
	public static final JsonConfig<WailaConfig> CONFIG = new JsonConfig<>(Jade.MODID + "/" + Jade.MODID, WailaConfig.class, null);
	public static int MAX_DISTANCE_SQR = 900;
	public static boolean FROZEN;

	public static void loadComplete() {
		if (FROZEN) {
			return;
		}
		FROZEN = true;

		WailaCommonRegistration.instance().priorities.sort(PluginConfig.INSTANCE.getKeys());
		WailaCommonRegistration.instance().loadComplete();
		if (CommonProxy.isPhysicallyClient()) {
			WailaClientRegistration.instance().loadComplete();
			ConfigGeneral.init();
		}
		PluginConfig.INSTANCE.reload();
	}
}
