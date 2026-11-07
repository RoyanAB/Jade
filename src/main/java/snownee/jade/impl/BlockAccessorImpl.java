package snownee.jade.impl;

import com.google.common.base.Suppliers;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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

	private static double distanceSq(BlockPos pos, ServerPlayerEntity player) {
		double dx = player.x - (pos.getX() + 0.5);
		double dy = player.y - (pos.getY() + 0.5);
		double dz = player.z - (pos.getZ() + 0.5);
		return dx * dx + dy * dy + dz * dz;
	}

	public static void handleRequest(SyncData data, ServerPlayerEntity player, Consumer<Runnable> executor, Consumer<NbtCompound> responseSender) {
		executor.accept(() -> {
			BlockPos pos = data.pos;
			World world = player.world;
			if (distanceSq(pos, player) > Jade.MAX_DISTANCE_SQR || !world.isChunkLoaded(pos))
				return;

			BlockEntity tile = world.getBlockEntity(pos);
			if (tile == null)
				return;

			List<IServerDataProvider<BlockAccessor>> providers = WailaCommonRegistration.instance().getBlockNBTProviders(tile);
			if (providers.isEmpty())
				return;

			BlockAccessor accessor = new Builder()
				.level(world)
				.player(player)
				.serverConnected(true)
				.showDetails(data.showDetails)
				.hit(data.hit)
				.blockState(world.getBlockState(pos))
				.blockEntity(tile)
				.fakeBlock(data.fakeBlock)
				.build();

			NbtCompound tag = new NbtCompound();
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

	@Override
	public Class<? extends snownee.jade.api.Accessor<?>> getAccessorType() {
		return BlockAccessor.class;
	}

	public static class Builder implements BlockAccessor.Builder {
		private World level;
		private PlayerEntity player;
		private NbtCompound serverData;
		private boolean connected;
		private boolean showDetails;
		private HitResult hit;
		private BlockState blockState;
		@Nullable
		private Supplier<BlockEntity> blockEntity;
		private ItemStack fakeBlock = ItemStack.EMPTY;

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
			level(accessor.getLevel()).player(accessor.getPlayer()).serverData(accessor.getServerData()).serverConnected(accessor.isServerConnected()).showDetails(accessor.showDetails());
			hit(accessor.getHitResult());
			blockState(accessor.getBlockState());
			blockEntity(accessor::getBlockEntity);
			fakeBlock = accessor.getFakeBlock();
			return this;
		}

		@Override
		public Builder requireVerification() {
			return this;
		}

		@Override
		public BlockAccessor build() {
			Objects.requireNonNull(level);
			Objects.requireNonNull(player);
			Objects.requireNonNull(hit);
			Objects.requireNonNull(blockState);
			return new BlockAccessorImpl(this);
		}
	}

	public static class SyncData {
		public final boolean showDetails;
		public final BlockPos pos;
		public final HitResult hit;
		public final ItemStack fakeBlock;

		public SyncData(BlockAccessor accessor) {
			showDetails = accessor.showDetails();
			pos = accessor.getPosition();
			hit = accessor.getHitResult();
			fakeBlock = accessor.getFakeBlock();
		}

		public SyncData(PacketBuffer buf) {
			showDetails = buf.readBoolean();
			pos = buf.readBlockPos();
			int face = buf.readByte();
			Vec3d hitVec = new Vec3d(buf.readFloat(), buf.readFloat(), buf.readFloat());
			hit = new HitResult(hitVec, face < 0 || face > 5 ? Direction.UP : Direction.byId(face), pos);
			fakeBlock = buf.readItem();
		}

		public void write(PacketBuffer buf) {
			buf.writeBoolean(showDetails);
			buf.writeBlockPos(hit.getPos());
			buf.writeByte(hit.face == null ? 0 : hit.face.getId());
			Vec3d hitVec = hit.facePos == null ? new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) : hit.facePos;
			buf.writeFloat((float) hitVec.x);
			buf.writeFloat((float) hitVec.y);
			buf.writeFloat((float) hitVec.z);
			buf.writeItem(fakeBlock);
		}
	}
}
