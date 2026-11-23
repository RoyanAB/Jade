package snownee.jade.addon.vanilla;

import net.minecraft.entity.living.attribute.EntityAttributes;
import net.minecraft.entity.living.mob.passive.animal.HorseBaseEntity;
import net.minecraft.entity.living.mob.passive.animal.LlamaEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.overlay.DisplayHelper;

public enum HorseStatsProvider implements IEntityComponentProvider {

	INSTANCE;

	private static final double MAX_JUMP_HEIGHT = getJumpHeight(2.0);
	//port: 1.12 randomizes horse speed in 0.1125~0.3375 (HorseBaseEntity#getChildMovementSpeedBonus)
	private static final double MAX_MOVEMENT_SPEED = 0.3375 * 42.16;

	private static Text switchText(String key, boolean showMax, double value, double max) {
		IThemeHelper t = IThemeHelper.get();
		Text valueText = t.info(DisplayHelper.dfCommas.format(value));
		if (showMax) {
			return new TranslatableText(key, new TranslatableText("jade.fraction", valueText, DisplayHelper.dfCommas.format(max)));
		}
		return new TranslatableText(key, valueText);
	}

	private static double getJumpHeight(double jumpStrength) {
		return -0.1817584952 * jumpStrength * jumpStrength * jumpStrength + 3.689713992 * jumpStrength * jumpStrength + 2.128599134 * jumpStrength - 0.343930367;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		HorseBaseEntity horse = (HorseBaseEntity) accessor.getEntity();
		boolean showMax = accessor.showDetails();
		if (horse instanceof LlamaEntity) {
			tooltip.add(switchText("jade.llamaStrength", showMax, ((LlamaEntity) horse).getStrength(), 5));
			return;
		}
		double jumpStrength = horse.getCustomJumpStrength();
		double jumpHeight = getJumpHeight(jumpStrength);
		double speed = horse.getAttribute(EntityAttributes.MOVEMENT_SPEED).get() * 42.16;
		tooltip.add(switchText("jade.horseStat.jump", showMax, jumpHeight, MAX_JUMP_HEIGHT));
		tooltip.add(switchText("jade.horseStat.speed", showMax, speed, MAX_MOVEMENT_SPEED));
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_HORSE_STATS;
	}
}
