package snownee.jade.addon.core;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.menu.LockableMenuProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.Nameable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.util.CommonProxy;

public enum ObjectNameProvider
	implements IBlockComponentProvider, IEntityComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	public static Text getEntityName(Entity entity) {
		if (!entity.hasCustomName()) {
			if (WailaClientRegistration.instance().shouldPick(entity)) {
				ItemStack stack = CommonProxy.getEntityPickedResult(entity, Minecraft.getInstance().player, null);
				if (stack != null && !stack.isEmpty()) {
					return new LiteralText(stack.getHoverName());
				}
			}
			if (entity instanceof ItemEntity) {
				return new LiteralText(((ItemEntity) entity).getItem().getHoverName());
			}
		}
		return new LiteralText(entity.getName());
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		Text name = null;
		if (accessor.getServerData().contains("givenName")) {
			try {
				name = Text.Serializer.fromJson(accessor.getServerData().getString("givenName"));
			} catch (Exception e) {
				// no
			}
		}
		if (name == null && WailaClientRegistration.instance().shouldPick(accessor.getBlockState())) {
			ItemStack pick = accessor.getPickedResult();
			if (pick != null && !pick.isEmpty())
				name = new LiteralText(pick.getHoverName());
		}
		if (name == null) {
			String key = accessor.getBlock().getTranslationKey();
			if (I18n.hasTranslation(key + ".name")) {
				name = new LiteralText(accessor.getBlock().getName());
			} else {
				ItemStack pick = accessor.getPickedResult();
				if (pick != null && !pick.isEmpty()) {
					name = new LiteralText(pick.getHoverName());
				} else {
					name = new LiteralText(key);
				}
			}
		}
		tooltip.add(IThemeHelper.get().title(name));
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		Text name = getEntityName(accessor.getEntity());
		tooltip.add(IThemeHelper.get().title(name));
	}

	@Override
	public void appendServerData(NbtCompound data, BlockAccessor accessor) {
		BlockEntity blockEntity = accessor.getBlockEntity();
		if (blockEntity instanceof Nameable) {
			Nameable nameable = (Nameable) blockEntity;
			Text name = null;
			//port: 1.12 has no MenuProvider on the block state, the combined chest provider carries "Large Chest"
			if (blockEntity instanceof ChestBlockEntity && accessor.getBlock() instanceof ChestBlock) {
				LockableMenuProvider provider = ((ChestBlock) accessor.getBlock()).getCombinedMenuProvider(accessor.getLevel(), accessor.getPosition(), false);
				if (provider != null) {
					name = provider.getDisplayName();
				}
			} else if (nameable.hasCustomName()) {
				name = nameable.getDisplayName();
			}
			if (name != null) {
				data.putString("givenName", Text.Serializer.toJson(name));
			}
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.CORE_OBJECT_NAME;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD - 100;
	}

}
