package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.overlay.PlayerTabOverlay;

@Mixin(PlayerTabOverlay.class)
public interface PlayerTabOverlayAccess {

	@Accessor("visible")
	boolean isVisible();

}
