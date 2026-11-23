package snownee.jade.util;

import io.netty.buffer.Unpooled;
import net.minecraft.resource.Identifier;
import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.core.api.util.NamespacedIdentifiers;
import net.ornithemc.osl.networking.api.PacketBuffer;

public final class Networking {

	private Networking() {
	}

	public static NamespacedIdentifier channel(Identifier id) {
		return NamespacedIdentifiers.from(id.getNamespace(), id.getPath());
	}

	public static PacketBuffer buffer() {
		return new PacketBuffer(Unpooled.buffer());
	}
}
