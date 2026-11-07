package snownee.jade.addon.harvest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class SimpleToolHandler implements ToolHandler {

	public final java.util.Set<net.minecraft.block.Block> blocks = Sets.newIdentityHashSet();
	protected final List<ItemStack> tools = Lists.newArrayList();
	private final Identifier uid;
	private final boolean testIsCorrectTool;

	public SimpleToolHandler(Identifier uid, boolean testIsCorrectTool, List<ItemStack> tools) {
		this.uid = uid;
		this.testIsCorrectTool = testIsCorrectTool;
		this.tools.addAll(tools);
	}

	public static SimpleToolHandler create(Identifier uid, boolean testIsCorrectTool, List<Item> tools) {
		List<ItemStack> stacks = Lists.newArrayList();
		for (Item item : tools) {
			stacks.add(new ItemStack(item));
		}
		return new SimpleToolHandler(uid, testIsCorrectTool, stacks);
	}

	public boolean matchesBlock(BlockState state) {
		return blocks.contains(state.getBlock());
	}

	@Override
	public ItemStack test(BlockState state, World world, BlockPos pos) {
		if (testIsCorrectTool) {
			float hardness = state.getMiningSpeed(world, pos);
			if (state.getMaterial().isToolNotRequired() && hardness == 0) {
				return ItemStack.EMPTY;
			}
			for (ItemStack tool : tools) {
				if (tool.canMineBlock(state) && tool.getMiningSpeed(state) > 1) {
					return tool;
				}
			}
			return ItemStack.EMPTY;
		}
		if (matchesBlock(state)) {
			return tools.get(0);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public List<ItemStack> getTools() {
		return tools;
	}

	@Override
	public Identifier getUid() {
		return uid;
	}

	public SimpleToolHandler addBlock(net.minecraft.block.Block block) {
		blocks.add(block);
		return this;
	}
}
