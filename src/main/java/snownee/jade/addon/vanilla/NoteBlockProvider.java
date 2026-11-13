package snownee.jade.addon.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.NoteBlockBlockEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

public enum NoteBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

	INSTANCE;

	private static final String[] PITCH = {"F♯/G♭", "G", "G♯/A♭", "A", "A♯/B♭", "B", "C", "C♯/D♭", "D", "D♯/E♭", "E", "F"};
	private static final Formatting[] OCTAVE = {Formatting.WHITE, Formatting.YELLOW, Formatting.GOLD};
	private static final Formatting[] OCTAVE_LIGHT = {Formatting.DARK_PURPLE, Formatting.DARK_BLUE, Formatting.BLUE};
	private static final String[] INSTRUMENTS = {"harp", "basedrum", "snare", "hat", "bass", "flute", "bell", "guitar", "chime", "xylophone"};

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		int note;
		int instrument;
		NbtCompound data = accessor.getServerData();
		//port: 1.12 keeps the note in the block entity and never syncs it after tuning,
		//so the server sends the current value along with the instrument
		if (data.contains("Note")) {
			note = data.getInt("Note");
			instrument = data.getInt("Instrument");
		} else if (accessor.getBlockEntity() instanceof NoteBlockBlockEntity) {
			note = ((NoteBlockBlockEntity) accessor.getBlockEntity()).note;
			instrument = getInstrument(accessor.getLevel(), accessor.getPosition());
		} else {
			return;
		}
		String key = "jade.instrument." + INSTRUMENTS[instrument];
		String name = I18n.hasTranslation(key)
			? I18n.translate(key) : INSTRUMENTS[instrument];
		String pitch = PITCH[note % PITCH.length];
		Formatting octave = (IThemeHelper.get().isLightColorScheme() ? OCTAVE_LIGHT : OCTAVE)[note / PITCH.length];
		tooltip.add(new LiteralText(name + " " + octave + pitch));
	}

	@Override
	public void appendServerData(NbtCompound data, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof NoteBlockBlockEntity) {
			data.putInt("Note", ((NoteBlockBlockEntity) accessor.getBlockEntity()).note);
			data.putInt("Instrument", getInstrument(accessor.getLevel(), accessor.getPosition()));
		}
	}

	/**
	 * Mirrors {@code NoteBlockBlockEntity#playNote}: the block checks come after the material
	 * checks and overwrite them, so e.g. a bone block (Material.STONE) ends up as xylophone.
	 */
	private static int getInstrument(World world, BlockPos pos) {
		if (world.getBlockState(pos.up()).getMaterial() != Material.AIR) {
			return 0;
		}
		BlockState state = world.getBlockState(pos.down());
		Block block = state.getBlock();
		Material material = state.getMaterial();
		int instrument = 0;
		if (material == Material.STONE) instrument = 1;
		if (material == Material.SAND) instrument = 2;
		if (material == Material.GLASS) instrument = 3;
		if (material == Material.WOOD) instrument = 4;
		if (block == Blocks.CLAY) instrument = 5;
		if (block == Blocks.GOLD_BLOCK) instrument = 6;
		if (block == Blocks.WOOL) instrument = 7;
		if (block == Blocks.PACKED_ICE) instrument = 8;
		if (block == Blocks.BONE_BLOCK) instrument = 9;
		return instrument;
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_NOTE_BLOCK;
	}
}
