package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.blockentities.BlockEntitySpacialAnchor;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.blocks.BlockEntityContainerSyncPacket;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityContainerSyncPacket.class)
public abstract class BlockEntitySyncMixin {
    @Shadow public int x;

    @Shadow public int y;

    @Shadow public int z;

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Field;set(Ljava/lang/Object;Ljava/lang/Object;)V"))
    void BECheck1(NetworkIdentity identity, ChannelHandlerContext ctx, CallbackInfo ci){
        BlockEntity be = identity.getZone().getBlockEntity(x, y, z);
        cosmicReach_Seamless_Portals$BECheck(be);
    }

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/items/containers/SlotContainer;replaceSlotContents(Lfinalforeach/cosmicreach/items/containers/SlotContainer;)V"))
    void BECheck2(NetworkIdentity identity, ChannelHandlerContext ctx, CallbackInfo ci){
        BlockEntity be = identity.getZone().getBlockEntity(x, y, z);
        cosmicReach_Seamless_Portals$BECheck(be);
    }

    @Unique
    void cosmicReach_Seamless_Portals$BECheck(BlockEntity blockEntity){
        if (blockEntity instanceof BlockEntityOmniumCalibrator){
            blockEntity.setTicking(true);
        } else if (blockEntity instanceof BlockEntityPortalGenerator pg) {
            pg.slotContainer.checkItem();
        }
        else if (blockEntity instanceof BlockEntitySpacialAnchor anchor){
            anchor.slotContainer.checkInput();
        }
    }
}
