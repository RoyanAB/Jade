package snownee.jade.addon.universal;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.view.ViewGroup;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class ItemCollector<T> {
	public static final int MAX_SIZE = 54;
	public static final ItemCollector<?> EMPTY = new ItemCollector<>(null);
	private static final Predicate<ItemStack> NON_EMPTY = stack -> {
		if (stack.isEmpty()) {
			return false;
		}
		NbtCompound tag = stack.getNbt();
		if (tag != null && tag.contains("CustomModelData")) {
			for (String key : tag.getKeys()) {
				if (key.toLowerCase(Locale.ENGLISH).endsWith("clear") && tag.getBoolean(key)) {
					return false;
				}
			}
		}
		return true;
	};
	private final Object2IntLinkedOpenHashMap<ItemDefinition> items = new Object2IntLinkedOpenHashMap<>();
	private final ItemIterator<T> iterator;
	public long version;
	public long lastTimeFinished;
	public List<ViewGroup<ItemStack>> mergedResult;

	public ItemCollector(ItemIterator<T> iterator) {
		this.iterator = iterator;
	}

	public List<ViewGroup<ItemStack>> update(Object target, long gameTime) {
		if (iterator == null) {
			return null;
		}
		T container = iterator.find(target);
		if (container == null) {
			return null;
		}
		long currentVersion = iterator.getVersion(container);
		if (mergedResult != null && iterator.isFinished()) {
			if (version == currentVersion) {
				return mergedResult; // content not changed
			}
			if (lastTimeFinished + 5 > gameTime) {
				return mergedResult; // avoid update too frequently
			}
			iterator.reset();
		}
		AtomicInteger count = new AtomicInteger();
		iterator.populate(container).forEach(stack -> {
			count.incrementAndGet();
			if (NON_EMPTY.test(stack)) {
				ItemDefinition def = new ItemDefinition(stack);
				items.addTo(def, stack.getSize());
			}
		});
		iterator.afterPopulate(count.get());
		if (mergedResult != null && !iterator.isFinished()) {
			updateCollectingProgress(mergedResult.get(0));
			return mergedResult;
		}
		List<ItemStack> partialResult = new ArrayList<>(Math.min(MAX_SIZE, items.size()));
		for (Object2IntMap.Entry<ItemDefinition> entry : items.object2IntEntrySet()) {
			if (partialResult.size() >= MAX_SIZE) {
				break;
			}
			partialResult.add(entry.getKey().toStack(entry.getIntValue()));
		}
		List<ViewGroup<ItemStack>> groups = Collections.singletonList(updateCollectingProgress(new ViewGroup<>(partialResult)));
		if (iterator.isFinished()) {
			mergedResult = groups;
			version = currentVersion;
			lastTimeFinished = gameTime;
			items.clear();
		}
		return groups;
	}

	protected ViewGroup<ItemStack> updateCollectingProgress(ViewGroup<ItemStack> group) {
		float progress = iterator.getCollectingProgress();
		NbtCompound data = group.getExtraData();
		if (Float.isNaN(progress)) {
			progress = 0;
		}
		if (progress >= 1) {
			data.remove("Collecting");
		} else {
			data.putFloat("Collecting", progress);
		}
		return group;
	}

	// 1.12 has no records, so equals/hashCode are written out by hand; the collector uses this as a
	// map key, value equality is required
	public static class ItemDefinition {
		public final Item item;
		//port: 1.12 shares one Item between all wood types, the metadata is what tells acacia from oak
		public final int metadata;
		@Nullable
		public final NbtCompound tag;

		ItemDefinition(ItemStack stack) {
			this(stack.getItem(), stack.getMetadata(), stack.getNbt());
		}

		public ItemDefinition(Item item, int metadata, @Nullable NbtCompound tag) {
			this.item = item;
			this.metadata = metadata;
			this.tag = tag;
		}

		public ItemStack toStack(int count) {
			ItemStack stack = new ItemStack(item, 1, metadata);
			stack.setSize(count);
			stack.setNbt(tag);
			return stack;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ItemDefinition)) {
				return false;
			}
			ItemDefinition that = (ItemDefinition) o;
			return item == that.item && metadata == that.metadata && Objects.equals(tag, that.tag);
		}

		@Override
		public int hashCode() {
			int result = 31 * System.identityHashCode(item) + metadata;
			return 31 * result + Objects.hashCode(tag);
		}
	}
}
