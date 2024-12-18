package com.nikrasoff.seamlessportals.networking.packets;

import com.nikrasoff.seamlessportals.blockentities.BlockEntityPortalGenerator;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blockentities.BlockEntity;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class DeactivatePortalGenPacket extends SPGamePacket {
    Zone zone;
    IntVector3 genPosition = new IntVector3();
    public DeactivatePortalGenPacket(){}
    public DeactivatePortalGenPacket(BlockEntityPortalGenerator portalGenerator){
        this.zone = portalGenerator.zone;
        this.genPosition.x = portalGenerator.getGlobalX();
        this.genPosition.y = portalGenerator.getGlobalY();
        this.genPosition.z = portalGenerator.getGlobalZ();
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.zone = GameSingletons.world.getZoneCreateIfNull(this.readString(byteBuf));
        this.readIntVector3(byteBuf, genPosition);
    }

    @Override
    public void write() {
        this.writeString(zone.zoneId);
        this.writeIntVector3(genPosition);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        BlockEntity be = this.zone.getBlockEntity(this.genPosition.x, this.genPosition.y, this.genPosition.z);
        if (be == null) return;
        if (be instanceof BlockEntityPortalGenerator pg){
            pg.closePortal();
            ServerSingletons.SERVER.broadcast(pg.zone, new PortalGeneratorUpdatePacket(pg));
        }
    }
}
