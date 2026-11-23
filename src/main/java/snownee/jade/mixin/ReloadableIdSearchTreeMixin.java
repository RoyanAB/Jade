package snownee.jade.mixin;

import net.minecraft.client.search.ReloadableIdSearchTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.JadeClient;

/**
 * 1.12.2 builds the creative search index from ItemStack tooltips. Jade's mod name line must not leak
 * into the search index, hence hide it while the tree is (re)built.
 */
@Mixin(ReloadableIdSearchTree.class)
public class ReloadableIdSearchTreeMixin {

	@Inject(method = "reload", at = @At("HEAD"))
	private void jade$preReload(CallbackInfo ci) {
		JadeClient.hideModName = true;
	}

	@Inject(method = "reload", at = @At("RETURN"))
	private void jade$postReload(CallbackInfo ci) {
		JadeClient.hideModName = false;
	}

	@Inject(method = "add", at = @At("HEAD"))
	private void jade$preAdd(Object value, CallbackInfo ci) {
		JadeClient.hideModName = true;
	}

	@Inject(method = "add", at = @At("RETURN"))
	private void jade$postAdd(Object value, CallbackInfo ci) {
		JadeClient.hideModName = false;
	}
}
