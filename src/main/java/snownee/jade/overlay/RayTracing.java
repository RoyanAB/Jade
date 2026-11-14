package snownee.jade.overlay;

import com.google.common.base.Predicate;
import net.minecraft.block.Blocks;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.mob.monster.boss.EnderDragonPart;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
			return Blocks.AIR.defaultState();
		}
		return world.getBlockState(hit.getPos());
	}

	/**
	 * Equivalent of 1.12's Entity.rayTrace + AABB clip used by ProjectileUtil.
	 */
	@Nullable
	public static HitResult getEntityHit(World world, Entity viewpoint, Vec3d start, Vec3d end, Predicate<Entity> filter) {
		Entity best = null;
		Vec3d bestPos = null;
		double bestDist = Double.MAX_VALUE;
		Box bound = new Box(start, end).expanded(1.0, 1.0, 1.0);
		for (Entity entity : world.getEntities(viewpoint, bound, filter)) {
			Box box = entity.getShape();
			if (box.getAverageSideLength() < 0.3) {
				box = box.expanded(0.3, 0.3, 0.3);
			}
			if (box.contains(start)) {
				best = entity;
				bestPos = start;
				break;
			}
			HitResult hit = box.clip(start, end);
			if (hit == null || hit.facePos == null) {
				continue;
			}
			double dist = start.squaredDistanceTo(hit.facePos);
			if (dist < bestDist) {
				best = entity;
				bestPos = hit.facePos;
				bestDist = dist;
			}
		}
		return best == null ? null : new HitResult(best, bestPos);
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
		Vec3d traceEnd;
		if (mc.crosshairTarget != null && mc.crosshairTarget.type == HitResult.Type.BLOCK && mc.crosshairTarget.facePos != null) {
			// extend the trace a bit so the block already under the crosshair cannot disappear due to floating point error
			traceEnd = eyePosition.add(mc.crosshairTarget.facePos.subtract(eyePosition).scale(1.01));
		} else {
			Vec3d lookVector = entity.getRotationVec(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		World world = entity.world;
		Predicate<Entity> predicate = e -> canBeTarget(e, entity);
		HitResult entityResult = getEntityHit(world, entity, eyePosition, traceEnd, predicate);

		if (mc.crosshairTarget != null && mc.crosshairTarget.type == HitResult.Type.BLOCK) {
			Vec3d lookVector = entity.getRotationVec(partialTicks);
			traceEnd = eyePosition.add(lookVector.x * playerReach, lookVector.y * playerReach, lookVector.z * playerReach);
		}

		//port: 1.12 cannot hit flowing fluids, the setting is a plain ON/OFF toggle
		boolean allowLiquids = Jade.CONFIG.get().getGeneral().shouldDisplayFluids();
		BlockPos eyePos = new BlockPos(eyePosition);
		if (world.getBlockState(eyePos).getMaterial().isLiquid()) {
			// do not target the fluid the player is inside of
			allowLiquids = false;
		}
		HitResult blockResult = world.rayTrace(eyePosition, traceEnd, allowLiquids);

		if (entityResult != null) {
			if (blockResult != null && blockResult.type == HitResult.Type.BLOCK) {
				double entityDist = entityResult.facePos.squaredDistanceTo(eyePosition);
				double blockDist = blockResult.facePos.squaredDistanceTo(eyePosition);
				if (entityDist < blockDist) {
					return entityResult;
				}
			} else {
				return entityResult;
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
		if (target instanceof PlayerEntity && ((PlayerEntity) target).isSpectator())
			return false;
		if (target == viewEntity.getVehicle())
			return false;
		if (target instanceof ProjectileEntity && target.ticks <= 10)
			return false;
		if (CommonProxy.isMultipartEntity(target) && !target.isAlive())
			return false;
		//port: 1.12 lists dragon parts as separate entities, never target them directly
		if (target instanceof EnderDragonPart)
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
