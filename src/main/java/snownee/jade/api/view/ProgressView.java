package snownee.jade.api.view;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.ProgressStyle;
import snownee.jade.impl.ui.SlimProgressStyle;

import java.util.Objects;

public class ProgressView {

	public ProgressStyle style;
	public float progress;
	@Nullable
	public Text text;

	public ProgressView(ProgressStyle style) {
		this.style = style;
		Objects.requireNonNull(style);
	}

	public static ProgressView read(NbtCompound tag) {
		ProgressView progressView = new ProgressView(new SlimProgressStyle());
		progressView.progress = tag.getFloat("Progress");
		return progressView;
	}

	public static NbtCompound create(float progress) {
		NbtCompound tag = new NbtCompound();
		tag.putFloat("Progress", progress);
		return tag;
	}

}
