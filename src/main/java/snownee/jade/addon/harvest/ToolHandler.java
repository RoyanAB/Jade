package snownee.jade.addon.harvest;

import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.jade.api.IJadeProvider;

import java.util.List;

public interface ToolHandler extends IJadeProvider {

	ItemStack test(BlockState state, World world, BlockPos pos);

	List<ItemStack> getTools();

	@Override
	Identifier getUid();

}
