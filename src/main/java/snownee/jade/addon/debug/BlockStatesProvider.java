package snownee.jade.addon.debug;

import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.property.BooleanProperty;
import net.minecraft.block.state.property.Property;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

import java.util.Collection;

public enum BlockStatesProvider implements IBlockComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		Collection<Property<?>> properties = state.properties();
		if (properties.isEmpty())
			return;
		IElementHelper helper = IElementHelper.get();
		IThemeHelper t = IThemeHelper.get();
		ITooltip box = helper.tooltip();
		for (Property<?> property : properties) {
			Comparable<?> value = getValue(state, property);
			Text valueText = new LiteralText(" " + value);
			if (property instanceof BooleanProperty)
				valueText = value == Boolean.TRUE ? t.success(valueText) : t.danger(valueText);
			box.add(new LiteralText(property.getName() + ":").append(valueText));
		}
		tooltip.add(helper.box(box, BoxStyle.getNestedBox()));
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> T getValue(BlockState state, Property<?> property) {
		return state.get((Property<T>) property);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.DEBUG_BLOCK_STATES;
	}

	@Override
	public int getDefaultPriority() {
		return -4500;
	}

	@Override
	public boolean enabledByDefault() {
		return false;
	}
}
