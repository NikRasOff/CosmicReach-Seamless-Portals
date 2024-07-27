package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.IPortalWorldLoader;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.ClientWorldLoader;
import finalforeach.cosmicreach.world.EntityChunk;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.worldgen.ChunkColumn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ClientWorldLoader.class)
public abstract class ClientWorldLoaderMixin implements IPortalWorldLoader {
    @Shadow protected abstract void loadSurroundingChunks(Zone zone, int playerChunkX, int playerChunkY, int playerChunkZ, int localGenRadiusInChunks, int lesserRadius);

    @Shadow private Array<ChunkColumn> chunkColumnsToGenerate;

    @Inject(method = "lambda$unloadFarAwayChunks$1", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/worldgen/ChunkColumn;getChunks(Lfinalforeach/cosmicreach/world/Zone;Lcom/badlogic/gdx/utils/Array;)Lcom/badlogic/gdx/utils/Array;"), cancellable = true)
    void stopUnloadingChunksNearPortals(int playerChunkX, int playerChunkZ, int playerChunkY, int chunkRadius, Zone zone, Array tmpColChunks, ChunkColumn cc, CallbackInfo ci){
        if (!isChunkOutsidePortalRange(cc)){
            ci.cancel();
        }
    }

    @Inject(method = "lambda$unloadFarAwayChunks$2", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/utils/Array;contains(Ljava/lang/Object;Z)Z"), cancellable = true)
    static private void stopUnloadingEntityChunksNearPortals(int playerChunkX, int playerChunkZ, int playerChunkY, int chunkRadius, Array entityRegionsToSave, Array entityChunksToRemove, EntityChunk ec, CallbackInfo ci){
        if (ec.hasEntities() && !isEntityChunkOutsidePortalRange(ec)){
            ci.cancel();
        }
    }

    @Unique
    static private boolean isEntityChunkOutsidePortalRange(EntityChunk ec){
        int portalChunkLoadRadius = 2;
        float minPortalResult = 1000000;
        boolean tooFarVertically = true;
        for (Map.Entry<Integer, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()) {
            if (!portalEntry.getValue().isPortalInRange(true)){
                continue;
            }
            int portalChunkX = Math.floorDiv((int) portalEntry.getValue().position.x, 16);
            int portalChunkY = Math.floorDiv((int) portalEntry.getValue().position.y, 16);
            int portalChunkZ = Math.floorDiv((int) portalEntry.getValue().position.z, 16);
            if (Math.abs(Math.floorDiv(ec.chunkY, 16) - Math.floorDiv(portalChunkY, 16)) <= 1){
                tooFarVertically = false;
            }
            float porRes = Vector2.dst2(ec.chunkX, ec.chunkZ, portalChunkX, portalChunkZ);
            if (porRes < minPortalResult) minPortalResult = porRes;
        }
        return tooFarVertically || (minPortalResult > 1 +  portalChunkLoadRadius * portalChunkLoadRadius);
    }

    @Unique
    static private boolean isChunkOutsidePortalRange(ChunkColumn cc){
        int portalChunkLoadRadius = 2;
        float minPortalResult = 1000000;
        boolean tooFarVertically = true;
        for (Map.Entry<Integer, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()) {
            if (!portalEntry.getValue().isPortalInRange(true)){
                continue;
            }
            int portalChunkX = Math.floorDiv((int) portalEntry.getValue().position.x, 16);
            int portalChunkY = Math.floorDiv((int) portalEntry.getValue().position.y, 16);
            int portalChunkZ = Math.floorDiv((int) portalEntry.getValue().position.z, 16);
            if (Math.abs(Math.floorDiv(cc.chunkY, 16) - Math.floorDiv(portalChunkY, 16)) <= 1){
                tooFarVertically = false;
            }
            float porRes = Vector2.dst2(cc.chunkX, cc.chunkZ, portalChunkX, portalChunkZ);
            if (porRes < minPortalResult) minPortalResult = porRes;
        }
        return tooFarVertically || (minPortalResult > 1 + portalChunkLoadRadius * portalChunkLoadRadius);
    }

    @Inject(method = "generateWorld", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/ClientWorldLoader;loadSurroundingChunks(Lfinalforeach/cosmicreach/world/Zone;IIIII)V"))
    void loadChunksWithPortals(World world, Zone zone, CallbackInfo ci){
        int portalChunkLoadRadius = 2;
        for (Map.Entry<Integer, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()) {
            if (!portalEntry.getValue().isPortalInRange(true)){
                continue;
            }
            int portalChunkX = Math.floorDiv((int) portalEntry.getValue().position.x, 16);
            int portalChunkY = Math.floorDiv((int) portalEntry.getValue().position.y, 16);
            int portalChunkZ = Math.floorDiv((int) portalEntry.getValue().position.z, 16);
            this.loadSurroundingChunks(zone, portalChunkX, portalChunkY, portalChunkZ, portalChunkLoadRadius, 6);
        }
    }

    @Override
    public void removeChunkColumnFromGen(ChunkColumn cc) {
        System.out.println("Deleted one! X:" + cc.chunkX + " Y:" + cc.chunkY + " Z:" + cc.chunkZ);
        boolean result = chunkColumnsToGenerate.removeValue(cc, true);
        System.out.println(chunkColumnsToGenerate.contains(cc, true));
        System.out.println(result);
    }
}
