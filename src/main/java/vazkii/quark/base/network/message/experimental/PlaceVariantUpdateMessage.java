package vazkii.quark.base.network.message.experimental;

import java.io.Serial;

import net.minecraftforge.network.NetworkEvent;
import vazkii.arl.network.IMessage;
import vazkii.quark.content.experimental.module.VariantSelectorModule;

public class PlaceVariantUpdateMessage implements IMessage {

	@Serial
	private static final long serialVersionUID = -6123685825175210844L;

	public String variant;
	
	public PlaceVariantUpdateMessage() { }

	public PlaceVariantUpdateMessage(String variant) {
		this.variant = variant;
	}

	@Override
	public boolean receive(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			VariantSelectorModule.setSavedVariant(context.getSender(), variant);
		});
		return true;
	}

}
