package vazkii.quark.base.network.message.structural;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.IntSupplier;

// Not an IMessage to avoid serializing things like loginIndex
public abstract class HandshakeMessage implements IntSupplier {
	private int loginIndex;

	public void setLoginIndex(final int loginIndex) {
		this.loginIndex = loginIndex;
	}

	public int getLoginIndex() {
		return loginIndex;
	}

	@Override
	public int getAsInt() {
		return loginIndex;
	}

	public abstract void encode(FriendlyByteBuf buf);

	public abstract boolean consume(NetworkEvent.Context context, BiConsumer<HandshakeMessage, NetworkEvent.Context> reply);
}
