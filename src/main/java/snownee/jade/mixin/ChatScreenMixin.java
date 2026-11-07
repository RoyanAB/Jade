package snownee.jade.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.util.ClientProxy;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

	@Inject(method = "render(IIF)V", at = @At("TAIL"))
	private void jade$renderOverlay(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		ClientProxy.onRenderTick();
	}

}
