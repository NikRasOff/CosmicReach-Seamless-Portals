package com.nikrasoff.seamlessportals.networking.packets;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PortalGeneratorUpdatePacket extends SPGamePacket {
    Vector2 portalSize = new Vector2();
    Vector2 primaryPortalOffset = new Vector2();
    Vector2 secondaryPortalOffset = new Vector2();
    IntVector3 position = new IntVector3();
    EntityUniqueId portalId = new EntityUniqueId();
    public PortalGeneratorUpdatePacket(){}
    public PortalGeneratorUpdatePacket(BlockEntityPortalGenerator portalGenerator){
        this.portalSize = portalGenerator.portalSize;
        this.primaryPortalOffset = portalGenerator.entrancePortalOffset;
        this.secondaryPortalOffset = portalGenerator.exitPortalOffset;
        this.position.set(portalGenerator.getGlobalX(), portalGenerator.getGlobalY(), portalGenerator.getGlobalZ());
        this.portalId = portalGenerator.portalId;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.readVector2(byteBuf, portalSize);
        this.readVector2(byteBuf, primaryPortalOffset);
        this.readVector2(byteBuf, secondaryPortalOffset);
        this.readIntVector3(byteBuf, position);
        this.readEntityUniqueId(byteBuf, this.portalId);
    }

    @Override
    public void write() {
        this.writeVector2(portalSize);
        this.writeVector2(primaryPortalOffset);
        this.writeVector2(secondaryPortalOffset);
        this.writeIntVector3(this.position);
        this.writeEntityUniqueId(this.portalId);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        Zone zone = networkIdentity.getZone();
        BlockEntity be = zone.getBlockEntity(position.x, position.y, position.z);
        if (be instanceof BlockEntityPortalGenerator portalGenerator){
            portalGenerator.portalSize.x = MathUtils.clamp(this.portalSize.x, 1, 5);
            portalGenerator.portalSize.y = MathUtils.clamp(this.portalSize.y, 1, 5);
            portalGenerator.entrancePortalOffset.x = MathUtils.clamp(this.primaryPortalOffset.x, -portalGenerator.getMaxOffsetX(), portalGenerator.getMaxOffsetX());
            portalGenerator.entrancePortalOffset.y = MathUtils.clamp(this.primaryPortalOffset.y, -portalGenerator.getMaxOffsetY(), portalGenerator.getMaxOffsetY());
            portalGenerator.exitPortalOffset.x = MathUtils.clamp(this.secondaryPortalOffset.x, -portalGenerator.getMaxOffsetX(), portalGenerator.getMaxOffsetX());
            portalGenerator.exitPortalOffset.y = MathUtils.clamp(this.secondaryPortalOffset.y, -portalGenerator.getMaxOffsetY(), portalGenerator.getMaxOffsetY());
            portalGenerator.portalId = this.portalId;
            if (GameSingletons.isClient) portalGenerator.justUpdated = true;
            if (!GameSingletons.isClient && ServerSingletons.SERVER != null){
                ServerSingletons.SERVER.broadcastToZoneExcept(zone, this, networkIdentity);
            }
        }
    }
}
