package snownee.jade.mixin;

import net.minecraft.client.gui.overlay.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccess {

	@Accessor("visible")
	boolean isVisible();

}
