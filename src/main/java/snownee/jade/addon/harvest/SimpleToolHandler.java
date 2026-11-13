package snownee.jade.addon.harvest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class SimpleToolHandler implements ToolHandler {

	public final java.util.Set<Block> blocks = Sets.newIdentityHashSet();
	protected final List<ItemStack> tools = Lists.newArrayList();
	private final Identifier uid;
	private final boolean testIsCorrectTool;

	public SimpleToolHandler(Identifier uid, boolean testIsCorrectTool, List<ItemStack> tools) {
		this.uid = uid;
		this.testIsCorrectTool = testIsCorrectTool;
		if (testIsCorrectTool) {
			Preconditions.checkArgument(!tools.isEmpty(), "tools cannot be empty");
		} else {
			Preconditions.checkArgument(tools.size() == 1, "tools must have only one element");
		}
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
				if (isEffective(state, tool)) {
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

	/**
	 * 1.12 equivalent of 1.20's {@code ItemStack#isCorrectToolForDrops}: the tool has to actually
	 * speed up mining this block, and - only for materials that require a tool - the tier has to
	 * be high enough.
	 * <p>
	 * {@code ItemStack#canMineBlock} must not be used on its own: vanilla only overrides it for
	 * special cases (shovel: snow, sword: cobweb, shears: web/tripwire, axe: nothing), so asking
	 * it about dirt or a log would report no tool at all.
	 */
	public static boolean isEffective(BlockState state, ItemStack tool) {
		if (tool.getMiningSpeed(state) <= 1.0F) {
			return false;
		}
		// same short circuit as PlayerInventory#canMineBlock
		return state.getMaterial().isToolNotRequired() || tool.canMineBlock(state);
	}

	@Override
	public List<ItemStack> getTools() {
		return tools;
	}

	@Override
	public Identifier getUid() {
		return uid;
	}

	public SimpleToolHandler addBlock(Block block) {
		blocks.add(block);
		return this;
	}
}
