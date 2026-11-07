package snownee.jade.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.util.ClientProxy;

@Mixin(GameGui.class)
public class GameGuiMixin {

	@Inject(method = "render(F)V", at = @At("TAIL"))
	private void jade$renderOverlay(float tickDelta, CallbackInfo ci) {
		if (Minecraft.getInstance().screen == null) {
			ClientProxy.onRenderTick();
		}
	}

}
