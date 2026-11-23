package snownee.jade.addon.vanilla;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

import java.util.Collection;

public enum StatusEffectsProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

	INSTANCE;

	public static Text getEffectName(StatusEffectInstance effect) {
		String name = I18n.translate(effect.getEffect().getTranslationKey());
		if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
			name += " " + I18n.translate("enchantment.level." + (effect.getAmplifier() + 1));
		}
		return new LiteralText(name);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("StatusEffects")) {
			return;
		}
		IElementHelper helper = IElementHelper.get();
		ITooltip box = helper.tooltip();
		NbtList list = accessor.getServerData().getList("StatusEffects", 10);
		for (int i = 0; i < list.size(); i++) {
			NbtCompound compound = list.getCompound(i);
			StatusEffectInstance effect = StatusEffectInstance.fromNbt(compound);
			if (effect == null) {
				continue;
			}
			String duration;
			if (effect.getDuration() >= 1000000) {
				duration = "∞";
			} else {
				int seconds = effect.getDuration() / 20;
				duration = String.format("%d:%02d", seconds / 60, seconds % 60);
			}
			Text s = new TranslatableText("jade.potion", getEffectName(effect), duration);
			box.add(effect.getEffect().isHarmful() ? IThemeHelper.get().danger(s) : IThemeHelper.get().success(s));
		}
		tooltip.add(helper.box(box, BoxStyle.getNestedBox()));
	}

	@Override
	public void appendServerData(NbtCompound tag, EntityAccessor accessor) {
		Collection<StatusEffectInstance> effects = ((LivingEntity) accessor.getEntity()).getStatusEffects();
		if (effects.isEmpty()) {
			return;
		}
		NbtList list = new NbtList();
		for (StatusEffectInstance effect : effects) {
			list.addElement(effect.toNbt(new NbtCompound()));
		}
		tag.put("StatusEffects", list);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_POTION_EFFECTS;
	}
}
