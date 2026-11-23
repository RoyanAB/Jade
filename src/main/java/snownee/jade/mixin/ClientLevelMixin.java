package snownee.jade.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import snownee.jade.util.UsernameCache;

@Mixin(ClientWorld.class)
public class ClientLevelMixin {

	@Inject(method = "addEntity", at = @At("HEAD"))
	private void jade$addEntity(Entity entity, CallbackInfoReturnable<Boolean> ci) {
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			UsernameCache.setUsername(player.getUuid(), player.getGameProfile().getName());
		}
	}

}
