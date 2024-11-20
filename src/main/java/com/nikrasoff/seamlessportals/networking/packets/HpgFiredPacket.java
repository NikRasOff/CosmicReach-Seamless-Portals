package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.extras.ExtraPortalUtils;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.NetworkSide;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class HpgFiredPacket extends GamePacket {
    boolean isSecondPortal;
    int hpgSlotNum;
    public HpgFiredPacket(){}
    public HpgFiredPacket(boolean isSecondPortal, int hpgSlotNum){
        this.isSecondPortal = isSecondPortal;
        this.hpgSlotNum = hpgSlotNum;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.isSecondPortal = this.readBoolean(byteBuf);
        this.hpgSlotNum = this.readInt(byteBuf);
    }

    @Override
    public void write() {
        this.writeBoolean(isSecondPortal);
        this.writeInt(hpgSlotNum);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if (networkIdentity.getSide() != NetworkSide.CLIENT){
            Player pl = networkIdentity.getPlayer();
            ItemStack hpgStack = pl.inventory.getSlot(this.hpgSlotNum).itemStack;
            if (hpgStack != null && hpgStack.getItem() != null && hpgStack.getItem().getID().equals("seamlessportals:handheld_portal_generator")){
                ExtraPortalUtils.fireHpg(pl, this.isSecondPortal, hpgStack);
            }
        }
    }
}
