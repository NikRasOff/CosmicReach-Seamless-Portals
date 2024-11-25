package com.nikrasoff.seamlessportals.mixin;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityOmniumCalibrator;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.items.containers.SlotContainer;
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

    @Inject(method = "<init>(Lfinalforeach/cosmicreach/blockentities/BlockEntity;Lfinalforeach/cosmicreach/items/containers/SlotContainer;)V", at = @At(value = "INVOKE", target = "Ljava/lang/RuntimeException;<init>(Ljava/lang/String;)V", unsafe = true))
    void debugStuff(BlockEntity entity, SlotContainer container, CallbackInfo ci){
        if (entity instanceof BlockEntityOmniumCalibrator omniumCalibrator){
            if (omniumCalibrator.slotContainer != container){
                SeamlessPortals.LOGGER.error("Well we're fucked");
            }
            else{
                SeamlessPortals.LOGGER.info("All good!");
            }
        }
    }

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
        }
    }
}
