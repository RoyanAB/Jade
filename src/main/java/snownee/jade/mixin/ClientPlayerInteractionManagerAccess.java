package snownee.jade.mixin;

import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccess {

	@Accessor("target")
	BlockPos getTarget();

	@Accessor("miningProgress")
	float getMiningProgress();

	@Accessor("isMiningBlock")
	boolean isMiningBlock();

}
