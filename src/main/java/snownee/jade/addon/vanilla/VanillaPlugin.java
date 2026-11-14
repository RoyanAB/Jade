package snownee.jade.addon.vanilla;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.PaintingEntity;
import net.minecraft.entity.living.ArmorStandEntity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.mob.monster.ZombieVillagerEntity;
import net.minecraft.entity.living.mob.passive.PassiveEntity;
import net.minecraft.entity.living.mob.passive.VillagerEntity;
import net.minecraft.entity.living.mob.passive.animal.AnimalEntity;
import net.minecraft.entity.living.mob.passive.animal.ChickenEntity;
import net.minecraft.entity.living.mob.passive.animal.HorseBaseEntity;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import snownee.jade.JadeClient;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.api.*;

import java.util.Arrays;
import java.util.List;

@WailaPlugin
public class VanillaPlugin implements IWailaPlugin {

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(BrewingStandProvider.INSTANCE, BrewingStandBlockEntity.class);
		registration.registerBlockDataProvider(CommandBlockProvider.INSTANCE, CommandBlockBlockEntity.class);
		registration.registerBlockDataProvider(JukeboxProvider.INSTANCE, JukeboxBlock.JukeboxBlockEntity.class);
		registration.registerBlockDataProvider(RedstoneProvider.INSTANCE, ComparatorBlockEntity.class);
		registration.registerBlockDataProvider(RedstoneProvider.INSTANCE, HopperBlockEntity.class);
		registration.registerBlockDataProvider(FurnaceProvider.INSTANCE, FurnaceBlockEntity.class);
		registration.registerBlockDataProvider(NoteBlockProvider.INSTANCE, NoteBlockBlockEntity.class);

		registration.registerEntityDataProvider(AnimalOwnerProvider.INSTANCE, Entity.class);
		registration.registerEntityDataProvider(StatusEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityDataProvider(MobGrowthProvider.INSTANCE, PassiveEntity.class);
		registration.registerEntityDataProvider(MobBreedingProvider.INSTANCE, AnimalEntity.class);
		registration.registerEntityDataProvider(ChickenEggProvider.INSTANCE, ChickenEntity.class);
		registration.registerEntityDataProvider(ZombieVillagerProvider.INSTANCE, ZombieVillagerEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.addConfig(Identifiers.MC_EFFECTIVE_TOOL, true);
		registration.addConfig(Identifiers.MC_HARVEST_TOOL_NEW_LINE, false);
		registration.addConfig(Identifiers.MC_SHOW_UNBREAKABLE, false);
		registration.addConfig(Identifiers.MC_HARVEST_TOOL_CREATIVE, false);
		registration.addConfig(Identifiers.MC_ANIMAL_OWNER_FETCH_NAMES, true);
		registration.addConfig(Identifiers.MC_BREAKING_PROGRESS, true);

		registration.addConfig(Identifiers.MC_ENTITY_ARMOR_MAX_FOR_RENDER, 20, 0, 100, false);
		registration.addConfig(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER, 40, 0, 100, false);
		registration.addConfig(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE, 10, 5, 30, false);
		registration.addConfig(Identifiers.MC_ENTITY_HEALTH_SHOW_FRACTIONS, false);

		registration.registerBlockComponent(BrewingStandProvider.INSTANCE, BrewingStandBlock.class);
		registration.registerEntityComponent(HorseStatsProvider.INSTANCE, HorseBaseEntity.class);
		registration.registerEntityComponent(ItemFrameProvider.INSTANCE, ItemFrameEntity.class);
		registration.registerEntityComponent(StatusEffectsProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityComponent(MobGrowthProvider.INSTANCE, PassiveEntity.class);
		registration.registerEntityComponent(MobBreedingProvider.INSTANCE, AnimalEntity.class);
		registration.registerBlockComponent(TNTStabilityProvider.INSTANCE, TntBlock.class);
		registration.registerEntityComponent(ArmorStandProvider.INSTANCE, ArmorStandEntity.class);
		registration.registerEntityComponent(PaintingProvider.INSTANCE, PaintingEntity.class);
		registration.registerEntityComponent(ChickenEggProvider.INSTANCE, ChickenEntity.class);
		registration.registerBlockComponent(HarvestToolProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(CommandBlockProvider.INSTANCE, CommandBlock.class);
		registration.registerBlockComponent(EnchantmentPowerProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(TotalEnchantmentPowerProvider.INSTANCE, EnchantingTableBlock.class);
		registration.registerBlockComponent(PlayerHeadProvider.INSTANCE, SkullBlock.class);
		registration.registerBlockIcon(ItemBERProvider.INSTANCE, SkullBlock.class);
		registration.registerEntityComponent(ItemTooltipProvider.INSTANCE, ItemEntity.class);
		registration.registerBlockComponent(FurnaceProvider.INSTANCE, FurnaceBlock.class);
		registration.registerEntityComponent(AnimalOwnerProvider.INSTANCE, Entity.class);
		registration.registerEntityComponent(FallingBlockProvider.INSTANCE, FallingBlockEntity.class);
		registration.registerEntityIcon(FallingBlockProvider.INSTANCE, FallingBlockEntity.class);
		registration.registerEntityComponent(EntityHealthProvider.INSTANCE, LivingEntity.class);
		registration.registerEntityComponent(EntityArmorProvider.INSTANCE, LivingEntity.class);
		registration.registerBlockComponent(RedstoneProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(CropProgressProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(JukeboxProvider.INSTANCE, JukeboxBlock.class);
		registration.registerBlockComponent(MobSpawnerProvider.INSTANCE, MobSpawnerBlock.class);
		registration.registerEntityComponent(MobSpawnerProvider.INSTANCE, SpawnerMinecartEntity.class);
		registration.registerBlockComponent(NoteBlockProvider.INSTANCE, NoteBlock.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, VillagerEntity.class);
		registration.registerEntityComponent(VillagerProfessionProvider.INSTANCE, ZombieVillagerEntity.class);
		registration.registerEntityComponent(ZombieVillagerProvider.INSTANCE, ZombieVillagerEntity.class);

		registration.addAfterRenderCallback(100, JadeClient::drawBreakingProgress);
		registration.markAsClientFeature(Identifiers.MC_EFFECTIVE_TOOL);
		registration.markAsClientFeature(Identifiers.MC_HARVEST_TOOL_NEW_LINE);
		registration.markAsClientFeature(Identifiers.MC_SHOW_UNBREAKABLE);
		registration.markAsClientFeature(Identifiers.MC_HARVEST_TOOL_CREATIVE);
		registration.markAsClientFeature(Identifiers.MC_BREAKING_PROGRESS);
		registration.markAsClientFeature(Identifiers.MC_ENTITY_ARMOR_MAX_FOR_RENDER);
		registration.markAsClientFeature(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER);
		registration.markAsClientFeature(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
		registration.markAsClientFeature(Identifiers.MC_ENTITY_HEALTH_SHOW_FRACTIONS);
		registration.markAsClientFeature(Identifiers.MC_HORSE_STATS);
		registration.markAsClientFeature(Identifiers.MC_ITEM_FRAME);
		registration.markAsClientFeature(Identifiers.MC_TNT_STABILITY);
		registration.markAsClientFeature(Identifiers.MC_ARMOR_STAND);
		registration.markAsClientFeature(Identifiers.MC_PAINTING);
		registration.markAsClientFeature(Identifiers.MC_HARVEST_TOOL);
		registration.markAsClientFeature(Identifiers.MC_ENCHANTMENT_POWER);
		registration.markAsClientFeature(Identifiers.MC_TOTAL_ENCHANTMENT_POWER);
		registration.markAsClientFeature(Identifiers.MC_PLAYER_HEAD);
		registration.markAsClientFeature(Identifiers.MC_ITEM_TOOLTIP);
		registration.markAsClientFeature(Identifiers.MC_ANIMAL_OWNER_FETCH_NAMES);
		registration.markAsClientFeature(Identifiers.MC_ENTITY_HEALTH);
		registration.markAsClientFeature(Identifiers.MC_ENTITY_ARMOR);
		registration.markAsClientFeature(Identifiers.MC_CROP_PROGRESS);
		registration.markAsClientFeature(Identifiers.MC_MOB_SPAWNER);
		registration.markAsClientFeature(Identifiers.MC_NOTE_BLOCK);
		registration.markAsClientFeature(Identifiers.MC_VILLAGER_PROFESSION);

		Text block = new TranslatableText("config.jade.plugin_minecraft.block");
		Text entity = new TranslatableText("config.jade.plugin_minecraft.entity");
		List<Text> both = Arrays.asList(block, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_ANIMAL_OWNER, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_ARMOR_STAND, both);
		registration.setConfigCategoryOverride(Identifiers.MC_BREWING_STAND, block);
		registration.setConfigCategoryOverride(Identifiers.MC_CHICKEN_EGG, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_COMMAND_BLOCK, block);
		registration.setConfigCategoryOverride(Identifiers.MC_CROP_PROGRESS, block);
		registration.setConfigCategoryOverride(Identifiers.MC_ENCHANTMENT_POWER, block);
		registration.setConfigCategoryOverride(Identifiers.MC_TOTAL_ENCHANTMENT_POWER, block);
		registration.setConfigCategoryOverride(Identifiers.MC_ENTITY_ARMOR, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_ENTITY_HEALTH, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_FURNACE, block);
		registration.setConfigCategoryOverride(Identifiers.MC_BREAKING_PROGRESS, block);
		registration.setConfigCategoryOverride(Identifiers.MC_HARVEST_TOOL, block);
		registration.setConfigCategoryOverride(Identifiers.MC_HORSE_STATS, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_ITEM_FRAME, both);
		registration.setConfigCategoryOverride(Identifiers.MC_ITEM_TOOLTIP, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_JUKEBOX, block);
		registration.setConfigCategoryOverride(Identifiers.MC_MOB_BREEDING, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_MOB_GROWTH, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_MOB_SPAWNER, block);
		registration.setConfigCategoryOverride(Identifiers.MC_NOTE_BLOCK, block);
		registration.setConfigCategoryOverride(Identifiers.MC_PAINTING, both);
		registration.setConfigCategoryOverride(Identifiers.MC_PLAYER_HEAD, block);
		registration.setConfigCategoryOverride(Identifiers.MC_POTION_EFFECTS, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_REDSTONE, block);
		registration.setConfigCategoryOverride(Identifiers.MC_TNT_STABILITY, block);
		registration.setConfigCategoryOverride(Identifiers.MC_VILLAGER_PROFESSION, entity);
		registration.setConfigCategoryOverride(Identifiers.MC_ZOMBIE_VILLAGER, entity);
	}
}
