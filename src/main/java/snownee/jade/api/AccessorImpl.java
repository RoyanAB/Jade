package snownee.jade.api;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * A generic class to get basic information of target and context.
 */
public abstract class AccessorImpl<T extends HitResult> implements Accessor<T> {

	private final World level;
	private final PlayerEntity player;
	private final NbtCompound serverData;
	private final Supplier<T> hit;
	private final boolean serverConnected;
	private final boolean showDetails;
	protected boolean verify;

	public AccessorImpl(World level, PlayerEntity player, NbtCompound serverData, Supplier<T> hit, boolean serverConnected, boolean showDetails) {
		this.level = level;
		this.player = player;
		this.hit = hit;
		this.serverConnected = serverConnected;
		this.showDetails = showDetails;
		this.serverData = serverData == null ? new NbtCompound() : serverData.copy();
	}

	@Override
	public World getLevel() {
		return level;
	}

	@Override
	public PlayerEntity getPlayer() {
		return player;
	}

	@Override
	public final @NotNull NbtCompound getServerData() {
		return serverData;
	}

	@Override
	public T getHitResult() {
		return hit.get();
	}

	/**
	 * Returns true if dedicated server has Jade installed.
	 */
	@Override
	public boolean isServerConnected() {
		return serverConnected;
	}

	@Override
	public boolean showDetails() {
		return showDetails;
	}

	@Override
	public abstract ItemStack getPickedResult();

	public void requireVerification() {
		verify = true;
	}
}
