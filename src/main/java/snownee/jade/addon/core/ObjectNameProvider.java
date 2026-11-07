package snownee.jade.addon.core;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.Nameable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.impl.WailaClientRegistration;

public enum ObjectNameProvider
		implements IBlockComponentProvider, IEntityComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	public static Text getEntityName(Entity entity) {
		if (!entity.hasCustomName()) {
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
		if (name == null && accessor.isFakeBlock()) {
			name = new LiteralText(accessor.getFakeBlock().getHoverName());
		}
		if (name == null && WailaClientRegistration.instance().shouldPick(accessor.getBlockState())) {
			ItemStack pick = accessor.getPickedResult();
			if (pick != null && !pick.isEmpty())
				name = new LiteralText(pick.getHoverName());
		}
		if (name == null) {
			String key = accessor.getBlock().getTranslationKey();
			if (I18n.hasTranslation(key)) {
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
			if (nameable.hasCustomName()) {
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
