package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.math.Vector2;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.networking.server.ServerIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.util.ArrayUtils;
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

@Mixin(ZoneLoader.class)
public abstract class ZoneLoaderMixin{
    @Shadow @Final public Zone zone;

    @Shadow protected abstract int getPlayerRadius(Player player, int playerRadius);

    @Shadow public abstract boolean isSaveRequested();

    @Shadow public abstract boolean loadColumn(Player p, Zone zone, int chunkX, int chunkY, int chunkZ, boolean shouldGenerate);

    @Shadow
    boolean tmpShouldUnload;

    @Unique
    boolean cosmicReach_Seamless_Portals$existUnloadedPortals;

    @Shadow
    protected abstract boolean loadSurroundingColumn(Player player, int chunkX, int chunkZ);

    @Inject(method = "shouldUnloadChunkColumn", at = @At(value = "HEAD"), cancellable = true)
    void stopUnloadingChunksNearPortals(Player p, ChunkColumn cc, int chunkRadius, CallbackInfoReturnable<Boolean> cir){
        if (!cosmicReach_Seamless_Portals$isChunkOutsidePortalRange(cc, chunkRadius)){
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "lambda$unloadFarAwayEntityChunks$1", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/ZoneLoader;getPlayerChunkX(Lfinalforeach/cosmicreach/entities/player/Player;)I"), cancellable = true)
    private void stopUnloadingEntityChunksNearPortals(EntityChunk ec, int chunkRadius, Player p, CallbackInfoReturnable<Boolean> cir){
        if (ec.hasEntities() && !cosmicReach_Seamless_Portals$isEntityChunkOutsidePortalRange(ec, chunkRadius)){
            this.tmpShouldUnload = false;
            cir.setReturnValue(false);
        }
    }

    @Unique
    static private boolean cosmicReach_Seamless_Portals$isEntityChunkOutsidePortalRange(EntityChunk ec, int chunkRadius){
        int portalChunkLoadRadius = 2;
        float minPortalResult = 1000000;
        boolean tooFarVertically = true;

        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
        for (Portal portal : portals) {
            if (!portal.isPortalInRange(true, chunkRadius)){
                continue;
            }
            int portalChunkX = Math.floorDiv((int) portal.position.x, 16);
            int portalChunkY = Math.floorDiv((int) portal.position.y, 16);
            int portalChunkZ = Math.floorDiv((int) portal.position.z, 16);
            if (Math.abs(Math.floorDiv(ec.chunkY, 16) - Math.floorDiv(portalChunkY, 16)) <= 1){
                tooFarVertically = false;
            }
            float porRes = Vector2.dst2(ec.chunkX, ec.chunkZ, portalChunkX, portalChunkZ);
            if (porRes < minPortalResult) minPortalResult = porRes;
        }
        return tooFarVertically || (minPortalResult > 1 + portalChunkLoadRadius * portalChunkLoadRadius);
    }

    @Unique
    static private boolean cosmicReach_Seamless_Portals$isChunkOutsidePortalRange(ChunkColumn cc, int chunkRadius){
        int portalChunkLoadRadius = 2;
        float minPortalResult = 1000000;
        boolean tooFarVertically = true;
        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
        for (Portal portal : portals) {
            if (!portal.isPortalInRange(true, chunkRadius)){
                continue;
            }
            int portalChunkX = Math.floorDiv((int) portal.position.x, 16);
            int portalChunkY = Math.floorDiv((int) portal.position.y, 16);
            int portalChunkZ = Math.floorDiv((int) portal.position.z, 16);
            if (Math.abs(Math.floorDiv(cc.chunkY, 16) - Math.floorDiv(portalChunkY, 16)) <= 1){
                tooFarVertically = false;
            }
            float porRes = Vector2.dst2(cc.chunkX, cc.chunkZ, portalChunkX, portalChunkZ);
            if (porRes < minPortalResult) minPortalResult = porRes;
        }
        return tooFarVertically || (minPortalResult > 1 + portalChunkLoadRadius * portalChunkLoadRadius);
    }

    @Unique
    private boolean cosmicReach_Seamless_Portals$loadSurroundingColumnPortal(Player player, int chunkX, int chunkZ, Portal portal){
        boolean playerNeedsLoading = false;
        int portalChunkY = Math.floorDiv((int) portal.position.y, 16);
        int regionChunkY = Math.floorDiv(portalChunkY, 16) * 16;
        int columnVertRenderDist = 1;
        int start = 0;
        if (portalChunkY - regionChunkY < 8 || portalChunkY > 0) {
            start = -columnVertRenderDist;
        }

        int end = columnVertRenderDist;

        for(int v = start; v <= end; ++v) {
            int chunkY = (Math.floorDiv(portalChunkY, 16) + v) * 16;
            if (this.isSaveRequested()) {
                return playerNeedsLoading;
            }

            boolean shouldGenerate = GameSingletons.isHost;
            playerNeedsLoading |= this.loadColumn(player, this.zone, chunkX, chunkY, chunkZ, shouldGenerate);
        }

        return playerNeedsLoading;
    }

    @Unique
    private void cosmicReach_Seamless_Portals$loadChunksNearPortal(Zone zone, Portal portal, Player player, int localGenRadiusInChunks, int lesserRadius) {
        int portalChunkX = Math.floorDiv((int) portal.position.x, 16);
        int portalChunkZ = Math.floorDiv((int) portal.position.z, 16);
        int portalRadius = 2;
        boolean playerNeedsLoading = false;
        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = -1;
        int maxDist = portalRadius * 2 + 1;
        boolean loadedRing = false;

        for(int i = 0; i < maxDist * maxDist; ++i) {
            if (Math.abs(x) <= portalRadius && Math.abs(z) <= portalRadius) {
                int chunkX = portalChunkX + x;
                int chunkZ = portalChunkZ + z;
                float chunkDistSq = Vector2.dst2((float)chunkX, (float)chunkZ, (float)portalChunkX, (float)portalChunkZ);
                boolean distCheck = chunkDistSq <= (float)(1 + portalRadius * portalRadius);
                if (distCheck) {
                    playerNeedsLoading |= this.cosmicReach_Seamless_Portals$loadSurroundingColumnPortal(player, chunkX, chunkZ, portal);
                    loadedRing |= playerNeedsLoading;
                }
            }

            if (x == z || x < 0 && x == -z || x > 0 && x == 1 - z) {
                int temp = dx;
                dx = -dz;
                dz = temp;
                if (loadedRing) {
                    break;
                }
            }

            x += dx;
            z += dz;
        }

        if (!playerNeedsLoading && player.isLoading()) {
            player.loading = false;
            ServerIdentity c = ServerSingletons.SERVER != null ? ServerSingletons.getConnection(player) : null;
            if (c != null) {
                c.getSettings().setProperty("CLIENT_DONE_LOADING_ON_JOIN_KEY", true, true);
            }
        }

    }

    @Inject(method = "needToGenDifferentChunks", at = @At("HEAD"), cancellable = true)
    void checkForUnloadedPortals(Zone zone, int localGenRadiusInChunks, CallbackInfoReturnable<Boolean> cir){
        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
        if (ArrayUtils.any(portals, (portal -> portal.zone == null))){
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "lambda$loadSurroundingChunks$0", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/world/ZoneLoader;loadSurroundingChunks(Lfinalforeach/cosmicreach/world/Zone;Lfinalforeach/cosmicreach/entities/player/Player;II)V"))
    void loadChunksWithPortals(int localGenRadiusInChunks, int lesserRadius, Player p, CallbackInfo ci){
        cosmicReach_Seamless_Portals$existUnloadedPortals = false;
        int portalChunkLoadRadius = 2;
        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
        for (Portal portal : portals) {
            if (!portal.isPortalInRange(true, localGenRadiusInChunks)){
                continue;
            }
            this.cosmicReach_Seamless_Portals$loadChunksNearPortal(zone, portal, p, portalChunkLoadRadius, portalChunkLoadRadius);
        }
    }
}
