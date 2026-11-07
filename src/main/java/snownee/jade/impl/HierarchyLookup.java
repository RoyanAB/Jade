package snownee.jade.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.*;
import net.minecraft.resource.Identifier;
import snownee.jade.Jade;
import snownee.jade.api.IJadeProvider;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class HierarchyLookup<T extends IJadeProvider> {

	private final Class<?> baseClass;
	private final Cache<Class<?>, List<T>> resultCache = CacheBuilder.newBuilder().build();
	private final boolean singleton;
	private ListMultimap<Class<?>, T> objects = ArrayListMultimap.create();

	public HierarchyLookup(Class<?> baseClass) {
		this(baseClass, false);
	}

	public HierarchyLookup(Class<?> baseClass, boolean singleton) {
		this.baseClass = baseClass;
		this.singleton = singleton;
	}

	public void register(Class<?> clazz, T provider) {
		Objects.requireNonNull(clazz);
		Objects.requireNonNull(provider.getUid());
		WailaCommonRegistration.instance().priorities.put(provider);
		objects.put(clazz, provider);
	}

	public List<T> get(Object obj) {
		if (obj == null) {
			return ImmutableList.of();
		}
		return get(obj.getClass());
	}

	public List<T> get(Class<?> clazz) {
		try {
			return resultCache.get(clazz, () -> {
				List<T> list = Lists.newArrayList();
				getInternal(clazz, list);
				list = ImmutableList.sortedCopyOf(Comparator.comparingInt(WailaCommonRegistration.instance().priorities::byValue), list);
				if (singleton && !list.isEmpty())
					return ImmutableList.of(list.get(0));
				return list;
			});
		} catch (ExecutionException e) {
			Jade.LOGGER.error("", e);
		}
		return ImmutableList.of();
	}

	private void getInternal(Class<?> clazz, List<T> list) {
		if (clazz == null) {
			return;
		}
		if (clazz != baseClass && clazz != Object.class) {
			getInternal(clazz.getSuperclass(), list);
		}
		list.addAll(objects.get(clazz));
	}

	public Multimap<Class<?>, T> getObjects() {
		return objects;
	}

	public void invalidate() {
		resultCache.invalidateAll();
	}

	public void loadComplete(PriorityStore<Identifier, IJadeProvider> priorityStore) {
		objects.asMap().forEach((clazz, list) -> {
			if (list.size() < 2) {
				return;
			}
			Set<Identifier> set = Sets.newHashSetWithExpectedSize(list.size());
			for (T provider : list) {
				if (set.contains(provider.getUid())) {
					throw new IllegalStateException("Duplicate UID: " + provider.getUid());
				}
				set.add(provider.getUid());
			}
		});

		objects = ImmutableListMultimap.<Class<?>, T>builder().orderValuesBy(Comparator.comparingInt(priorityStore::byValue)).putAll(objects).build();
	}

}
