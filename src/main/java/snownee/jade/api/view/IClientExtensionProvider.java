package snownee.jade.api.view;

import snownee.jade.api.Accessor;
import snownee.jade.api.IJadeProvider;

import java.util.List;

public interface IClientExtensionProvider<IN, OUT> extends IJadeProvider {

	List<ClientViewGroup<OUT>> getClientGroups(Accessor<?> accessor, List<ViewGroup<IN>> groups);

}
