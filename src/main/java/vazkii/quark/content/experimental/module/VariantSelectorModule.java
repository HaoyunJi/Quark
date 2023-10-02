package vazkii.quark.content.experimental.module;

import java.util.Arrays;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.base.Quark;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.network.QuarkNetwork;
import vazkii.quark.base.network.message.experimental.PlaceVariantChangedMessage;
import vazkii.quark.content.experimental.config.BlockSuffixConfig;
import vazkii.quark.content.experimental.item.HammerItem;

@LoadModule(category = ModuleCategory.EXPERIMENTAL, hasSubscriptions = true, enabledByDefault = false,
		description = "Allows placing variant blocks automatically via a selector menu, can also disable all variant block recipes and items")
public class VariantSelectorModule extends QuarkModule {

	private static final String TAG_CURRENT_VARIANT = Quark.MOD_ID + ":CurrentSelectedVariant";
	
	private static String clientVariant = "";
	private static boolean staticEnabled;

	@Config(description = "Set this to true to automatically convert any dropped variant items into their originals. Do this ONLY if you intend to take control of every recipe via a data pack or equivalent, as this will introduce dupes otherwise.")
	public static boolean convertVariantItems = false;
	
	@Config(flag = "hammer", description = "Enable the hammer, allowing variants to be swapped between eachother, including the original block. Do this ONLY under the same circumstances as Convert Variant Items.")
	public static boolean enableHammer = false;

	@Config 
	public static BlockSuffixConfig variants = new BlockSuffixConfig(
			Arrays.asList("slab", "stairs", "wall", "fence", "vertical_slab"), 
			Arrays.asList("quark"));
	
	public static Item hammer;
	
	@Override
	public void configChanged() {
		staticEnabled = enabled;
	}
	
	@Override
	public void register() {
		hammer = new HammerItem(this).setCondition(() -> enableHammer);
	}
	
	public static String getSavedVariant(Player player) {
		if(player.level.isClientSide)
			return clientVariant;
		
		return player.getPersistentData().getString(TAG_CURRENT_VARIANT);
	}
	
	private static void setSavedVariant(Player player, String variant) {
		if(player instanceof ServerPlayer sp) {
			if(variant == null)
				variant = "";
			
			player.getPersistentData().putString(TAG_CURRENT_VARIANT, variant);
			player.sendSystemMessage(Component.literal("Changed variant to " + (variant.isEmpty() ? "block" : variant) + " (WIP)"));
			QuarkNetwork.sendToPlayer(new PlaceVariantChangedMessage(variant), sp);
		}
	}
	
	private static Block getMainHandVariantBlock(Player player, String variant) {
		ItemStack mainHand = player.getMainHandItem();
		if(mainHand.getItem() instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			return getVariantForBlock(block, variant);
		}
		
		return null;
	}
	
	public static Block getVariantForBlock(Block block, String variant) {
		Block variantBlock = variants.getBlockForVariant(block, variant);
		if(variantBlock != null)
			return variantBlock;
		
		return null;
	}
	
	public static Block getVariantOrOriginal(Block block, String variant) {
		block = variants.getOriginalBlock(block);
		
		if(variant == null || variant.isEmpty())
			return variants.getOriginalBlock(block);
		
		return getVariantForBlock(block, variant);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void setClientVariant(String variant) {
		clientVariant = variant;
	}
	
	@SubscribeEvent // TODO temp
	public void temp(RightClickBlock event) {
		Level level = event.getLevel();
		if(level.isClientSide || event.getHand() != InteractionHand.MAIN_HAND)
			return;
		
		Player player = event.getEntity();
		ItemStack stack = event.getItemStack();
		
		if(stack.isEmpty()) {
			Block block = level.getBlockState(event.getPos()).getBlock();
			setSavedVariant(player, variants.getVariantForBlock(block));
		}
	}
	
	@SubscribeEvent
	public void addEntityToWorld(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if(convertVariantItems && entity instanceof ItemEntity ie) {
			ItemStack stack = ie.getItem();
			if(stack.getItem() instanceof BlockItem bi) {
				Block block = bi.getBlock();
				Block otherBlock = variants.getOriginalBlock(block);
				
				if(otherBlock != block) {
					ItemStack clone = new ItemStack(otherBlock.asItem());
					clone.setTag(stack.getTag());
					ie.setItem(clone);
				}
			}
			
		}
	}
	
	public static BlockState modifyBlockPlacementState(BlockState state, BlockPlaceContext ctx) {
		if(!staticEnabled || state == null)
			return state;
		
		Player player = ctx.getPlayer();
		if(player != null) {
			String variant = getSavedVariant(player);
			if(variant != null && !variant.isEmpty()) {
				Block target = getVariantForBlock(state.getBlock(), variant);
				return target.getStateForPlacement(ctx);
			}
		}
		
		return state;
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onRender(RenderGuiOverlayEvent.Pre event) {
		if(event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type())
			return;
		
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		String savedVariant = getSavedVariant(player);
		
		if(savedVariant != null) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack displayLeft = mainHand.copy();
			
			Block variantBlock = null;
			
			if(displayLeft.is(hammer)) {
				HitResult result = mc.hitResult;
				if(result instanceof BlockHitResult bhr) {
					BlockPos pos = bhr.getBlockPos();
					Block testBlock = player.level.getBlockState(pos).getBlock();
					
					displayLeft = new ItemStack(testBlock);
					variantBlock = getVariantOrOriginal(testBlock, savedVariant);
				}
			}
			else
				variantBlock = getMainHandVariantBlock(player, savedVariant);
			
			if(variantBlock != null) {
				ItemStack displayRight = new ItemStack(variantBlock);
				
				if(displayLeft.getItem() == displayRight.getItem())
					return;

				Window window = event.getWindow();
				int x = window.getGuiScaledWidth() / 2;
				int y = window.getGuiScaledHeight() / 2 + 12;
				int pad = 8;

				displayLeft.setCount(1);
				
				mc.font.draw(event.getPoseStack(), "->", x - 5, y + 5, 0xFFFFFF);
				mc.getItemRenderer().renderAndDecorateItem(displayLeft, (int) x - 16 - pad, (int) y);
				mc.getItemRenderer().renderAndDecorateItem(displayRight, (int) x + pad, (int) y);
			}
		}
	}
	
}