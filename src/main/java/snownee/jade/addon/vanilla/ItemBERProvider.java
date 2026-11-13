package snownee.jade.addon.vanilla;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resource.Identifier;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum ItemBERProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
		if (accessor.getBlockEntity() != null) {
			ItemStack stack = accessor.getPickedResult();
			if (stack.isEmpty()) {
				return null;
			}
			//port: 1.12 has no Minecraft#addCustomNbtData. Only skulls need extra data here, the
			//"skull" item model is builtin/entity and BlockEntityItemRenderer reads SkullOwner
			if (accessor.getBlockEntity() instanceof SkullBlockEntity) {
				addSkullOwner(stack, (SkullBlockEntity) accessor.getBlockEntity());
			}
			return IElementHelper.get().item(stack);
		}
		return null;
	}

	private static void addSkullOwner(ItemStack stack, SkullBlockEntity skull) {
		GameProfile profile = skull.getProfile();
		if (profile == null) {
			return;
		}
		NbtCompound nbt = stack.hasNbt() ? stack.getNbt().copy() : new NbtCompound();
		nbt.put("SkullOwner", NbtUtils.writeProfile(new NbtCompound(), profile));
		stack.setNbt(nbt);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_ITEM_BER;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

}
