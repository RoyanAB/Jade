package snownee.jade.api;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import net.ornithemc.osl.networking.api.PacketBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * A generic class to get basic information of target and context.
 */
public interface Accessor<T extends HitResult> {

	World getLevel();

	PlayerEntity getPlayer();

	@NotNull
	NbtCompound getServerData();

	T getHitResult();

	/**
	 * @return {@code true} if the dedicated server has Jade installed.
	 */
	boolean isServerConnected();

	ItemStack getPickedResult();

	boolean showDetails();

	Object getTarget();

	Class<? extends Accessor<?>> getAccessorType();

	@Deprecated
	void toNetwork(PacketBuffer buf);

	boolean verifyData(NbtCompound data);

}
