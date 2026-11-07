package snownee.jade.api;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.HitResult;

/**
 * Class to get information of entity target and context.
 */
public interface EntityAccessor extends Accessor<HitResult> {

	Entity getEntity();

	/**
	 * For part entity like ender dragon's, getEntity() will return the parent entity.
	 */
	//TODO 1.21: remove default implementation
	default Entity getRawEntity() {
		return getEntity();
	}

	@Override
	default Class<? extends Accessor<?>> getAccessorType() {
		return EntityAccessor.class;
	}

	@NonExtendable
	public interface Builder {
		Builder level(World level);

		Builder player(PlayerEntity player);

		Builder serverData(NbtCompound serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		default Builder hit(HitResult hit) {
			return hit(() -> hit);
		}

		Builder hit(Supplier<HitResult> hit);

		default Builder entity(Entity entity) {
			return entity(() -> entity);
		}

		Builder entity(Supplier<Entity> entity);

		Builder from(EntityAccessor accessor);

		Builder requireVerification();

		EntityAccessor build();
	}
}
