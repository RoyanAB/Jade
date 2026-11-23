package snownee.jade.mixin;

import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.command.JadeClientCommand;

@Mixin(LocalClientPlayerEntity.class)
public class LocalClientPlayerEntityMixin {

	@Inject(method = "sendChat(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
	private void jade$clientCommand(String message, CallbackInfo ci) {
		if (JadeClientCommand.tryRun(message)) {
			ci.cancel();
		}
	}

}
