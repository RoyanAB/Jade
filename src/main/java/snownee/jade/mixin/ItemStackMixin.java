package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TooltipFlag;
import snownee.jade.JadeClient;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(method = "getTooltip", at = @At("RETURN"))
	private void jade$appendModName(PlayerEntity player, TooltipFlag flag, CallbackInfoReturnable<java.util.List<String>> cir) {
		java.util.List<String> tooltip = cir.getReturnValue();
		if (tooltip != null) {
			JadeClient.onTooltip(tooltip, (ItemStack) (Object) this, flag);
		}
	}

}
