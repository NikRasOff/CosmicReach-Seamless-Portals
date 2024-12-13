package com.nikrasoff.seamlessportals.networking.packets;

import com.badlogic.gdx.math.Vector2;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import finalforeach.cosmicreach.networking.GamePacket;
import io.netty.buffer.ByteBuf;

public abstract class SPGamePacket extends GamePacket {
    public void readVector2(ByteBuf byteBuf, Vector2 out){
        out.x = this.readFloat(byteBuf);
        out.y = this.readFloat(byteBuf);
    }
    public void readIntVector3(ByteBuf byteBuf, IntVector3 out){
        out.set(this.readInt(byteBuf), this.readInt(byteBuf), this.readInt(byteBuf));
    }
    public void writeVector2(Vector2 v){
        this.writeFloat(v.x);
        this.writeFloat(v.y);
    }
    public void writeIntVector3(IntVector3 v){
        this.writeInt(v.x);
        this.writeInt(v.y);
        this.writeInt(v.z);
    }
}
