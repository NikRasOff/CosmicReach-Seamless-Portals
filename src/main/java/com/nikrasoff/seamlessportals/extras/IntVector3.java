package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

import java.util.Objects;

public class IntVector3 {
    public int x;
    public int y;
    public int z;

    public IntVector3(){
        this(0, 0, 0);
    }

    public IntVector3(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public IntVector3(Vector3 vector){
        this(MathUtils.floor(vector.x), MathUtils.floor(vector.y), MathUtils.floor(vector.z));
    }

    public IntVector3(IntVector3 vector){
        this(vector.x, vector.y, vector.z);
    }

    public IntVector3(BlockPosition blockPos){
        this(blockPos.getGlobalX(), blockPos.getGlobalY(), blockPos.getGlobalZ());
    }
    public IntVector3 set(Vector3 other){
        return this.set(MathUtils.floor(other.x), MathUtils.floor(other.y), MathUtils.floor(other.z));
    }
    public IntVector3 set(IntVector3 other){
        return this.set(other.x, other.y, other.z);
    }
    public IntVector3 set(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public IntVector3 add(IntVector3 other){
        this.x += other.x;
        this.y += other.y;
        this.z += other.z;
        return this;
    }

    public IntVector3 add(int x, int y, int z){
        return this.add(new IntVector3(x, y, z));
    }

    public IntVector3 sub(IntVector3 other){
        this.x -= other.x;
        this.y -= other.y;
        this.z -= other.z;
        return this;
    }

    public IntVector3 scl(int scalar){
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    public IntVector3 cpy(){
        return new IntVector3(this);
    }

    public float lengthSq(){
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public float length(){
        return (float) Math.sqrt(this.lengthSq());
    }

    public Vector3 toVector3(){
        return new Vector3(this.x, this.y, this.z);
    }
    public Vector3 toVector3(Vector3 vector){
        return vector.set(this.x, this.y, this.z);
    }

    public BlockPosition toBlockPosition(Zone zone){
        Chunk c = zone.getChunkAtBlock(x, y, z);
        return new BlockPosition(c, x - c.getBlockX(), y - c.getBlockY(), z - c.getBlockZ());
    }
    public BlockPosition toBlockPosition(){
        return this.toBlockPosition(GameSingletons.world.getDefaultZone());
    }

    public static IntVector3 lesserVector(IntVector3 first, IntVector3 second){
        IntVector3 result = new IntVector3();
        result.x = Math.min(first.x, second.x);
        result.y = Math.min(first.y, second.y);
        result.z = Math.min(first.z, second.z);
        return result;
    }

    public static IntVector3 leastVector(Vector3... v){
        IntVector3 r = new IntVector3(v[0]);
        for(int i = 1; i < v.length; ++i){
            r.set(IntVector3.lesserVector(r, new IntVector3(v[i])));
        }
        return r;
    }

    public static IntVector3 greaterVector(IntVector3 first, IntVector3 second){
        IntVector3 result = new IntVector3();
        result.x = Math.max(first.x, second.x);
        result.y = Math.max(first.y, second.y);
        result.z = Math.max(first.z, second.z);
        return result;
    }

    public static IntVector3 greatestVector(Vector3... v){
        IntVector3 r = new IntVector3(v[0]);
        for(int i = 1; i < v.length; ++i){
            r.set(IntVector3.greaterVector(r, new IntVector3(v[i])));
        }
        return r;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntVector3 that = (IntVector3) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
