package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.extras.ExtraPortalUtils;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.NetworkSide;
import finalforeach.cosmicreach.networking.packets.MessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PortalClearPacket extends GamePacket {
    int hpgSlotNum;
    public PortalClearPacket(){}
    public PortalClearPacket(int hpgSlotNum){
        this.hpgSlotNum = hpgSlotNum;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.hpgSlotNum = this.readInt(byteBuf);
    }

    @Override
    public void write() {
        this.writeInt(this.hpgSlotNum);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if (networkIdentity.getSide() != NetworkSide.CLIENT){
            Player pl = networkIdentity.getPlayer();
            ItemStack hpgStack = pl.inventory.getSlot(this.hpgSlotNum).itemStack;
            if (hpgStack != null && hpgStack.getItem() != null && (hpgStack.getItem().getID().equals(HandheldPortalGen.hpgID) || hpgStack.getItem().getID().equals(UnstableHandheldPortalGen.hpgID))){
                ExtraPortalUtils.clearPortals(networkIdentity.getPlayer(), hpgStack);
            }
            else {
                networkIdentity.send(new MessagePacket("[Seamless portals] Interact with the Handheld portal generator in your inventory to make it work."));
            }
        }
    }
}
