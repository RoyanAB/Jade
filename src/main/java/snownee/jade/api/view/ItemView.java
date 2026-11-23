package snownee.jade.api.view;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.ui.IElement;
import snownee.jade.util.CommonProxy;

import java.util.List;
import java.util.Objects;

public class ItemView {

	public ItemStack item;
	@Nullable
	public String amountText;
	@Nullable
	public List<IElement> description;

	public ItemView(ItemStack item) {
		Objects.requireNonNull(item);
		this.item = item;
	}

	public static List<ViewGroup<ItemStack>> groupOf(Inventory container, Accessor<?> accessor) {
		return CommonProxy.containerGroup(container, accessor);
	}

	/**
	 * @param storage a platform specific item storage. 1.12 has no item storage API, so anything that
	 *                is an {@link Inventory} is accepted here.
	 */
	public static List<ViewGroup<ItemStack>> groupOf(Object storage, Accessor<?> accessor) {
		return CommonProxy.storageGroup(storage, accessor);
	}

	public ItemView amountText(String amountText) {
		this.amountText = amountText;
		return this;
	}

	public ItemView description(List<IElement> description) {
		this.description = description;
		return this;
	}

}
