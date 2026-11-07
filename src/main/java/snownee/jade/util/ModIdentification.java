package snownee.jade.util;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import snownee.jade.api.callback.JadeItemModNameCallback;
import snownee.jade.impl.WailaClientRegistration;

public class ModIdentification {

	public static final ModIdentification INSTANCE = new ModIdentification();
	private static final Map<String, Optional<String>> NAMES = Maps.newConcurrentMap();

	public static void invalidateCache() {
		NAMES.clear();
	}

	public static Optional<String> getModName(String namespace) {
		return NAMES.computeIfAbsent(namespace, $ -> {
			Optional<String> name = ClientProxy.getModName(namespace);
			if (name.isPresent()) {
				return name;
			}
			String key = "jade.modName." + namespace;
			if (I18n.hasTranslation(key)) {
				return Optional.of(I18n.translate(key));
			} else {
				return Optional.empty();
			}
		});
	}

	public static String getModName(Identifier id) {
		return getModName(id.getNamespace()).orElse(id.getNamespace());
	}

	public static String getModName(net.minecraft.block.Block block) {
		return getModName(CommonProxy.getId(block));
	}

	public static String getModName(ItemStack stack) {
		for (JadeItemModNameCallback callback : WailaClientRegistration.instance().itemModNameCallback.callbacks()) {
			String s = callback.gatherItemModName(stack);
			if (!Strings.isNullOrEmpty(s)) {
				return s;
			}
		}
		String id = CommonProxy.getModIdFromItem(stack);
		return getModName(id).orElse(id);
	}

	public static String getModName(Entity entity) {
		if (entity instanceof ItemEntity) {
			return getModName(((ItemEntity) entity).getItem());
		}
		Identifier id = net.minecraft.entity.Entities.REGISTRY.getKey((Class<? extends net.minecraft.entity.Entity>) entity.getClass());
		return id == null ? "minecraft" : getModName(id.getNamespace()).orElse(id.getNamespace());
	}

	public void onResourceManagerReload() {
		invalidateCache();
	}

}
