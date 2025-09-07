package com.nikrasoff.seamlessportals.networking.packets;

import com.badlogic.gdx.math.Vector3;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class CreateEffectPacket extends GamePacket {
    CRBinDeserializer deserializer = new CRBinDeserializer();
    Identifier effectId;
    float startingTime;
    Vector3 position = new Vector3();
    Zone zone;
    CRBinSerializer serializer;

    public CreateEffectPacket(){}
    public CreateEffectPacket(Identifier effectId, float startingTime, Vector3 position, Zone zone, CRBinSerializer serializer){
        this.effectId = effectId;
        this.startingTime = startingTime;
        this.position = position;
        this.zone = zone;
        this.serializer = serializer;
    }
    @Override
    public void receive(ByteBuf byteBuf) {
        this.effectId = Identifier.of(this.readString(byteBuf));
        this.startingTime = this.readFloat(byteBuf);
        this.readVector3(byteBuf, position);
        this.zone = GameSingletons.world.getZoneCreateIfNull(this.readString(byteBuf));
        this.deserializer.prepareForRead(byteBuf.nioBuffer());
    }

    @Override
    public void write() {
        this.writeString(effectId.toString());
        this.writeFloat(startingTime);
        this.writeVector3(position);
        this.writeString(zone.zoneId);
        this.writeCRBin(serializer);
    }

    @Override
    public void handle(NetworkIdentity networkIdentity, ChannelHandlerContext channelHandlerContext) {
        if (GameSingletons.isClient){
            SeamlessPortals.effectManager.createEffect(effectId, startingTime, position, zone, deserializer);
        }
    }
}
