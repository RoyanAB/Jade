package snownee.jade.addon.universal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.LootInventoryBlockEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.menu.LockableMenuProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.commons.lang3.mutable.MutableBoolean;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Direction2D;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.*;
import snownee.jade.impl.WailaClientRegistration;
import snownee.jade.impl.WailaCommonRegistration;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.impl.ui.HorizontalLineElement;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum ItemStorageProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>,
	IServerExtensionProvider<Object, ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {

	INSTANCE;

	public final Cache<Object, ItemCollector<?>> targetCache = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(60, TimeUnit.SECONDS).build();
	public final Cache<Object, ItemCollector<?>> containerCache = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(120, TimeUnit.SECONDS).build();

	public static void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
		if (!accessor.getServerData().contains("JadeItemStorage")) {
			if (accessor.getServerData().getBoolean("Loot")) {
				tooltip.add(new TranslatableText("jade.loot_not_generated"));
			} else if (accessor.getServerData().getBoolean("Locked")) {
				tooltip.add(new TranslatableText("jade.locked"));
			}
			return;
		}
		IClientExtensionProvider<ItemStack, ItemView> provider = null;
		try {
			provider = WailaClientRegistration.instance().itemStorageProviders.get(new Identifier(accessor.getServerData().getString("JadeItemStorageUid")));
		} catch (Exception ignored) {
		}
		if (provider == null) {
			return;
		}
		List<ClientViewGroup<ItemView>> groups = provider.getClientGroups(accessor, ViewGroup.readList(accessor.getServerData(), "JadeItemStorage", itemTag -> {
			ItemStack item = new ItemStack(itemTag);
			if (!item.isEmpty() && itemTag.contains("NewCount")) {
				item.setSize(itemTag.getInt("NewCount"));
			}
			return item;
		}));

		if (groups == null || groups.isEmpty()) {
			return;
		}

		MutableBoolean showName = new MutableBoolean(true);
		{
			int totalSize = 0;
			for (ClientViewGroup<ItemView> group : groups) {
				for (ItemView view : group.views) {
					if (view.amountText != null) {
						showName.setFalse();
					}
					if (!view.item.isEmpty()) {
						++totalSize;
					}
				}
			}
			if (showName.isTrue()) {
				showName.setValue(totalSize < PluginConfig.INSTANCE.getInt(Identifiers.MC_ITEM_STORAGE_SHOW_NAME_AMOUNT));
			}
		}

		IElementHelper helper = IElementHelper.get();
		boolean renderGroup = groups.size() > 1 || groups.get(0).shouldRenderGroup();
		ClientViewGroup.tooltip(tooltip, groups, renderGroup, (theTooltip, group) -> {
			if (renderGroup) {
				theTooltip.add(new HorizontalLineElement());
				if (group.title != null) {
					theTooltip.append(helper.text(group.title).scale(0.5F));
					theTooltip.append(new HorizontalLineElement());
				}
			}
			if (group.views.isEmpty()) {
				NbtCompound data = group.extraData;
				if (data != null && data.contains("Collecting")) {
					float progress = data.getFloat("Collecting");
					if (progress < 1) {
						Text component = new TranslatableText("jade.collectingItems");
						if (progress > 0) {
							component = component.append(String.format(" %s%%", (int) (progress * 100)));
						}
						theTooltip.add(component);
					}
				}
			}
			int drawnCount = 0;
			int realSize = PluginConfig.INSTANCE.getInt(accessor.showDetails() ? Identifiers.MC_ITEM_STORAGE_DETAILED_AMOUNT : Identifiers.MC_ITEM_STORAGE_NORMAL_AMOUNT);
			realSize = Math.min(group.views.size(), realSize);
			List<IElement> elements = Lists.newArrayList();
			for (int i = 0; i < realSize; i++) {
				ItemView itemView = group.views.get(i);
				ItemStack stack = itemView.item;
				if (stack.isEmpty()) {
					continue;
				}
				if (i > 0 && (showName.isTrue() || drawnCount >= PluginConfig.INSTANCE.getInt(Identifiers.MC_ITEM_STORAGE_ITEMS_PER_LINE))) {
					theTooltip.add(elements);
					theTooltip.setLineMargin(-1, Direction2D.DOWN, -1);
					elements.clear();
					drawnCount = 0;
				}

				if (showName.isTrue()) {
					if (itemView.description != null) {
						elements.add(helper.smallItem(stack));
						elements.addAll(itemView.description);
					} else {
						elements.add(helper.smallItem(stack).clearCachedMessage());
						elements.add(helper.text(new LiteralText(IDisplayHelper.get().humanReadableNumber(stack.getSize(), "", false, null))
							.append("× ")
							.append(IDisplayHelper.get().stripColor(new LiteralText(stack.getHoverName())))).message(null));
					}
				} else if (itemView.amountText != null) {
					elements.add(helper.item(stack, 1, itemView.amountText));
				} else {
					elements.add(helper.item(stack));
				}
				drawnCount += 1;
			}

			if (!elements.isEmpty()) {
				theTooltip.add(elements);
			}
		});
	}

	public static void putData(Accessor<?> accessor) {
		NbtCompound tag = accessor.getServerData();
		Object target = accessor.getTarget();
		PlayerEntity player = accessor.getPlayer();
		for (IServerExtensionProvider<Object, ItemStack> provider : WailaCommonRegistration.instance().itemStorageProviders.get(target)) {
			List<ViewGroup<ItemStack>> groups = provider.getGroups(accessor, target);
			if (groups == null) {
				continue;
			}
			if (ViewGroup.saveList(tag, "JadeItemStorage", groups, item -> {
				NbtCompound itemTag = new NbtCompound();
				int count = item.getSize();
				if (count > 64) {
					item.setSize(1);
				}
				item.writeNbt(itemTag);
				if (count > 64) {
					itemTag.putInt("NewCount", count);
					item.setSize(count);
				}
				return itemTag;
			})) {
				tag.putString("JadeItemStorageUid", provider.getUid().toString());
			} else if (target instanceof LootInventoryBlockEntity && ((LootInventoryBlockEntity) target).getLootTableId() != null) {
				tag.putBoolean("Loot", true);
			} else if (player != null && !player.abilities.creativeMode && !player.isSpectator() && target instanceof LockableMenuProvider && ((LockableMenuProvider) target).isLocked()) {
				tag.putBoolean("Locked", true);
			}
			break;
		}
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getBlockEntity() instanceof FurnaceBlockEntity) {
			return;
		}
		append(tooltip, accessor, config);
	}

	@Override
	public void appendServerData(NbtCompound tag, BlockAccessor accessor) {
		if (accessor.getBlockEntity() instanceof FurnaceBlockEntity) {
			return;
		}
		putData(accessor);
	}

	@Override
	public Identifier getUid() {
		return Identifiers.UNIVERSAL_ITEM_STORAGE;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.BODY + 1000;
	}

	@Override
	public List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor, Object target) {
		if (target instanceof LootInventoryBlockEntity && ((LootInventoryBlockEntity) target).getLootTableId() != null) {
			return java.util.Collections.emptyList();
		}
		PlayerEntity player = accessor.getPlayer();
		if (player != null && !player.abilities.creativeMode && !player.isSpectator() && target instanceof LockableMenuProvider && ((LockableMenuProvider) target).isLocked()) {
			return java.util.Collections.emptyList();
		}
		if (player != null && target instanceof EnderChestBlockEntity) {
			Inventory inventory = player.getEnderChestInventory();
			return new ItemCollector<>(new ItemIterator.ContainerItemIterator(0)).update(inventory, accessor.getLevel().getTime());
		}
		ItemCollector<?> itemCollector;
		try {
			itemCollector = targetCache.get(target, () -> CommonProxy.createItemCollector(target, containerCache));
		} catch (ExecutionException e) {
			WailaExceptionHandler.handleErr(e, null, null);
			return null;
		}
		if (itemCollector == ItemCollector.EMPTY) {
			return null;
		}
		return itemCollector.update(target, accessor.getLevel().getTime());
	}

	@Override
	public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
		if (groups == null) {
			return java.util.Collections.emptyList();
		}
		return ClientViewGroup.map(groups, ItemView::new, null);
	}

}
