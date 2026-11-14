package snownee.jade.mixin;

import net.minecraft.entity.living.mob.passive.animal.HorseBaseEntity;
import net.minecraft.inventory.AnimalInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HorseBaseEntity.class)
public interface HorseBaseEntityAccess {

	@Accessor("inventory")
	AnimalInventory jade$getInventory();

}
