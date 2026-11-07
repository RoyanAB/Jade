package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.overlay.BossEventOverlay;
import snownee.jade.Jade;
import snownee.jade.api.config.IWailaConfig.BossBarOverlapMode;
import snownee.jade.overlay.OverlayRenderer;

@Mixin(BossEventOverlay.class)
public class BossHealthOverlayMixin {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void jade$render(CallbackInfo ci) {
		BossBarOverlapMode mode = Jade.CONFIG.get().getGeneral().getBossBarOverlapMode();
		if (mode == BossBarOverlapMode.HIDE_BOSS_BAR && OverlayRenderer.shown) {
			ci.cancel();
		}
	}

}
