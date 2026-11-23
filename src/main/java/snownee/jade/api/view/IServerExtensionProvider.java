package snownee.jade.api.view;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.IJadeProvider;

import java.util.List;

public interface IServerExtensionProvider<IN, OUT> extends IJadeProvider {

	@Nullable
	List<ViewGroup<OUT>> getGroups(Accessor<?> accessor, IN target);

}
