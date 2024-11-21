package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.ExtraPortalUtils;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.NetworkSide;
import finalforeach.cosmicreach.networking.packets.MessagePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.Predicate;

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
            else {
                networkIdentity.send(new MessagePacket("<Seamless portals>: Handheld portal generators gotten from the item catalog don't work"));
                SeamlessPortals.LOGGER.warn("Not HPG!");
                if (hpgStack == null){
                    SeamlessPortals.LOGGER.warn("Reason: stack is null");
                }
                else if (hpgStack.getItem() == null){
                    SeamlessPortals.LOGGER.warn("Reason: item is null");
                }
                else {
                    SeamlessPortals.LOGGER.warn("Reason: item is not HPG but " + hpgStack.getItem().getID());
                }
                SeamlessPortals.LOGGER.warn("Slot provided: " + this.hpgSlotNum);
                ItemSlot slot = pl.inventory.getFirstMatchingItemSlot(itemSlot -> {
                    if (itemSlot.isEmpty()) return false;
                    return itemSlot.itemStack.getItem().getID().equals("seamlessportals:handheld_portal_generator");
                });
                SeamlessPortals.LOGGER.warn("Actual slot: " + (slot == null ? "null" : slot.getSlotId()));
            }
        }
    }
}
