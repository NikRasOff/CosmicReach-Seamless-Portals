package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PortalDeletePacket extends GamePacket {
    EntityUniqueId portalId = new EntityUniqueId();
    public PortalDeletePacket(){}
    public PortalDeletePacket(EntityUniqueId portalId){
        this.portalId = portalId;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.readEntityUniqueId(byteBuf, portalId);
    }

    @Override
    public void write() {
        this.writeEntityUniqueId(portalId);
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
