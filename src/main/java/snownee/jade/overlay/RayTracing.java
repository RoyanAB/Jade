package snownee.jade.overlay;

import java.util.List;
import com.google.common.base.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import snownee.jade.Jade;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.IElement;
import snownee.jade.impl.ObjectDataCenter;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.ui.ItemStackElement;
import snownee.jade.util.CommonProxy;

public class RayTracing {

	public static final RayTracing INSTANCE = new RayTracing();
	public static Predicate<Entity> ENTITY_FILTER = entity -> true;
	private final Minecraft mc = Minecraft.getInstance();
	private HitResult target = null;

	private RayTracing() {
	}

	public static BlockState wrapBlock(World world, HitResult hit) {
		if (hit.type != HitResult.Type.BLOCK) {
			return net.minecraft.block.Blocks.AIR.defaultState();
		}
		return world.getBlockState(hit.getPos());
	}

	@Nullable
	public static Entity getEntityHit(World world, Entity viewpoint, Vec3d start, Vec3d end, Predicate<Entity> filter) {
		Entity best = null;
		double bestDist = Double.MAX_VALUE;
		Box traceBox = new Box(start, end).expanded(1.0, 1.0, 1.0);
		for (Entity entity : world.getEntities(viewpoint, traceBox, filter)) {
			Box box = entity.getShape().expanded(0.3, 0.3, 0.3);
			double dist;
			if (box.contains(start)) {
				dist = 0;
			} else {
				Vec3d center = new Vec3d(entity.x, entity.y + entity.height / 2, entity.z);
				dist = distanceSqToSegment(center, start, end);
				if (dist > (entity.width * entity.width + entity.height * entity.height) / 4 + 0.09) {
					continue;
				}
			}
			if (dist < bestDist) {
				best = entity;
				bestDist = dist;
			}
		}
		return best;
	}

	private static double distanceSqToSegment(Vec3d point, Vec3d start, Vec3d end) {
		Vec3d ab = end.subtract(start);
		Vec3d ap = point.subtract(start);
		double abLen = ab.squaredDistanceToOrigin();
		double t = abLen <= 1.0E-7 ? 0 : Math.max(0, Math.min(1, ap.dot(ab) / abLen));
		Vec3d projection = start.add(ab.scale(t));
		return point.squaredDistanceTo(projection);
	}

	public static boolean isEmptyElement(IElement element) {
		return element == null || element == ItemStackElement.EMPTY;
	}

	public void fire() {
		Entity viewpoint = mc.getCamera();
		if (viewpoint == null)
			return;

		if (mc.crosshairTarget != null && mc.crosshairTarget.type == HitResult.Type.ENTITY) {
			Entity targetEntity = mc.crosshairTarget.entity;
			if (targetEntity != null && canBeTarget(targetEntity, viewpoint)) {
				target = mc.crosshairTarget;
				return;
			}
		}

		float reach = (mc.interactionManager == null ? 5 : mc.interactionManager.getReach()) + Jade.CONFIG.get().getGeneral().getReachDistance();
		target = rayTrace(viewpoint, reach, mc.getTickDelta());
	}

	public HitResult getTarget() {
		return target;
	}

	@Nullable
	public HitResult rayTrace(Entity entity, double playerReach, float partialTicks) {
		Vec3d eyePosition = entity.getEyePosition(partialTicks);
		Vec3d lookVector = entity.getRotationVec(partialTicks);
		Vec3d traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);

		World world = entity.world;
		Box bound = new Box(eyePosition, traceEnd);
		Predicate<Entity> predicate = e -> canBeTarget(e, entity);
		Entity entityResult = getEntityHit(world, entity, eyePosition, traceEnd, predicate);

		HitResult blockResult = world.rayTrace(eyePosition, traceEnd, false);

		if (entityResult != null) {
			if (blockResult != null && blockResult.type == HitResult.Type.BLOCK) {
				double entityDist = entityResult.squaredDistanceTo(eyePosition.x, eyePosition.y, eyePosition.z);
				double blockDist = blockResult.facePos.squaredDistanceTo(eyePosition);
				if (entityDist < blockDist) {
					return new HitResult(entityResult, blockResult.facePos);
				}
			} else {
				return new HitResult(entityResult, traceEnd);
			}
		}
		if (blockResult != null && blockResult.type == HitResult.Type.BLOCK) {
			BlockState state = wrapBlock(world, blockResult);
			if (WailaClientRegistration.instance().shouldHide(state)) {
				return null;
			}
		}
		return blockResult;
	}

	private boolean canBeTarget(Entity target, Entity viewEntity) {
		if (target.removed)
			return false;
		if (target instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) target;
			if (player.isSpectator())
				return false;
		}
		if (target == viewEntity.getVehicle())
			return false;
		if (CommonProxy.isMultipartEntity(target))
			return false;
		if (viewEntity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) viewEntity;
			if (target.isInvisibleTo(player))
				return false;
		} else {
			if (target.isInvisible())
				return false;
		}
		return !WailaClientRegistration.instance().shouldHide(target) && ENTITY_FILTER.test(target);
	}

	public IElement getIcon() {
		Accessor<?> accessor = ObjectDataCenter.get();
		if (accessor == null)
			return null;

		IElement icon = ObjectDataCenter.getIcon();
		if (isEmptyElement(icon))
			return null;
		else
			return icon;
	}

}
