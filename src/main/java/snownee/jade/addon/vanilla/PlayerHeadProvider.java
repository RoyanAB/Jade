package snownee.jade.addon.vanilla;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.util.CommonProxy;

public enum PlayerHeadProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() instanceof SkullBlockEntity) {
			GameProfile profile = ((SkullBlockEntity) accessor.getBlockEntity()).getProfile();
			if (profile == null) {
				return;
			}
			String name = profile.getName();
			if (name == null) {
				name = CommonProxy.getLastKnownUsername(profile.getId());
			}
			if (name == null || name.trim().isEmpty()) {
				return;
			}
			String text = net.minecraft.client.resource.language.I18n.translate("item.skull.player.name", name);
			tooltip.replace(Identifiers.CORE_OBJECT_NAME, IThemeHelper.get().title(new LiteralText(text)));
		}
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_PLAYER_HEAD;
	}
}
