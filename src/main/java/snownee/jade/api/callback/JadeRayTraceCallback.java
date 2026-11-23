package snownee.jade.api.callback;

import net.minecraft.world.HitResult;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;

@FunctionalInterface
public interface JadeRayTraceCallback {

	@Nullable
	Accessor<?> onRayTrace(HitResult hitResult, @Nullable Accessor<?> accessor, @Nullable Accessor<?> originalAccessor);

}
