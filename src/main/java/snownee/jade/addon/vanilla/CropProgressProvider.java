package snownee.jade.addon.vanilla;

import net.minecraft.block.*;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum CropProgressProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		if (accessor.getBlock() == Blocks.WHEAT) {
			return IElementHelper.get().item(new ItemStack(Items.WHEAT));
		}
		if (accessor.getBlock() == Blocks.BEETROOTS) {
			return IElementHelper.get().item(new ItemStack(Items.BEETROOT));
		}
		return null;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		Block block = state.getBlock();
		if (block instanceof BeetrootsBlock) {
			addMaturityTooltip(tooltip, state.get(BeetrootsBlock.AGE) / 3F);
		} else if (block instanceof WheatBlock) {
			addMaturityTooltip(tooltip, state.get(WheatBlock.AGE) / 7F);
		} else if (block instanceof NetherWartBlock) {
			addMaturityTooltip(tooltip, state.get(NetherWartBlock.AGE) / 3F);
		}
	}

	private static void addMaturityTooltip(ITooltip tooltip, float growthValue) {
		growthValue *= 100.0F;
		if (growthValue < 100.0F) {
			tooltip.add(new LiteralText(net.minecraft.client.resource.language.I18n.translate("tooltip.jade.crop_growth",
				IThemeHelper.get().info(String.format("%.0f%%", growthValue)).getFormattedString())));
		} else {
			Text mature = IThemeHelper.get().success(new LiteralText(net.minecraft.client.resource.language.I18n.translate("tooltip.jade.crop_mature")));
			tooltip.add(new LiteralText(net.minecraft.client.resource.language.I18n.translate("tooltip.jade.crop_growth", mature.getFormattedString())));
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_CROP_PROGRESS;
	}
}
