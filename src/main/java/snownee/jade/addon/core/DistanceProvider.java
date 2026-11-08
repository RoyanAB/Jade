package snownee.jade.addon.core;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.theme.ThemeHelper;

import java.text.DecimalFormat;

public enum DistanceProvider implements IBlockComponentProvider, IEntityComponentProvider {

	INSTANCE;

	public static final DecimalFormat fmt = new DecimalFormat("#.#");
	private static final int[] colors = {0xef9a9a, 0xa5d6a7, 0x90caf9, 0xb02a37, 0x198754, 0x0a58ca};

	public static String distance(Accessor<?> accessor) {
		PlayerEntity player = accessor.getPlayer();
		Vec3d eye = new Vec3d(player.x, player.y + player.getEyeHeight(), player.z);
		return fmt.format(eye.distanceTo(accessor.getHitResult().facePos));
	}

	public static Text display(int i, int colorIndex) {
		if (IThemeHelper.get().isLightColorScheme())
			colorIndex += 3;
		return new LiteralText(Integer.toString(i)).setStyle(ThemeHelper.colorStyle(colors[colorIndex]));
	}

	public static void xyz(ITooltip tooltip, Vec3i pos) {
		Text display = new TranslatableText("jade.blockpos", display(pos.getX(), 0), display(pos.getY(), 1), display(pos.getZ(), 2));
		String narrate = I18n.translate("narration.jade.blockpos", narrate(pos.getX()), narrate(pos.getY()), narrate(pos.getZ()));
		tooltip.add(IElementHelper.get().text(display).message(narrate));
	}

	public static String narrate(int i) {
		return i >= 0 ? Integer.toString(i) : I18n.translate("narration.jade.negative", -i);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		append(tooltip, accessor, accessor.getPosition(), config);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Entity entity = accessor.getEntity();
		append(tooltip, accessor, new BlockPos(entity.x, entity.y, entity.z), config);
	}

	public void append(ITooltip tooltip, Accessor<?> accessor, BlockPos pos, IPluginConfig config) {
		boolean distance = config.get(Identifiers.CORE_DISTANCE);
		String distanceVal = distance ? distance(accessor) : null;
		String distanceMsg = distance ? I18n.translate("narration.jade.distance", distanceVal) : null;
		if (config.get(Identifiers.CORE_COORDINATES)) {
			if (config.get(Identifiers.CORE_REL_COORDINATES) && Screen.isControlDown()) {
				PlayerEntity player = accessor.getPlayer();
				xyz(tooltip, pos.subtract(new BlockPos(player.x, player.y + player.getEyeHeight(), player.z)));
			} else {
				xyz(tooltip, pos);
			}
			if (distance) {
				tooltip.append(IElementHelper.get().text(new LiteralText(I18n.translate("jade.distance1", distanceVal))).message(distanceMsg));
			}
		} else if (distance) {
			tooltip.add(IElementHelper.get().text(new LiteralText(I18n.translate("jade.distance2", distanceVal))).message(distanceMsg));
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.CORE_DISTANCE;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return -4600;
	}

}
