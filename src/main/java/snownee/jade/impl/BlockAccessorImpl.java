package snownee.jade.impl;

import com.google.common.base.Suppliers;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Identifier;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import net.ornithemc.osl.networking.api.PacketBuffer;
import org.jetbrains.annotations.Nullable;
import snownee.jade.Jade;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class to get information of block target and context.
 */
public class BlockAccessorImpl extends AccessorImpl<HitResult> implements BlockAccessor {

	private final BlockState blockState;
	@Nullable
	private final Supplier<BlockEntity> blockEntity;
	private ItemStack fakeBlock;

	private BlockAccessorImpl(Builder builder) {
		super(builder.level, builder.player, builder.serverData, Suppliers.ofInstance(builder.hit), builder.connected, builder.showDetails);
		blockState = builder.blockState;
		blockEntity = builder.blockEntity;
		fakeBlock = builder.fakeBlock;
	}

	public static void handleRequest(SyncData data, ServerPlayerEntity player, Consumer<Runnable> executor, Consumer<NbtCompound> responseSender) {
		executor.accept(() -> {
			BlockAccessor accessor = data.unpack(player);
			if (accessor == null) {
				return;
			}
			BlockPos pos = accessor.getPosition();
			World world = player.world;
			//port: 1.20 uses pos.distSqr(player.blockPosition()), 1.12 only offers this helper
			if (player.getSquaredDistanceToCenter(pos) > Jade.MAX_DISTANCE_SQR || !world.isChunkLoaded(pos)) {
				return;
			}

			BlockEntity tile = accessor.getBlockEntity();
			if (tile == null) {
				return;
			}

			List<IServerDataProvider<BlockAccessor>> providers = WailaCommonRegistration.instance().getBlockNBTProviders(tile);
			if (providers.isEmpty()) {
				return;
			}

			// the accessor owns the tag, providers write into it directly (same as the original)
			NbtCompound tag = accessor.getServerData();
			for (IServerDataProvider<BlockAccessor> provider : providers) {
				try {
					provider.appendServerData(tag, accessor);
				} catch (Exception e) {
					WailaExceptionHandler.handleErr(e, provider, null);
				}
			}

			tag.putInt("x", pos.getX());
			tag.putInt("y", pos.getY());
			tag.putInt("z", pos.getZ());
			Identifier id = CommonProxy.getId(tile.getBlock());
			tag.putString("id", id == null ? "unknown" : id.toString());
			responseSender.accept(tag);
		});
	}

	@Override
	@Deprecated
	public void toNetwork(PacketBuffer buf) {
		new SyncData(this).write(buf);
	}

	@Override
	public Block getBlock() {
		return getBlockState().getBlock();
	}

	@Override
	public BlockState getBlockState() {
		return blockState;
	}

	@Override
	public BlockEntity getBlockEntity() {
		return blockEntity == null ? null : blockEntity.get();
	}

	@Override
	public BlockPos getPosition() {
		return getHitResult().getPos();
	}

	@Override
	public Direction getSide() {
		return getHitResult().face;
	}

	@Override
	public ItemStack getPickedResult() {
		return CommonProxy.getBlockPickedResult(blockState, getPlayer(), getHitResult());
	}

	@Override
	public Object getTarget() {
		return getBlockEntity();
	}

	@Override
	public boolean isFakeBlock() {
		return !fakeBlock.isEmpty();
	}

	@Override
	public ItemStack getFakeBlock() {
		return fakeBlock;
	}

	public void setFakeBlock(ItemStack fakeBlock) {
		this.fakeBlock = fakeBlock;
	}

	@Override
	public boolean verifyData(NbtCompound data) {
		if (!verify) {
			return true;
		}
		if (getBlockEntity() == null || !data.contains("x")) {
			return false;
		}
		return data.getInt("x") == getPosition().getX() && data.getInt("y") == getPosition().getY() && data.getInt("z") == getPosition().getZ();
	}

	public static class Builder implements BlockAccessor.Builder {
		private World level;
		private PlayerEntity player;
		private NbtCompound serverData;
		private boolean connected;
		private boolean showDetails;
		private HitResult hit;
		private BlockState blockState = Blocks.AIR.defaultState();
		@Nullable
		private Supplier<BlockEntity> blockEntity;
		private ItemStack fakeBlock = ItemStack.EMPTY;
		private boolean verify;

		@Override
		public Builder level(World level) {
			this.level = level;
			return this;
		}

		@Override
		public Builder player(PlayerEntity player) {
			this.player = player;
			return this;
		}

		@Override
		public Builder serverData(NbtCompound serverData) {
			this.serverData = serverData;
			return this;
		}

		@Override
		public Builder serverConnected(boolean connected) {
			this.connected = connected;
			return this;
		}

		@Override
		public Builder showDetails(boolean showDetails) {
			this.showDetails = showDetails;
			return this;
		}

		@Override
		public Builder hit(HitResult hit) {
			this.hit = hit;
			return this;
		}

		@Override
		public Builder blockState(BlockState state) {
			this.blockState = state;
			return this;
		}

		@Override
		public Builder blockEntity(Supplier<BlockEntity> blockEntity) {
			this.blockEntity = blockEntity;
			return this;
		}

		@Override
		public Builder fakeBlock(ItemStack stack) {
			this.fakeBlock = stack;
			return this;
		}

		@Override
		public Builder from(BlockAccessor accessor) {
			level = accessor.getLevel();
			player = accessor.getPlayer();
			serverData = accessor.getServerData();
			connected = accessor.isServerConnected();
			showDetails = accessor.showDetails();
			hit = accessor.getHitResult();
			blockState = accessor.getBlockState();
			blockEntity = accessor::getBlockEntity;
			fakeBlock = accessor.getFakeBlock();
			return this;
		}

		@Override
		public Builder requireVerification() {
			verify = true;
			return this;
		}

		@Override
		public BlockAccessor build() {
			Objects.requireNonNull(level);
			Objects.requireNonNull(player);
			Objects.requireNonNull(hit);
			Objects.requireNonNull(blockState);
			BlockAccessorImpl accessor = new BlockAccessorImpl(this);
			if (verify) {
				accessor.requireVerification();
			}
			return accessor;
		}
	}

	public static class SyncData {
		public final boolean showDetails;
		public final HitResult hit;
		public final BlockState blockState;
		public final ItemStack fakeBlock;

		public SyncData(BlockAccessor accessor) {
			showDetails = accessor.showDetails();
			hit = accessor.getHitResult();
			blockState = accessor.getBlockState();
			fakeBlock = accessor.getFakeBlock();
		}

		public SyncData(PacketBuffer buffer) {
			showDetails = buffer.readBoolean();
			BlockPos pos = buffer.readBlockPos();
			int face = buffer.readByte();
			Vec3d hitVec = new Vec3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
			hit = new HitResult(hitVec, face < 0 || face > 5 ? Direction.UP : Direction.byId(face), pos);
			//port: 1.12 has no Block.stateById, Block.serialize packs block id + metadata
			int stateId = buffer.readVarInt();
			Block block = stateId == 0 ? null : Block.byId(stateId & 0xFFF);
			blockState = block == null ? Blocks.AIR.defaultState() : block.getStateFromMetadata(stateId >> 12);
			fakeBlock = buffer.readItem();
		}

		public void write(PacketBuffer buffer) {
			buffer.writeBoolean(showDetails);
			buffer.writeBlockPos(hit.getPos());
			buffer.writeByte(hit.face == null ? 0 : hit.face.getId());
			Vec3d hitVec = hit.facePos == null ? new Vec3d(hit.getPos().getX() + 0.5, hit.getPos().getY() + 0.5, hit.getPos().getZ() + 0.5) : hit.facePos;
			buffer.writeFloat((float) hitVec.x);
			buffer.writeFloat((float) hitVec.y);
			buffer.writeFloat((float) hitVec.z);
			buffer.writeVarInt(Block.serialize(blockState));
			buffer.writeItem(fakeBlock);
		}

		@Nullable
		public BlockAccessor unpack(ServerPlayerEntity player) {
			Supplier<BlockEntity> blockEntity = null;
			if (blockState.getBlock().hasBlockEntity()) {
				BlockPos pos = hit.getPos();
				blockEntity = Suppliers.memoize(() -> player.world.getBlockEntity(pos));
			}
			return new Builder()
				.level(player.world)
				.player(player)
				.showDetails(showDetails)
				.hit(hit)
				.blockState(blockState)
				.blockEntity(blockEntity)
				.fakeBlock(fakeBlock)
				.build();
		}
	}
}
