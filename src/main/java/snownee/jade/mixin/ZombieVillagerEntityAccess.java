package snownee.jade.mixin;

import net.minecraft.entity.living.mob.monster.ZombieVillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZombieVillagerEntity.class)
public interface ZombieVillagerEntityAccess {

	@Accessor("conversionTicks")
	int jade$getConversionTicks();

}
