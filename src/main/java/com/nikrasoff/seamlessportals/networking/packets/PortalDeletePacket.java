package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PortalDeletePacket extends GamePacket {
    int portalId;
    public PortalDeletePacket(){}
    public PortalDeletePacket(int portalId){
        this.portalId = portalId;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.portalId = this.readInt(byteBuf);
    }

    @Override
    public void write() {
        this.writeInt(portalId);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if (networkIdentity.isClient()){
            Portal portal = SeamlessPortals.portalManager.getPortal(this.portalId);
            if (portal == null) return;
            SeamlessPortals.portalManager.removePortal(portal);
        }
    }
}
