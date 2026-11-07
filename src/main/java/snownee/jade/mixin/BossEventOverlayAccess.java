package snownee.jade.mixin;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.overlay.BossEventOverlay;
import net.minecraft.client.world.ClientBossEvent;

@Mixin(BossEventOverlay.class)
public interface BossEventOverlayAccess {

	@Accessor("bossEvents")
	Map<UUID, ClientBossEvent> getBossEvents();

}
