package snownee.jade.addon.universal;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class ItemIterator<T> {
	public static final AtomicLong version = new AtomicLong();
	protected final Function<Object, @Nullable T> containerFinder;
	protected final int fromIndex;
	protected boolean finished;
	protected int currentIndex;

	protected ItemIterator(Function<Object, @Nullable T> containerFinder, int fromIndex) {
		this.containerFinder = containerFinder;
		this.currentIndex = this.fromIndex = fromIndex;
	}

	@Nullable
	public T find(Object target) {
		return containerFinder.apply(target);
	}

	public final boolean isFinished() {
		return finished;
	}

	public long getVersion(T container) {
		return version.getAndIncrement();
	}

	public abstract Stream<ItemStack> populate(T container);

	public void reset() {
		currentIndex = fromIndex;
		finished = false;
	}

	public void afterPopulate(int count) {
		currentIndex += count;
		if (count == 0 || currentIndex >= 10000) {
			finished = true;
		}
	}

	public float getCollectingProgress() {
		return Float.NaN;
	}

	public static abstract class SlottedItemIterator<T> extends ItemIterator<T> {
		protected float progress;

		public SlottedItemIterator(Function<Object, @Nullable T> containerFinder, int fromIndex) {
			super(containerFinder, fromIndex);
		}

		protected abstract int getSlotCount(T container);

		protected abstract ItemStack getItemInSlot(T container, int slot);

		@Override
		public Stream<ItemStack> populate(T container) {
			int slotCount = getSlotCount(container);
			int toIndex = currentIndex + ItemCollector.MAX_SIZE * 2;
			if (toIndex >= slotCount) {
				toIndex = slotCount;
				finished = true;
			}
			progress = (float) (currentIndex - fromIndex) / (slotCount - fromIndex);
			return IntStream.range(currentIndex, toIndex).mapToObj(slot -> getItemInSlot(container, slot));
		}

		@Override
		public float getCollectingProgress() {
			return progress;
		}
	}

	public static class ContainerItemIterator extends SlottedItemIterator<Inventory> {
		public ContainerItemIterator(int fromIndex) {
			this(Inventory.class::cast, fromIndex);
		}

		public ContainerItemIterator(Function<Object, @Nullable Inventory> containerFinder, int fromIndex) {
			super(containerFinder, fromIndex);
		}

		@Override
		protected int getSlotCount(Inventory container) {
			return container.getSize();
		}

		@Override
		protected ItemStack getItemInSlot(Inventory container, int slot) {
			return container.getItem(slot);
		}
	}

	public static abstract class SlotlessItemIterator<T> extends ItemIterator<T> {

		protected SlotlessItemIterator(Function<Object, @Nullable T> containerFinder, int fromIndex) {
			super(containerFinder, fromIndex);
		}

		@Override
		public Stream<ItemStack> populate(T container) {
			return populateRaw(container).skip(currentIndex).limit(ItemCollector.MAX_SIZE * 2L);
		}

		protected abstract Stream<ItemStack> populateRaw(T container);
	}
}
