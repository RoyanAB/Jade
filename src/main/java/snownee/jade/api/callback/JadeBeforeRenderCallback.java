package snownee.jade.api.callback;

import snownee.jade.api.Accessor;
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.TooltipRect;

@FunctionalInterface
public interface JadeBeforeRenderCallback {

	boolean beforeRender(IBoxElement rootElement, TooltipRect rect, Accessor<?> accessor);

}
