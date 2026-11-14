package snownee.jade.api.view;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ViewGroup<T> {

	public final List<T> views;
	@Nullable
	public String id;
	@Nullable
	protected NbtCompound extraData;

	public ViewGroup(List<T> views) {
		this.views = views;
	}

	public void save(NbtCompound tag, Function<T, NbtCompound> writer) {
		NbtList list = new NbtList();
		for (T view : views) {
			list.addElement(writer.apply(view));
		}
		tag.put("Views", list);
		if (id != null) {
			tag.putString("Id", id);
		}
		if (extraData != null) {
			tag.put("Data", extraData);
		}
	}

	public static <T> ViewGroup<T> read(NbtCompound tag, Function<NbtCompound, T> reader) {
		NbtList list = tag.getList("Views", 10);
		List<T> views = Lists.newArrayList();
		for (int i = 0; i < list.size(); i++) {
			views.add(reader.apply(list.getCompound(i)));
		}
		ViewGroup<T> group = new ViewGroup<>(views);
		if (tag.contains("Id")) {
			group.id = tag.getString("Id");
		}
		if (tag.contains("Data")) {
			group.extraData = tag.getCompound("Data");
		}
		return group;
	}

	public static <T> boolean saveList(NbtCompound tag, String key, List<ViewGroup<T>> groups, Function<T, NbtCompound> writer) {
		if (groups == null || groups.isEmpty()) {
			return false;
		}
		NbtList groupList = new NbtList();
		for (ViewGroup<T> group : groups) {
			if (group.views.isEmpty()) {
				continue;
			}
			NbtCompound groupTag = new NbtCompound();
			group.save(groupTag, writer);
			groupList.addElement(groupTag);
		}
		if (!groupList.isEmpty()) {
			tag.put(key, groupList);
			return true;
		}
		return false;
	}

	@Nullable
	public static <T> List<ViewGroup<T>> readList(NbtCompound tag, String key, Function<NbtCompound, T> reader) {
		NbtList list = tag.getList(key, 10);
		if (list.isEmpty()) {
			return null;
		}
		List<ViewGroup<T>> groups = Lists.newArrayList();
		for (int i = 0; i < list.size(); i++) {
			ViewGroup<T> group = read(list.getCompound(i), reader);
			if (!group.views.isEmpty()) {
				groups.add(group);
			}
		}
		return groups;
	}

	public NbtCompound getExtraData() {
		if (extraData == null) {
			extraData = new NbtCompound();
		}
		return extraData;
	}

	public void setProgress(float progress) {
		getExtraData().putFloat("Progress", progress);
	}

}
