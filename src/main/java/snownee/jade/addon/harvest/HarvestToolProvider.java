package snownee.jade.addon.harvest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.state.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElement.Align;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.ClientProxy;
import snownee.jade.util.CommonProxy;

public class HarvestToolProvider implements IBlockComponentProvider {

	public static final HarvestToolProvider INSTANCE = new HarvestToolProvider();

	public static final Cache<BlockState, ImmutableList<ItemStack>> resultCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
	public static final Map<Identifier, ToolHandler> TOOL_HANDLERS = Maps.newLinkedHashMap();
	private static final Vec2f ITEM_SIZE = new Vec2f(10, 0);

	static {
		if (CommonProxy.isPhysicallyClient()) {
			registerHandler(SimpleToolHandler.create(Identifiers.JADE("pickaxe"), true, java.util.Arrays.asList(
					Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE)));
			registerHandler(SimpleToolHandler.create(Identifiers.JADE("axe"), true, java.util.Arrays.asList(
					Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.DIAMOND_AXE)));
			registerHandler(SimpleToolHandler.create(Identifiers.JADE("shovel"), true, java.util.Arrays.asList(
					Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL)));
			registerHandler(SimpleToolHandler.create(Identifiers.JADE("hoe"), true, java.util.Arrays.asList(
					Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.DIAMOND_HOE)));
			registerHandler(SimpleToolHandler.create(Identifiers.JADE("sword"), false, java.util.Collections.singletonList(Items.WOODEN_SWORD))
					.addBlock(net.minecraft.block.Blocks.WEB));
			registerHandler(new ShearsToolHandler());
		}
	}

	public static ImmutableList<ItemStack> getTool(BlockState state, World world, BlockPos pos) {
		ImmutableList.Builder<ItemStack> tools = ImmutableList.builder();
		for (ToolHandler handler : TOOL_HANDLERS.values()) {
			ItemStack tool = handler.test(state, world, pos);
			if (!tool.isEmpty()) {
				tools.add(tool);
			}
		}
		return tools.build();
	}

	public static synchronized void registerHandler(ToolHandler handler) {
		TOOL_HANDLERS.put(handler.getUid(), handler);
	}

	public static void clearCache() {
		resultCache.invalidateAll();
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		net.minecraft.entity.living.player.PlayerEntity player = accessor.getPlayer();
		if (!config.get(Identifiers.MC_HARVEST_TOOL_CREATIVE) && (player.isCreative() || player.isSpectator())) {
			return;
		}
		GameMode gameType = ClientProxy.getGameMode();
		if (gameType == GameMode.ADVENTURE) {
			return;
		}
		BlockState state = accessor.getBlockState();
		float destroySpeed = state.getMiningSpeed(accessor.getLevel(), accessor.getPosition());
		float destroyProgress = state.getMiningSpeed(player, accessor.getLevel(), accessor.getPosition());
		if (destroySpeed < 0 || destroyProgress <= 0) {
			if (config.get(Identifiers.MC_SHOW_UNBREAKABLE)) {
				Text text = IThemeHelper.get().failure(net.minecraft.client.resource.language.I18n.translate("jade.harvest_tool.unbreakable"));
				tooltip.add(IElementHelper.get().text(text).message(null));
			}
			return;
		}

		boolean newLine = config.get(Identifiers.MC_HARVEST_TOOL_NEW_LINE);
		List<IElement> elements = getText(accessor, config);
		if (elements.isEmpty()) {
			return;
		}
		for (IElement element : elements) {
			element.message(null);
		}
		if (newLine) {
			tooltip.add(elements);
		} else {
			for (IElement element : elements) {
				element.align(Align.RIGHT);
			}
			tooltip.append(0, elements);
		}
	}

	public List<IElement> getText(BlockAccessor accessor, IPluginConfig config) {
		BlockState state = accessor.getBlockState();
		boolean requiresTool = !state.getMaterial().isToolNotRequired();
		if (!requiresTool && !config.get(Identifiers.MC_EFFECTIVE_TOOL)) {
			return java.util.Collections.emptyList();
		}
		List<ItemStack> tools;
		try {
			tools = resultCache.get(state, () -> getTool(state, accessor.getLevel(), accessor.getPosition()));
		} catch (ExecutionException e) {
			return java.util.Collections.emptyList();
		}
		if (tools.isEmpty()) {
			return java.util.Collections.emptyList();
		}

		int offsetY = 0;
		if (!config.get(Identifiers.MC_HARVEST_TOOL_NEW_LINE)) {
			offsetY = -3;
		}
		List<IElement> elements = Lists.newArrayList();
		for (ItemStack tool : tools) {
			elements.add(IElementHelper.get().item(tool, 0.75F).translate(new Vec2f(-1, offsetY)).size(ITEM_SIZE).message(null));
		}

		if (!elements.isEmpty()) {
			elements.add(0, IElementHelper.get().spacer(5, 0));
			ItemStack held = accessor.getPlayer().getItemInMainHand();
			boolean canHarvest = held.canMineBlock(state) && held.getMiningSpeed(state) > 1;
			if (CommonProxy.isShearable(state) && CommonProxy.isShears(held)) {
				canHarvest = true;
			}
			if (requiresTool || canHarvest) {
				IThemeHelper t = IThemeHelper.get();
				Text text = canHarvest ? t.success("✔") : t.danger("✕");
				elements.add(IElementHelper.get().text(text)
						.scale(0.75F)
						.zOffset(800)
						.size(Vec2f.ZERO)
						.translate(new Vec2f(-3, 6.25F + offsetY)));
			}
		}

		return elements;
	}

	@Override
	public Identifier getUid() {
		return Identifiers.MC_HARVEST_TOOL;
	}

	@Override
	public int getDefaultPriority() {
		return TooltipPosition.HEAD + 2000;
	}
}
