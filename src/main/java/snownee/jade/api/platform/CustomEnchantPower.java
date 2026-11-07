package snownee.jade.api.platform;

import net.minecraft.block.state.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface CustomEnchantPower {

	float getEnchantPowerBonus(BlockState state, World world, BlockPos pos);

}
