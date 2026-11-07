package snownee.jade.api.callback;

import snownee.jade.api.Accessor;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.TooltipRect;

@FunctionalInterface
public interface JadeAfterRenderCallback {

	void afterRender(IBoxElement rootElement, TooltipRect rect, Accessor<?> accessor);

}
