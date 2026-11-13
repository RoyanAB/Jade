package snownee.jade.addon.vanilla;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.living.mob.monster.ZombieVillagerEntity;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

// @MerchantScreen / Villager.getTypeName
public enum VillagerProfessionProvider implements IEntityComponentProvider {

	INSTANCE;

	private static final String[] PROFESSIONS = {"farmer", "librarian", "priest", "smith", "butcher", "nitwit"};

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		int profession;
		//port: 1.12 has no VillagerData, villagers and zombie villagers expose the profession separately
		if (accessor.getEntity() instanceof VillagerEntity) {
			profession = ((VillagerEntity) accessor.getEntity()).getProfession();
		} else if (accessor.getEntity() instanceof ZombieVillagerEntity) {
			profession = ((ZombieVillagerEntity) accessor.getEntity()).getProfession();
		} else {
			return;
		}
		String name = profession >= 0 && profession < PROFESSIONS.length ? PROFESSIONS[profession] : "farmer";
		String key = "entity.Villager." + name;
		Text text = new LiteralText(I18n.hasTranslation(key)
			? I18n.translate(key) : name);
		tooltip.add(text);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_VILLAGER_PROFESSION;
	}
}
