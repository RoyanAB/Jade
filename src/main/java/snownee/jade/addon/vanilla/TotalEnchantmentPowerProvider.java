package snownee.jade.addon.vanilla;

import net.minecraft.resource.Identifier;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.util.CommonProxy;

public enum TotalEnchantmentPowerProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		World world = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		float power = 0;
		// EnchantingTableMenu.java
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if ((x == 0 && z == 0) || !world.isAir(pos.add(x, 0, z)) || !world.isAir(pos.add(x, 1, z))) {
					continue;
				}
				power += getPower(world, pos.add(x * 2, 0, z * 2));
				power += getPower(world, pos.add(x * 2, 1, z * 2));
				if (x != 0 && z != 0) {
					power += getPower(world, pos.add(x * 2, 0, z));
					power += getPower(world, pos.add(x * 2, 1, z));
					power += getPower(world, pos.add(x, 0, z * 2));
					power += getPower(world, pos.add(x, 1, z * 2));
				}
			}
		}

		if (power > 0) {
			tooltip.add(new TranslatableText("jade.ench_power", IThemeHelper.get().info(DisplayHelper.dfCommas.format(power))));
		}
	}

	public static float getPower(World world, BlockPos pos) {
		return CommonProxy.getEnchantPowerBonus(world.getBlockState(pos), world, pos);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_TOTAL_ENCHANTMENT_POWER;
	}

	@Override
	public int getDefaultPriority() {
		return -400;
	}
}
