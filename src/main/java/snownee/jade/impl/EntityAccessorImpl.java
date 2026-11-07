package snownee.jade.impl;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import net.ornithemc.osl.networking.api.PacketBuffer;
import snownee.jade.Jade;
import snownee.jade.api.AccessorImpl;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.util.CommonProxy;
import snownee.jade.util.WailaExceptionHandler;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class to get information of entity target and context.
 */
public class EntityAccessorImpl extends AccessorImpl<HitResult> implements EntityAccessor {

	private final Supplier<Entity> entity;

	public EntityAccessorImpl(Builder builder) {
		super(builder.level, builder.player, builder.serverData, builder.hit, builder.connected, builder.showDetails);
		entity = builder.entity;
	}

	public static void handleRequest(SyncData data, ServerPlayerEntity player, Consumer<Runnable> executor, Consumer<NbtCompound> responseSender) {
		executor.accept(() -> {
			Entity target = CommonProxy.getPartEntity(data.unpackEntity(player), data.partIndex);
			if (target == null)
				return;
			Entity entity = CommonProxy.wrapPartEntityParent(target);
			if (player.getSquaredDistanceTo(new net.minecraft.util.math.BlockPos(entity.x, entity.y, entity.z)) > Jade.MAX_DISTANCE_SQR)
				return;
			List<IServerDataProvider<EntityAccessor>> providers = WailaCommonRegistration.instance().getEntityNBTProviders(entity);
			if (providers.isEmpty())
				return;

			EntityAccessor accessor = new Builder()
				.level(player.world)
				.player(player)
				.serverConnected(true)
				.showDetails(data.showDetails)
				.entity(() -> target)
				.hit(() -> new HitResult(target, data.hitVec))
				.build();

			NbtCompound tag = new NbtCompound();
			for (IServerDataProvider<EntityAccessor> provider : providers) {
				try {
					provider.appendServerData(tag, accessor);
				} catch (Exception e) {
					WailaExceptionHandler.handleErr(e, provider, null);
				}
			}

			tag.putInt("WailaEntityID", entity.getNetworkId());
			responseSender.accept(tag);
		});
	}

	@Override
	@Deprecated
	public void toNetwork(PacketBuffer buf) {
		new SyncData(this).write(buf);
	}

	@Override
	public Entity getEntity() {
		return CommonProxy.wrapPartEntityParent(getRawEntity());
	}

	@Override
	public Entity getRawEntity() {
		return entity.get();
	}

	@Override
	public ItemStack getPickedResult() {
		return CommonProxy.getEntityPickedResult(entity.get(), getPlayer(), getHitResult());
	}

	@Override
	public Object getTarget() {
		return getEntity();
	}

	@Override
	public boolean verifyData(NbtCompound data) {
		if (!verify)
			return true;
		if (!data.contains("WailaEntityID"))
			return false;
		return data.getInt("WailaEntityID") == getEntity().getNetworkId();
	}

	@Override
	public Class<? extends snownee.jade.api.Accessor<?>> getAccessorType() {
		return EntityAccessor.class;
	}

	public static class Builder implements EntityAccessor.Builder {

		public boolean showDetails;
		private World level;
		private PlayerEntity player;
		private NbtCompound serverData;
		private boolean connected;
		private Supplier<HitResult> hit;
		private Supplier<Entity> entity;
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
		public Builder hit(Supplier<HitResult> hit) {
			this.hit = hit;
			return this;
		}

		@Override
		public Builder entity(Supplier<Entity> entity) {
			this.entity = entity;
			return this;
		}

		@Override
		public Builder from(EntityAccessor accessor) {
			level = accessor.getLevel();
			player = accessor.getPlayer();
			serverData = accessor.getServerData();
			connected = accessor.isServerConnected();
			showDetails = accessor.showDetails();
			hit = accessor::getHitResult;
			entity = accessor::getEntity;
			return this;
		}

		@Override
		public EntityAccessor.Builder requireVerification() {
			verify = true;
			return this;
		}

		@Override
		public EntityAccessor build() {
			EntityAccessorImpl accessor = new EntityAccessorImpl(this);
			if (verify) {
				accessor.requireVerification();
			}
			return accessor;
		}
	}

	public static class SyncData {
		public final boolean showDetails;
		public final int id;
		public final int partIndex;
		public final Vec3d hitVec;

		public SyncData(EntityAccessor accessor) {
			showDetails = accessor.showDetails();
			id = accessor.getEntity().getNetworkId();
			partIndex = CommonProxy.getPartEntityIndex(accessor.getRawEntity());
			hitVec = accessor.getHitResult().facePos;
		}

		public SyncData(PacketBuffer buffer) {
			showDetails = buffer.readBoolean();
			id = buffer.readVarInt();
			partIndex = buffer.readVarInt();
			hitVec = new Vec3d(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		}

		public void write(PacketBuffer buffer) {
			buffer.writeBoolean(showDetails);
			buffer.writeVarInt(id);
			buffer.writeVarInt(partIndex);
			buffer.writeFloat((float) hitVec.x);
			buffer.writeFloat((float) hitVec.y);
			buffer.writeFloat((float) hitVec.z);
		}

		public Entity unpackEntity(ServerPlayerEntity player) {
			return player.world.getEntity(id);
		}
	}
}
