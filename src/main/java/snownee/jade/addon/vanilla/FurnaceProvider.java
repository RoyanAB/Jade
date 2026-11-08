package snownee.jade.addon.vanilla;

import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.resource.Identifier;
import net.minecraft.util.math.Vec2f;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum FurnaceProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		NbtCompound data = accessor.getServerData();
		if (!data.contains("progress")) {
			return;
		}
		int progress = data.getInt("progress");
		NbtList furnaceItems = data.getList("furnace", 10);
		ItemStack[] inventory = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
		for (int i = 0; i < furnaceItems.size() && i < 3; i++) {
			inventory[i] = new ItemStack(furnaceItems.getCompound(i));
		}
		IElementHelper helper = IElementHelper.get();
		int total = Math.max(1, data.getInt("total"));
		tooltip.add(helper.item(inventory[0]));
		tooltip.append(helper.item(inventory[1]));
		tooltip.append(helper.spacer(4, 0));
		tooltip.append(helper.progress((float) progress / total).translate(new Vec2f(-2, 0)));
		tooltip.append(helper.item(inventory[2]));
	}

	@Override
	public void appendServerData(NbtCompound data, BlockAccessor accessor) {
		FurnaceBlockEntity furnace = (FurnaceBlockEntity) accessor.getBlockEntity();
		if (furnace.isEmpty()) {
			return;
		}
		NbtList items = new NbtList();
		for (int i = 0; i < 3; i++) {
			items.addElement(furnace.getItem(i).writeNbt(new NbtCompound()));
		}
		data.put("furnace", items);
		NbtCompound furnaceTag = furnace.writeNbt(new NbtCompound());
		data.putInt("progress", furnaceTag.getInt("CookTime"));
		data.putInt("total", furnaceTag.getInt("CookTimeTotal"));
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_FURNACE;
	}
}
