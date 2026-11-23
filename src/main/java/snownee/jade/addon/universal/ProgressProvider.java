package snownee.jade.addon.universal;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.*;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;

import java.util.List;
import java.util.function.Function;

public enum ProgressProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	public static void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("JadeProgress")) {
			IClientExtensionProvider<NbtCompound, ProgressView> provider = null;
			try {
				provider = WailaClientRegistration.instance().progressProviders.get(new Identifier(accessor.getServerData().getString("JadeProgressUid")));
			} catch (Exception ignored) {
			}
			if (provider != null) {
				List<ClientViewGroup<ProgressView>> groups = provider.getClientGroups(accessor, ViewGroup.readList(accessor.getServerData(), "JadeProgress", Function.identity()));
				if (groups.isEmpty()) {
					return;
				}

				IElementHelper helper = IElementHelper.get();
				boolean renderGroup = groups.size() > 1 || groups.get(0).shouldRenderGroup();
				BoxStyle.GradientBorder boxStyle = BoxStyle.getTransparent().clone();
				boxStyle.bgColor = 0x44FFFFFF;
				ClientViewGroup.tooltip(tooltip, groups, renderGroup, (theTooltip, group) -> {
					if (renderGroup) {
						group.renderHeader(theTooltip);
					}
					for (ProgressView view : group.views) {
						if (view.text != null) {
							theTooltip.add(helper.text(view.text).scale(0.75F));
							theTooltip.setLineMargin(-1, Direction2D.DOWN, 0);
						}
						theTooltip.add(helper.progress(view.progress, null, view.style, boxStyle, false).size(new Vec2f(10, 2)));
					}
				});
			}
		}
	}

	public static void putData(Accessor<?> accessor) {
		NbtCompound tag = accessor.getServerData();
		Object target = accessor.getTarget();
		for (IServerExtensionProvider<Object, NbtCompound> provider : WailaCommonRegistration.instance().progressProviders.get(target)) {
			List<ViewGroup<NbtCompound>> groups = provider.getGroups(accessor, target);
			if (groups != null) {
				if (ViewGroup.saveList(tag, "JadeProgress", groups, Function.identity()))
					tag.putString("JadeProgressUid", provider.getUid().toString());
				return;
			}
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(NbtCompound data, BlockAccessor accessor) {
		putData(accessor);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.UNIVERSAL_PROGRESS;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
