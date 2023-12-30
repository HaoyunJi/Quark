package vazkii.quark.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import vazkii.quark.content.experimental.module.VariantSelectorModule;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

	@ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
	private ItemStack renderArmWithItem(ItemStack stack, AbstractClientPlayer player) {
		return VariantSelectorModule.modifyHeldItemStack(player, stack);
	}


}
