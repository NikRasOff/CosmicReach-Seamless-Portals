package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PortalAnimationPacket extends GamePacket {
    int portalId;
    String animName;

    public PortalAnimationPacket(){}
    public PortalAnimationPacket(int portalId, String animName){
        this.portalId = portalId;
        this.animName = animName;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.portalId = this.readInt(byteBuf);
        this.animName = this.readString(byteBuf);
    }

    @Override
    public void write() {
        this.writeInt(portalId);
        this.writeString(animName);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if (networkIdentity.isClient()){
            Portal p = SeamlessPortals.portalManager.getPortal(this.portalId);
            if (p != null){
                p.playAnimation(this.animName);
            }
        }
    }
}
