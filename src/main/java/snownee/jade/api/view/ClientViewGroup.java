package snownee.jade.api.view;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.*;
import snownee.jade.impl.ui.HorizontalLineElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ClientViewGroup<T> {

	public final List<T> views;
	@Nullable
	public Text title;
	public MessageType messageType = MessageType.NORMAL;
	public float boxProgress;
	@Nullable
	public NbtCompound extraData;

	public ClientViewGroup(List<T> views) {
		this.views = views;
	}

	public static <IN, OUT> List<ClientViewGroup<OUT>> map(List<ViewGroup<IN>> groups, Function<IN, OUT> itemFactory, @Nullable BiConsumer<ViewGroup<IN>, ClientViewGroup<OUT>> clientGroupDecorator) {
		List<ClientViewGroup<OUT>> result = new ArrayList<>(groups.size());
		for (ViewGroup<IN> in : groups) {
			List<OUT> views = new ArrayList<>(in.views.size());
			for (IN view : in.views) {
				OUT out = itemFactory.apply(view);
				if (out != null) {
					views.add(out);
				}
			}
			ClientViewGroup<OUT> group = new ClientViewGroup<>(views);
			NbtCompound data = in.extraData;
			if (data != null) {
				if (data.contains("Progress")) {
					group.boxProgress = data.getFloat("Progress");
				}
				if (data.contains("MessageType")) {
					group.messageType = MessageType.parse(data.getString("MessageType"));
				}
			}
			if (clientGroupDecorator != null) {
				clientGroupDecorator.accept(in, group);
			}
			group.extraData = data;
			result.add(group);
		}
		return result;
	}

	public static <T> void tooltip(ITooltip tooltip, List<ClientViewGroup<T>> groups, boolean renderGroup, BiConsumer<ITooltip, ClientViewGroup<T>> consumer) {
		for (ClientViewGroup<T> group : groups) {
			ITooltip theTooltip = renderGroup ? IElementHelper.get().tooltip() : tooltip;
			consumer.accept(theTooltip, group);
			if (renderGroup) {
				BoxStyle boxStyle = BoxStyle.getViewGroup().clone();
				IBoxElement box = IElementHelper.get().box(theTooltip, boxStyle);
				box.setBoxProgress(group.messageType, group.boxProgress);
				if (group.title != null) {
					box.setPadding(Direction2D.UP, 0);
					box.size(null);
				}
				tooltip.add(box);
				if (box.getStyle().hasRoundCorner()) {
					tooltip.setLineMargin(-1, Direction2D.UP, 3);
					tooltip.setLineMargin(-1, Direction2D.DOWN, 3);
				}
			}
		}
	}

	public boolean shouldRenderGroup() {
		return title != null || boxProgress > 0;
	}

	public void renderHeader(ITooltip tooltip) {
		if (title != null) {
			tooltip.add(new HorizontalLineElement());
			tooltip.append(IElementHelper.get().text(title).scale(0.5F));
			tooltip.append(new HorizontalLineElement());
		}
	}
}
