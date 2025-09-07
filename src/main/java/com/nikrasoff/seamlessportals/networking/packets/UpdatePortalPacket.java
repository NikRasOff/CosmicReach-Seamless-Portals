package com.nikrasoff.seamlessportals.networking.packets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class UpdatePortalPacket extends GamePacket {
    EntityUniqueId portalId = new EntityUniqueId();
    EntityUniqueId linkedPortalId = new EntityUniqueId();
    Vector3 position = new Vector3();
    Vector3 lookDirection = new Vector3();
    Vector3 upVector = new Vector3();
    Vector2 size = new Vector2();
    Zone zone;
    public UpdatePortalPacket(){}
    public UpdatePortalPacket(Portal portal){
        this.portalId = portal.uniqueId;
        this.linkedPortalId = (portal.linkedPortal == null) ? new EntityUniqueId() : portal.linkedPortal.uniqueId;
        this.position = portal.getPosition();
        this.lookDirection = portal.viewDirection;
        this.upVector = portal.upVector;
        this.size.x = portal.portalSize.x;
        this.size.y = portal.portalSize.y;
        this.zone = portal.zone;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.readEntityUniqueId(byteBuf, this.portalId);
        this.readEntityUniqueId(byteBuf, this.linkedPortalId);
        this.readVector3(byteBuf, this.position);
        this.readVector3(byteBuf, this.lookDirection);
        this.readVector3(byteBuf, this.upVector);
        this.size.x = this.readFloat(byteBuf);
        this.size.y = this.readFloat(byteBuf);
        this.zone = GameSingletons.world.getZoneCreateIfNull(this.readString(byteBuf));
    }

    @Override
    public void write() {
        this.writeEntityUniqueId(this.portalId);
        this.writeEntityUniqueId(this.linkedPortalId);
        this.writeVector3(this.position);
        this.writeVector3(this.lookDirection);
        this.writeVector3(this.upVector);
        this.writeFloat(this.size.x);
        this.writeFloat(this.size.y);
        this.writeString(this.zone.zoneId);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if (GameSingletons.isClient){
            Portal portal = SeamlessPortals.portalManager.getPortal(this.portalId);
            if (portal == null) return;
            portal.pendingLinkedPortal = SeamlessPortals.portalManager.getPortal(this.linkedPortalId);
            portal.setPosition(this.position);
            portal.viewDirection = this.lookDirection;
            portal.upVector = this.upVector;
            portal.updateSize(this.size);
            if (this.zone != portal.zone){
                portal.zone.removeEntity(portal);
                this.zone.addEntity(portal);
            }
        }
    }
}
