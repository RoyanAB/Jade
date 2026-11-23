package snownee.jade.api.callback;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface JadeItemModNameCallback {

	@Nullable
	String gatherItemModName(ItemStack stack);

}
