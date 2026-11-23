package snownee.jade.api;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import java.util.function.Supplier;

/**
 * Class to get information of block target and context.
 */
public interface BlockAccessor extends Accessor<HitResult> {

	Block getBlock();

	BlockState getBlockState();

	BlockEntity getBlockEntity();

	BlockPos getPosition();

	Direction getSide();

	@Override
	default Class<? extends Accessor<?>> getAccessorType() {
		return BlockAccessor.class;
	}

	@NonExtendable
	interface Builder {
		Builder level(World level);

		Builder player(PlayerEntity player);

		Builder serverData(NbtCompound serverData);

		Builder serverConnected(boolean connected);

		Builder showDetails(boolean showDetails);

		Builder hit(HitResult hit);

		Builder blockState(BlockState state);

		default Builder blockEntity(BlockEntity blockEntity) {
			return blockEntity(() -> blockEntity);
		}

		Builder blockEntity(Supplier<BlockEntity> blockEntity);

		Builder from(BlockAccessor accessor);

		Builder requireVerification();

		BlockAccessor build();
	}

}
