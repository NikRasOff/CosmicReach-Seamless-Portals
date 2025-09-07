package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.math.Vector2;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.server.ServerIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.world.EntityChunk;
import finalforeach.cosmicreach.world.Zone;
import finalforeach.cosmicreach.world.ZoneLoader;
import finalforeach.cosmicreach.worldgen.ChunkColumn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ZoneLoader.class)
public abstract class ZoneLoaderMixin{
    @Shadow @Final public Zone zone;

    @Shadow protected abstract int getPlayerRadius(Player player, int playerRadius);

    @Shadow public abstract boolean isSaveRequested();

    @Shadow public abstract boolean loadColumn(Player p, Zone zone, int chunkX, int chunkY, int chunkZ, boolean shouldGenerate);

    @Inject(method = "shouldUnloadChunkColumn", at = @At(value = "HEAD"), cancellable = true)
    void stopUnloadingChunksNearPortals(Player p, ChunkColumn cc, int chunkRadius, CallbackInfoReturnable<Boolean> cir){
        if (!cosmicReach_Seamless_Portals$isChunkOutsidePortalRange(cc, chunkRadius)){
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "lambda$unloadFarAwayEntityChunks$1", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/ZoneLoader;getPlayerChunkX(Lfinalforeach/cosmicreach/entities/player/Player;)I"), cancellable = true)
    private void stopUnloadingEntityChunksNearPortals(EntityChunk ec, int chunkRadius, Player p, CallbackInfoReturnable<Boolean> cir){
        if (ec.hasEntities() && !cosmicReach_Seamless_Portals$isEntityChunkOutsidePortalRange(ec, chunkRadius)){
            cir.setReturnValue(false);
        }
    }

    @Unique
    static private boolean cosmicReach_Seamless_Portals$isEntityChunkOutsidePortalRange(EntityChunk ec, int chunkRadius){
        int portalChunkLoadRadius = 2;
        float minPortalResult = 1000000;
        boolean tooFarVertically = true;
        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()) {
            if (!portalEntry.getValue().isPortalInRange(true, chunkRadius)){
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
    static private boolean cosmicReach_Seamless_Portals$isChunkOutsidePortalRange(ChunkColumn cc, int chunkRadius){
        int portalChunkLoadRadius = 2;
        float minPortalResult = 1000000;
        boolean tooFarVertically = true;
        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()) {
            if (!portalEntry.getValue().isPortalInRange(true, chunkRadius)){
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

    @Unique
    private void cosmicReach_Seamless_Portals$loadChunksNearPortal(Zone zone, Portal portal, Player player, int localGenRadiusInChunks, int lesserRadius) {
        int portalChunkX = Math.floorDiv((int) portal.position.x, 16);
        int portalChunkY = Math.floorDiv((int) portal.position.y, 16);
        int portalChunkZ = Math.floorDiv((int) portal.position.z, 16);
        int columnVertRenderDist = 1;
        int playerRadius = this.getPlayerRadius(player, localGenRadiusInChunks);
        boolean playerNeedsLoading = false;

        for(int v = -columnVertRenderDist; v <= columnVertRenderDist; ++v) {
            int chunkY = (Math.floorDiv(portalChunkY, 16) + v) * 16;

            for(int i = -playerRadius; i <= playerRadius; ++i) {
                if (player.isLoading() && playerRadius > lesserRadius) {
                    return;
                }

                for(int k = -playerRadius; k <= playerRadius; ++k) {
                    if (this.isSaveRequested()) {
                        return;
                    }

                    int chunkX = portalChunkX + i;
                    int chunkZ = portalChunkZ + k;
                    float chunkDistSq = Vector2.dst2((float)chunkX, (float)chunkZ, (float)portalChunkX, (float)portalChunkZ);
                    boolean distCheck = chunkDistSq <= (float)(1 + playerRadius * playerRadius);
                    if (distCheck) {
                        boolean shouldGenerate = GameSingletons.isHost && distCheck;
                        playerNeedsLoading |= this.loadColumn(player, zone, chunkX, chunkY, chunkZ, shouldGenerate);
                    }
                }
            }
        }

        if (!playerNeedsLoading && player.isLoading()) {
            player.loading = false;
            ServerIdentity c = ServerSingletons.SERVER != null ? ServerSingletons.getConnection(player) : null;
            if (c != null) {
                c.getSettings().setProperty("CLIENT_DONE_LOADING_ON_JOIN_KEY", true, true);
            }
        }

    }

    @Inject(method = "lambda$loadSurroundingChunks$0", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/ZoneLoader;loadSurroundingChunks(Lfinalforeach/cosmicreach/world/Zone;Lfinalforeach/cosmicreach/entities/player/Player;II)V"))
    void loadChunksWithPortals(int localGenRadiusInChunks, int lesserRadius, Player p, CallbackInfo ci){
        int portalChunkLoadRadius = 2;
        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()) {
            if (!portalEntry.getValue().isPortalInRange(true, localGenRadiusInChunks)){
                continue;
            }
            this.cosmicReach_Seamless_Portals$loadChunksNearPortal(zone, portalEntry.getValue(), p, portalChunkLoadRadius, portalChunkLoadRadius);
        }
    }
}
