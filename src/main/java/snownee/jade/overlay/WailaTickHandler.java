package snownee.jade.overlay;

import snownee.jade.api.AccessorClientHandler;
import com.google.common.base.Preconditions;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import snownee.jade.Jade;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.callback.JadeRayTraceCallback;
import snownee.jade.api.callback.JadeTooltipCollectedCallback;
import snownee.jade.api.config.IWailaConfig;
import snownee.jade.api.config.IWailaConfig.DisplayMode;
import snownee.jade.api.config.IWailaConfig.IConfigGeneral;
import snownee.jade.api.theme.Theme;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.ui.BoxElement;
import snownee.jade.util.ClientProxy;

public class WailaTickHandler {

	private static WailaTickHandler INSTANCE = new WailaTickHandler();
	public BoxElement rootElement;
	public ProgressTracker progressTracker = new ProgressTracker();

	public static WailaTickHandler instance() {
		if (INSTANCE == null)
			INSTANCE = new WailaTickHandler();
		return INSTANCE;
	}

	public static void narrate(ITooltip tooltip, boolean dedupe) {
	}

	public static void clearLastNarration() {
	}

	public void tickClient() {
		progressTracker.tick();

		IConfigGeneral config = Jade.CONFIG.get().getGeneral();
		if (!config.shouldDisplayTooltip()) {
			rootElement = null;
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (!ClientProxy.shouldShowWithOverlay(client, client.screen)) {
			return;
		}

		World world = client.world;
		Entity entity = client.getCamera();
		if (world == null || entity == null) {
			rootElement = null;
			return;
		}

		RayTracing.INSTANCE.fire();
		HitResult target = RayTracing.INSTANCE.getTarget();

		Tooltip tooltip = new Tooltip();

		if (target == null) {
			rootElement = null;
			return;
		}

		Accessor<?> accessor = null;
		if (target.type == HitResult.Type.BLOCK) {
			BlockState state = RayTracing.wrapBlock(world, target);
			BlockEntity tileEntity = world.getBlockEntity(target.getPos());
			accessor = WailaClientRegistration.instance().blockAccessor()
				.blockState(state)
				.blockEntity(tileEntity)
				.hit(target)
				.requireVerification()
				.build();
		} else if (target.type == HitResult.Type.ENTITY && target.entity != null) {
			accessor = WailaClientRegistration.instance().entityAccessor()
				.hit(target)
				.entity(target.entity)
				.requireVerification()
				.build();
		} else if (client.screen instanceof BaseOptionsScreen) {
			BlockPos pos = new BlockPos(entity.x, entity.y, entity.z);
			accessor = WailaClientRegistration.instance().blockAccessor()
				.blockState(Blocks.GRASS.defaultState())
				.hit(new HitResult(new Vec3d(entity.x, entity.y + entity.getEyeHeight(), entity.z), Direction.UP, pos))
				.build();
		}

		Accessor<?> originalAccessor = accessor;
		for (JadeRayTraceCallback callback : WailaClientRegistration.instance().rayTraceCallback.callbacks()) {
			accessor = callback.onRayTrace(target, accessor, originalAccessor);
		}
		ObjectDataCenter.set(accessor);
		if (accessor == null || accessor.getHitResult() == null) {
			rootElement = null;
			return;
		}

		AccessorClientHandler<Accessor<?>> handler = WailaClientRegistration.instance().getAccessorHandler(accessor.getAccessorType());
		if (!handler.shouldDisplay(accessor)) {
			rootElement = null;
			return;
		}
		if (accessor.isServerConnected()) {
			if (!accessor.verifyData(accessor.getServerData())) {
				accessor.getServerData().getKeys().clear();
			}
			boolean request = handler.shouldRequestData(accessor);
			if (ObjectDataCenter.isTimeElapsed(ObjectDataCenter.rateLimiter)) {
				ObjectDataCenter.resetTimer();
				if (request)
					handler.requestData(accessor);
			}
			if (request && ObjectDataCenter.getServerData() == null) {
				return;
			}
		}

		OverlayRenderer.theme.setValue(IWailaConfig.get().getOverlay().getTheme());
		Accessor<?> accessor0 = accessor;
		WailaClientRegistration.instance().beforeTooltipCollectCallback.call(callback -> {
			callback.beforeCollecting(OverlayRenderer.theme, accessor0);
		});
		Theme theme = OverlayRenderer.theme.getValue();
		Preconditions.checkNotNull(theme, "Theme cannot be null");

		if (config.getDisplayMode() == DisplayMode.LITE && !ClientProxy.isShowDetailsPressed()) {
			Tooltip dummyTooltip = new Tooltip();
			handler.gatherComponents(accessor, $ -> {
				if (Math.abs(WailaCommonRegistration.instance().priorities.byValue($)) > 5000) {
					return tooltip;
				} else {
					return dummyTooltip;
				}
			});
			if (!dummyTooltip.isEmpty()) {
				tooltip.sneakyDetails = true;
			}
		} else {
			handler.gatherComponents(accessor, $ -> tooltip);
		}

		rootElement = new BoxElement(tooltip, theme.tooltipStyle);
		rootElement.tag(Identifiers.ROOT);
		rootElement.setThemeIcon(RayTracing.INSTANCE.getIcon(), theme);
		for (JadeTooltipCollectedCallback callback : WailaClientRegistration.instance().tooltipCollectedCallback.callbacks()) {
			callback.onTooltipCollected(rootElement, accessor);
		}
	}
}
