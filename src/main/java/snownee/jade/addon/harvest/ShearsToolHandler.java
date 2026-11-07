package snownee.jade.addon.harvest;

import java.util.List;

import net.minecraft.block.Blocks;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.jade.api.Identifiers;
import snownee.jade.util.CommonProxy;

public class ShearsToolHandler extends SimpleToolHandler {

	public ShearsToolHandler() {
		super(Identifiers.JADE("shears"), false, java.util.Collections.singletonList(new ItemStack(Items.SHEARS)));
		blocks.add(Blocks.VINE);
		blocks.add(Blocks.TRIPWIRE);
		blocks.add(Blocks.DEADBUSH);
		blocks.add(Blocks.WEB);
		blocks.add(Blocks.TALLGRASS);
	}

	@Override
	public ItemStack test(BlockState state, World world, BlockPos pos) {
		if (CommonProxy.isShearable(state)) {
			return tools.get(0);
		}
		return super.test(state, world, pos);
	}
}
