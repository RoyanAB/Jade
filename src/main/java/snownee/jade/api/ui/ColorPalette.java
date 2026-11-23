package snownee.jade.api.ui;

public class ColorPalette {
	public static final ColorPalette DEFAULT = new ColorPalette(0xFFA0A0A0, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF55FF55, 0xFFFFF3CD, 0xFFFF5555, 0xFFAA0000);

	private final int normal;
	private final int info;
	private final int title;
	private final int success;
	private final int warning;
	private final int danger;
	private final int failure;

	public ColorPalette(int normal, int info, int title, int success, int warning, int danger, int failure) {
		this.normal = normal;
		this.info = info;
		this.title = title;
		this.success = success;
		this.warning = warning;
		this.danger = danger;
		this.failure = failure;
	}

	public int normal() {
		return normal;
	}

	public int info() {
		return info;
	}

	public int title() {
		return title;
	}

	public int success() {
		return success;
	}

	public int warning() {
		return warning;
	}

	public int danger() {
		return danger;
	}

	public int failure() {
		return failure;
	}

	public int get(MessageType type) {
		switch (type) {
			case INFO:
				return info;
			case TITLE:
				return title;
			case SUCCESS:
				return success;
			case WARNING:
				return warning;
			case DANGER:
				return danger;
			case FAILURE:
				return failure;
			default:
				return normal;
		}
	}
}
