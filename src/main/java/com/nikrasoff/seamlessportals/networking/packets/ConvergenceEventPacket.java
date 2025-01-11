package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ConvergenceEventPacket extends GamePacket {
    // The funny >:)
    EntityUniqueId portalID = new EntityUniqueId();
    int textureNum = 0;
    public ConvergenceEventPacket(){}
    public ConvergenceEventPacket(HPGPortal portal){
        this.portalID = portal.uniqueId;
        this.textureNum = portal.convEventTexture;
    }
    public ConvergenceEventPacket(HPGPortal portal, int texNum){
        this.portalID = portal.uniqueId;
        this.textureNum = texNum;
    }

    @Override
    public void receive(ByteBuf byteBuf) {
        this.readEntityUniqueId(byteBuf, portalID);
        this.textureNum = this.readInt(byteBuf);
    }

    @Override
    public void write() {
        this.writeEntityUniqueId(this.portalID);
        this.writeInt(this.textureNum);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if (networkIdentity.isClient()){
            Portal p = SeamlessPortals.portalManager.getPortal(this.portalID);
            if (p instanceof HPGPortal hpgPortal && p.linkedPortal == null){
                if (this.textureNum == -1){
                    hpgPortal.convEventHappening = false;
                }
                else{
                    hpgPortal.convEventHappening = true;
                    hpgPortal.convEventTexture = this.textureNum;
                }
            }
        }
    }
}
