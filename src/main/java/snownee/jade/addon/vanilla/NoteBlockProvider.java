package snownee.jade.addon.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.NoteBlockBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;

public enum NoteBlockProvider implements IBlockComponentProvider {

	INSTANCE;

	private static final String[] PITCH = {"F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B", "C", "C♯/D♭", "D", "D♯/E♭", "E", "F"};
	private static final Formatting[] OCTAVE = {Formatting.WHITE, Formatting.YELLOW, Formatting.GOLD};
	private static final Formatting[] OCTAVE_LIGHT = {Formatting.DARK_PURPLE, Formatting.DARK_BLUE, Formatting.BLUE};
	private static final String[] INSTRUMENTS = {"harp", "basedrum", "snare", "hat", "bass", "flute", "bell", "guitar", "chime", "xylophone"};

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (!(accessor.getBlockEntity() instanceof NoteBlockBlockEntity)) {
			return;
		}
		int note = ((NoteBlockBlockEntity) accessor.getBlockEntity()).note;
		World world = accessor.getLevel();
		BlockPos pos = accessor.getPosition();
		int instrument = getInstrument(world, pos);
		String key = "jade.instrument." + INSTRUMENTS[instrument];
		String name = net.minecraft.client.resource.language.I18n.hasTranslation(key)
			? net.minecraft.client.resource.language.I18n.translate(key) : INSTRUMENTS[instrument];
		String pitch = PITCH[note % PITCH.length];
		Formatting octave = (snownee.jade.api.theme.IThemeHelper.get().isLightColorScheme() ? OCTAVE_LIGHT : OCTAVE)[note / PITCH.length];
		tooltip.add(new LiteralText(name + " " + octave + pitch));
	}

	private static int getInstrument(World world, BlockPos pos) {
		if (world.getBlockState(pos.up()).getMaterial() != Material.AIR) {
			return 0;
		}
		net.minecraft.block.state.BlockState state = world.getBlockState(pos.down());
		Block block = state.getBlock();
		Material material = state.getMaterial();
		if (material == Material.STONE) return 1;
		if (material == Material.SAND) return 2;
		if (material == Material.GLASS) return 3;
		if (material == Material.WOOD) return 4;
		if (block == Blocks.CLAY) return 5;
		if (block == Blocks.GOLD_BLOCK) return 6;
		if (block == Blocks.WOOL) return 7;
		if (block == Blocks.PACKED_ICE) return 8;
		if (block == Blocks.BONE_BLOCK) return 9;
		return 0;
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_NOTE_BLOCK;
	}
}
