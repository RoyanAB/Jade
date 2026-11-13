package snownee.jade.mixin;

import net.minecraft.block.spawner.MobSpawner;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpawnerMinecartEntity.class)
public interface SpawnerMinecartEntityAccess {

	@Accessor("spawner")
	MobSpawner jade$getSpawner();

}
