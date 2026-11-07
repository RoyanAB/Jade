package snownee.jade.addon.vanilla;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElementHelper;

public enum BrewingStandProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("BrewingStand")) {
			return;
		}
		NbtCompound tag = accessor.getServerData().getCompound("BrewingStand");
		int fuel = tag.getInt("Fuel");
		int time = tag.getInt("Time");
		IElementHelper helper = IElementHelper.get();
		tooltip.add(helper.smallItem(new ItemStack(Items.BLAZE_POWDER)));
		tooltip.append(IThemeHelper.get().info(fuel));
		if (time > 0) {
			tooltip.append(helper.spacer(5, 0));
			tooltip.append(helper.smallItem(new ItemStack(Items.CLOCK)));
			tooltip.append(IThemeHelper.get().seconds(time));
		}
	}

	@Override
	public void appendServerData(NbtCompound tag, BlockAccessor accessor) {
		BrewingStandBlockEntity brewingStand = (BrewingStandBlockEntity) accessor.getBlockEntity();
		NbtCompound compound = new NbtCompound();
		compound.putInt("Time", brewingStand.getData(0));
		compound.putInt("Fuel", brewingStand.getData(1));
		tag.put("BrewingStand", compound);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_BREWING_STAND;
	}
}
