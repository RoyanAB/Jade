package snownee.jade.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import snownee.jade.api.*;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.ElementHelper;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.overlay.RayTracing;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

import java.util.List;
import java.util.function.Function;

public class EntityAccessorClientHandler implements AccessorClientHandler<EntityAccessor> {

	@Override
	public boolean shouldDisplay(EntityAccessor accessor) {
		IWailaConfig.IConfigGeneral general = IWailaConfig.get().getGeneral();
		if (!general.getDisplayEntities()) {
			return false;
		}
		return general.getDisplayBosses() || !CommonProxy.isBoss(accessor.getEntity());
	}

	@Override
	public boolean shouldRequestData(EntityAccessor accessor) {
		return !WailaCommonRegistration.instance().getEntityNBTProviders(accessor.getEntity()).isEmpty();
	}

	@Override
	public void requestData(EntityAccessor accessor) {
		ClientProxy.requestEntityData(accessor);
	}

	@Override
	public IElement getIcon(EntityAccessor accessor) {
		IElement icon = null;
		Entity entity = accessor.getEntity();
		if (entity instanceof ItemEntity) {
			icon = ItemStackElement.of(((ItemEntity) entity).getItem());
		} else {
			ItemStack stack = accessor.getPickedResult();
			if (!(stack.getItem() instanceof SpawnEggItem) || !(entity instanceof LivingEntity))
				icon = ItemStackElement.of(stack);
		}

		for (IEntityComponentProvider provider : WailaClientRegistration.instance().getEntityIconProviders(entity, PluginConfig.INSTANCE::get)) {
			try {
				IElement element = provider.getIcon(accessor, PluginConfig.INSTANCE, icon);
				if (!RayTracing.isEmptyElement(element))
					icon = element;
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, null);
			}
		}
		return icon;
	}

	@Override
	public void gatherComponents(EntityAccessor accessor, Function<IJadeProvider, ITooltip> tooltipProvider) {
		List<IEntityComponentProvider> providers = WailaClientRegistration.instance().getEntityProviders(accessor.getEntity(), PluginConfig.INSTANCE::get);
		for (IEntityComponentProvider provider : providers) {
			ITooltip tooltip = tooltipProvider.apply(provider);
			try {
				ElementHelper.INSTANCE.setCurrentUid(provider.getUid());
				provider.appendTooltip(tooltip, accessor, PluginConfig.INSTANCE);
			} catch (Throwable e) {
				WailaExceptionHandler.handleErr(e, provider, tooltip);
			} finally {
				ElementHelper.INSTANCE.setCurrentUid(null);
			}
		}
	}

}
