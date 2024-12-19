package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import com.github.puzzle.game.items.data.DataTag;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.items.data.attributes.IntDataAttribute;
import com.github.puzzle.game.items.data.attributes.StringDataAttribute;
import com.github.puzzle.game.items.data.attributes.Vector3DataAttribute;
import com.github.puzzle.game.util.DataTagUtil;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.items.HandheldPortalGen;
import com.nikrasoff.seamlessportals.networking.packets.PortalAnimationPacket;
import com.nikrasoff.seamlessportals.networking.packets.UpdatePortalPacket;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.PooledBlockPosition;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.packets.ContainerSyncPacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class ExtraPortalUtils {
    private static final Ray ray = new Ray();
    private static final Array<BlockPosition> toVisit = new Array<>();
    private static final Vector3 workingPos = new Vector3();
    private static final Queue<BlockPosition> blockQueue = new Queue<>();
    private static final Array<BlockPosition> positionsToFree = new Array<>();
    private static final BoundingBox tmpBoundingBox = new BoundingBox();
    private static final BoundingBox hitBB = new BoundingBox();
    private static final Array<BoundingBox> tmpBoundingBoxes = new Array<>(BoundingBox.class);
    private static final Vector3 intersection = new Vector3();
    static Pool<BlockPosition> positionPool = new Pool<BlockPosition>() {
        protected BlockPosition newObject() {
            PooledBlockPosition<BlockPosition> p = new PooledBlockPosition<>(ExtraPortalUtils.positionPool, (Chunk)null, 0, 0, 0);
            ExtraPortalUtils.positionsToFree.add(p);
            return p;
        }
    };

    private static void addBlockToQueue(Zone zone, BlockPosition bp, int dx, int dy, int dz) {
        BlockPosition step = bp.getOffsetBlockPos(positionPool, zone, dx, dy, dz);
        if (step != null && !toVisit.contains(step, false)) {
            BlockState block = bp.getBlockState();
            if (block != null) {
                block.getBoundingBox(tmpBoundingBox, step);
                if (Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
                    blockQueue.addLast(step);
                    toVisit.add(step);
                }
            }
        }

    }

    private static boolean intersectsWithBlock(BlockState block, BlockPosition nextBlockPos) {
        block.getBoundingBox(tmpBoundingBox, nextBlockPos);
        if (!Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
            return false;
        } else {
            block.getAllBoundingBoxes(tmpBoundingBoxes, nextBlockPos);
            Array.ArrayIterator<BoundingBox> var3 = tmpBoundingBoxes.iterator();

            BoundingBox bb;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                bb = var3.next();
            } while(!Intersector.intersectRayBounds(ray, bb, intersection));

            hitBB.set(bb);
            return true;
        }
    }

    public static RaycastOutput raycast(Zone zone, Vector3 origin, Vector3 direction, float length){
        boolean raycastHit = false;
        BlockPosition hitBlockPos = null;
        BlockPosition lastBlockPosAtPoint = null;
        toVisit.clear();
        blockQueue.clear();

        ray.set(origin, direction);

        workingPos.set(ray.origin);

        for(; workingPos.dst(ray.origin) <= length; workingPos.add(ray.direction)) {
            int bx = (int)Math.floor((double)workingPos.x);
            int by = (int)Math.floor((double)workingPos.y);
            int bz = (int)Math.floor((double)workingPos.z);
            int dx = 0;
            int dy = 0;
            int dz = 0;
            if (lastBlockPosAtPoint != null) {
                if (lastBlockPosAtPoint.isAtGlobal(bx, by, bz)) {
                    continue;
                }

                dx = bx - lastBlockPosAtPoint.getGlobalX();
                dy = by - lastBlockPosAtPoint.getGlobalY();
                dz = bz - lastBlockPosAtPoint.getGlobalZ();
            }

            Chunk c = zone.getChunkAtBlock(bx, by, bz);
            if (c == null) {
                continue;
            }

            BlockPosition nextBlockPos = (BlockPosition)positionPool.obtain();
            nextBlockPos.set(c, bx - c.blockX, by - c.blockY, bz - c.blockZ);
            if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1) {
                if (dx != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, dx, 0, 0);
                }

                if (dy != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, 0, dy, 0);
                }

                if (dz != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, 0, 0, dz);
                }

                if (dx != 0 && dy != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, dx, dy, 0);
                }

                if (dx != 0 && dz != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, dx, 0, dz);
                }

                if (dy != 0 && dz != 0) {
                    addBlockToQueue(zone, lastBlockPosAtPoint, 0, dy, dz);
                }
            }

            if (!toVisit.contains(nextBlockPos, false)) {
                BlockState block = nextBlockPos.getBlockState();
                block.getBoundingBox(tmpBoundingBox, nextBlockPos);
                if (Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
                    blockQueue.addLast(nextBlockPos);
                    toVisit.add(nextBlockPos);
                } else if (block.canRaycastForReplace()) {
                    tmpBoundingBox.min.set((float)nextBlockPos.getGlobalX(), (float)nextBlockPos.getGlobalY(), (float)nextBlockPos.getGlobalZ());
                    tmpBoundingBox.max.set(tmpBoundingBox.min).add(1.0F, 1.0F, 1.0F);
                    if (Intersector.intersectRayBounds(ray, tmpBoundingBox, intersection)) {
                        blockQueue.addLast(nextBlockPos);
                        toVisit.add(nextBlockPos);
                    }
                }
            }

            label186:
            while(true) {
                BlockState blockState;
                BlockPosition curBlockPos;
                do {
                    if (!blockQueue.notEmpty()) {
                        break label186;
                    }

                    curBlockPos = blockQueue.removeFirst();
                    blockState = curBlockPos.getBlockState();
                } while(!blockState.hasEmptyModel() && !intersectsWithBlock(blockState, curBlockPos));
                if (blockState.canRaycastForBreak()) {
                    hitBlockPos = curBlockPos;
                    raycastHit = true;
                }

                if (hitBlockPos != null){
                    break;
                }
            }

            if (raycastHit) {
                break;
            }

            lastBlockPosAtPoint = nextBlockPos;
        }

        positionPool.freeAll(positionsToFree);

        if (!raycastHit){
            return null;
        }

        Vector3 hitBBCenter = new Vector3();
        hitBB.getCenter(hitBBCenter);
        Vector3 hitBBSize = new Vector3();
        hitBB.getDimensions(hitBBSize);
        Vector3 normal = intersection.cpy().sub(hitBBCenter);
        normal.x /= hitBBSize.x;
        normal.y /= hitBBSize.y;
        normal.z /= hitBBSize.z;

        return new RaycastOutput(intersection, DirectionVector.getClosestDirection(normal), hitBlockPos);
    }
    public static void fireHpg(Player player, boolean isSecondPortal, ItemStack hpgItemStack){
        DataTagManifest hpgManifest = DataTagUtil.getManifestFromStack(hpgItemStack);

        if (!hpgManifest.hasTag("portal1Chunk")){
            hpgManifest.setTag("portal1Chunk", new DataTag<>("p1Chunk", new Vector3DataAttribute(new Vector3())));
        }
        if (!hpgManifest.hasTag("portal1Id")){
            hpgManifest.setTag("portal1Id", new DataTag<>("p1Id", new IntDataAttribute(-1)));
        }
        if (!hpgManifest.hasTag("portal1Zone")){
            hpgManifest.setTag("portal1Zone", new DataTag<>("p1Zone", new StringDataAttribute(GameSingletons.world.defaultZoneId)));
        }
        DataTag<Vector3> primaryPortalChunkPos = hpgManifest.getTag("portal1Chunk");
        DataTag<Integer> primaryPortalId = hpgManifest.getTag("portal1Id");
        DataTag<String> primaryPortalZone = hpgManifest.getTag("portal1Zone");

        if (!hpgManifest.hasTag("portal2Chunk")){
            hpgManifest.setTag("portal2Chunk", new DataTag<>("p2Chunk", new Vector3DataAttribute(new Vector3())));
        }
        if (!hpgManifest.hasTag("portal2Id")){
            hpgManifest.setTag("portal2Id", new DataTag<>("p2Id", new IntDataAttribute(-1)));
        }
        if (!hpgManifest.hasTag("portal2Zone")){
            hpgManifest.setTag("portal2Zone", new DataTag<>("p2Zone", new StringDataAttribute(GameSingletons.world.defaultZoneId)));
        }
        DataTag<Vector3> secondaryPortalChunkPos = hpgManifest.getTag("portal2Chunk");
        DataTag<Integer> secondaryPortalId = hpgManifest.getTag("portal2Id");
        DataTag<String> secondaryPortalZone = hpgManifest.getTag("portal2Zone");

        PortalManager pm = SeamlessPortals.portalManager;
        workingPos.set(player.getPosition()).add(player.getEntity().viewPositionOffset);
        RaycastOutput result = raycast(player.getZone(), workingPos, player.getEntity().viewDirection, 1000F);
        if (!isSecondPortal){
            if (result != null){
                Portal prPortal = pm.getPortalWithGen(primaryPortalId.getValue(), primaryPortalChunkPos.getValue(), primaryPortalZone.getValue());
                Portal secPortal = pm.getPortalWithGen(secondaryPortalId.getValue(), secondaryPortalChunkPos.getValue(), secondaryPortalZone.getValue());
                if (prPortal == null){
                    Vector3 upDir = getUpVectorForPortals(result.hitNormal(), player);
                    Portal newPortal = new Portal(new Vector2(1, 2), result.hitNormal().getVector().cpy().scl(-1), upDir, getPositionForPortals(result.hitPos(), result.hitNormal()));
                    if (!newPortal.figureOutPlacement(player.getZone(), 0.5f, 0.5f, 1, 1, HandheldPortalGen.surfaceBlacklist, true)){
                        SeamlessPortals.portalManager.removePortal(newPortal);
                        return;
                    }
                    primaryPortalId.attribute.setValue(newPortal.getPortalID());
                    primaryPortalChunkPos.attribute.getValue().set(Math.floorDiv((int) newPortal.position.x, 16), Math.floorDiv((int) newPortal.position.y, 16), Math.floorDiv((int) newPortal.position.z, 16));
                    primaryPortalZone.attribute.setValue(player.zoneId);
                    if (secondaryPortalId.getValue() != -1){
                        if (secPortal == null){
                            secondaryPortalId.attribute.setValue(-1);
                        }
                        else{
                            newPortal.linkPortal(secPortal);
                            secPortal.linkPortal(newPortal);
                            if (GameSingletons.isClient){
                                secPortal.playAnimation("rebind");
                            }
                            if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                                ServerSingletons.SERVER.broadcast(secPortal.zone, new PortalAnimationPacket(secPortal.getPortalID(), "rebind"));
                            }
                        }
                    }
                    player.getZone().addEntity(newPortal);
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                        ServerSingletons.SERVER.broadcast(newPortal.zone, new PortalAnimationPacket(newPortal.getPortalID(), "start"));
                    }
                }
                else{
                    Vector3 originalPos = prPortal.position.cpy();
                    Vector3 originalDir = prPortal.viewDirection.cpy();
                    Vector3 originalUpVector = prPortal.upVector.cpy();

                    prPortal.setPosition(getPositionForPortals(result.hitPos(), result.hitNormal()));
                    prPortal.viewDirection = result.hitNormal().getVector().cpy().scl(-1);
                    prPortal.upVector = getUpVectorForPortals(result.hitNormal(), player);

                    if (!prPortal.figureOutPlacement(player.getZone(), 0.5f, 0.5f, 1f, 1f, HandheldPortalGen.surfaceBlacklist, true)){
                        prPortal.setPosition(originalPos);
                        prPortal.viewDirection.set(originalDir);
                        prPortal.upVector.set(originalUpVector);
                        return;
                    }

                    if (GameSingletons.isClient){
                        if (secPortal != null){
                            secPortal.playAnimation("rebind");
                        }
                        prPortal.playAnimation("start");
                    }
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                        ServerSingletons.SERVER.broadcast(prPortal.zone, new UpdatePortalPacket(prPortal));
                        if (secPortal != null){
                            ServerSingletons.SERVER.broadcast(secPortal.zone, new PortalAnimationPacket(secPortal.getPortalID(), "rebind"));
                        }
                        ServerSingletons.SERVER.broadcast(prPortal.zone, new PortalAnimationPacket(prPortal.getPortalID(), "start"));
                    }
                }
            }
        }
        else {
            if (result != null){
                Portal secPortal = pm.getPortalWithGen(secondaryPortalId.getValue(), secondaryPortalChunkPos.getValue(), primaryPortalZone.getValue());
                Portal prPortal = pm.getPortalWithGen(primaryPortalId.getValue(), primaryPortalChunkPos.getValue(), secondaryPortalZone.getValue());

                if (secPortal == null){
                    Vector3 upDir = getUpVectorForPortals(result.hitNormal(), player);
                    Portal newPortal = new Portal(new Vector2(1, 2), result.hitNormal().getVector(), upDir, getPositionForPortals(result.hitPos(), result.hitNormal()));
                    if (!newPortal.figureOutPlacement(player.getZone(), 0.5f, 0.5f, 1, 1, HandheldPortalGen.surfaceBlacklist, false)){
                        SeamlessPortals.portalManager.removePortal(newPortal);
                        return;
                    }
                    secondaryPortalId.attribute.setValue(newPortal.getPortalID());
                    secondaryPortalChunkPos.attribute.getValue().set(Math.floorDiv((int) newPortal.position.x, 16), Math.floorDiv((int) newPortal.position.y, 16), Math.floorDiv((int) newPortal.position.z, 16));
                    secondaryPortalZone.attribute.setValue(player.zoneId);
                    if (primaryPortalId.getValue() != -1){
                        if (prPortal == null){
                            primaryPortalId.attribute.setValue(-1);
                        }
                        else{
                            newPortal.linkPortal(prPortal);
                            prPortal.linkPortal(newPortal);
                            if (GameSingletons.isClient){
                                prPortal.playAnimation("rebind");
                            }
                            if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                                ServerSingletons.SERVER.broadcast(prPortal.zone, new PortalAnimationPacket(prPortal.getPortalID(), "rebind"));
                            }
                        }
                    }
                    player.getZone().addEntity(newPortal);
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                        ServerSingletons.SERVER.broadcast(newPortal.zone, new PortalAnimationPacket(newPortal.getPortalID(), "start"));
                    }
                }
                else{
                    Vector3 originalPos = secPortal.position.cpy();
                    Vector3 originalDir = secPortal.viewDirection.cpy();
                    Vector3 originalUpVector = secPortal.upVector.cpy();

                    secPortal.setPosition(getPositionForPortals(result.hitPos(), result.hitNormal()));
                    secPortal.viewDirection = result.hitNormal().getVector();
                    secPortal.upVector = getUpVectorForPortals(result.hitNormal(), player);

                    if (!secPortal.figureOutPlacement(player.getZone(), 0.5f, 0.5f, 1f, 1f, HandheldPortalGen.surfaceBlacklist, false)){
                        secPortal.setPosition(originalPos);
                        secPortal.viewDirection.set(originalDir);
                        secPortal.upVector.set(originalUpVector);
                        return;
                    }

                    if (GameSingletons.isClient){
                        if (prPortal != null){
                            prPortal.playAnimation("rebind");
                        }
                        secPortal.playAnimation("start");
                    }
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                        ServerSingletons.SERVER.broadcast(secPortal.zone, new UpdatePortalPacket(secPortal));
                        if (prPortal != null){
                            ServerSingletons.SERVER.broadcast(prPortal.zone, new PortalAnimationPacket(prPortal.getPortalID(), "rebind"));
                        }
                        ServerSingletons.SERVER.broadcast(secPortal.zone, new PortalAnimationPacket(secPortal.getPortalID(), "start"));
                    }
                }
            }
        }
        if (!GameSingletons.isClient || GameSingletons.client().getLocalPlayer() != player){
            ServerSingletons.getConnection(player).send(new ContainerSyncPacket(0, player.inventory));
        }
    }
    private static Vector3 getUpVectorForPortals(DirectionVector dv, Player pl){
        switch (dv.getName()){
            case "posY" -> {
                Vector3 upDir = new Vector3(pl.getEntity().viewDirection);
                upDir.y = 0;
                upDir.nor();
                DirectionVector closestVec = DirectionVector.getClosestHorizontalDirection(upDir);
                if (closestVec.getVector().dot(upDir) > 0.96) return closestVec.getVector().cpy();
                return upDir;
            }
            case "negY" -> {
                Vector3 upDir = new Vector3(pl.getEntity().viewDirection);
                upDir.y = 0;
                upDir.scl(-1);
                upDir.nor();
                DirectionVector closestVec = DirectionVector.getClosestHorizontalDirection(upDir);
                if (closestVec.getVector().dot(upDir) > 0.96) return closestVec.getVector().cpy();
                return upDir;
            }
            default -> {
                return new Vector3(0 ,1, 0);
            }
        }
    }

    private static Vector3 getPositionForPortals(Vector3 pos, DirectionVector normal){
        switch (normal.getName()){
            case "posY", "negY" -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.04F));
                newPos.x = (float) (Math.round(newPos.x * 2) / 2.0);
                newPos.z = (float) (Math.round(newPos.z * 2) / 2.0);
                return newPos;
            }
            case "posX", "negX" -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.05F));
                newPos.y = (float) Math.round(newPos.y);
                newPos.z = (float) (Math.round(newPos.z * 2) / 2.0);
                return newPos;
            }
            case "posZ", "negZ" -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.05F));
                newPos.y = (float) Math.round(newPos.y);
                newPos.x = (float) (Math.round(newPos.x * 2) / 2.0);
                return newPos;
            }
            default -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.05F));
                newPos.y = (float) Math.floor(newPos.y + 0.5);
                return newPos;
            }
        }
    }
    public static void clearPortals(Player player, ItemStack hpgItemStack){
        DataTagManifest hpgManifest = DataTagUtil.getManifestFromStack(hpgItemStack);

        if (!hpgManifest.hasTag("portal1Chunk")){
            hpgManifest.setTag("portal1Chunk", new DataTag<>("p1Chunk", new Vector3DataAttribute(new Vector3())));
        }
        if (!hpgManifest.hasTag("portal1Id")){
            hpgManifest.setTag("portal1Id", new DataTag<>("p1Id", new IntDataAttribute(-1)));
        }
        if (!hpgManifest.hasTag("portal1Zone")){
            hpgManifest.setTag("portal1Zone", new DataTag<>("p1Zone", new StringDataAttribute(GameSingletons.world.defaultZoneId)));
        }
        DataTag<Vector3> primaryPortalChunkPos = hpgManifest.getTag("portal1Chunk");
        DataTag<Integer> primaryPortalId = hpgManifest.getTag("portal1Id");
        DataTag<String> primaryPortalZone = hpgManifest.getTag("portal1Zone");

        if (!hpgManifest.hasTag("portal2Chunk")){
            hpgManifest.setTag("portal2Chunk", new DataTag<>("p2Chunk", new Vector3DataAttribute(new Vector3())));
        }
        if (!hpgManifest.hasTag("portal2Id")){
            hpgManifest.setTag("portal2Id", new DataTag<>("p2Id", new IntDataAttribute(-1)));
        }
        if (!hpgManifest.hasTag("portal2Zone")){
            hpgManifest.setTag("portal2Zone", new DataTag<>("p2Zone", new StringDataAttribute(GameSingletons.world.defaultZoneId)));
        }
        DataTag<Vector3> secondaryPortalChunkPos = hpgManifest.getTag("portal2Chunk");
        DataTag<Integer> secondaryPortalId = hpgManifest.getTag("portal2Id");
        DataTag<String> secondaryPortalZone = hpgManifest.getTag("portal2Zone");

        PortalManager pm = SeamlessPortals.portalManager;

        if (primaryPortalId.getValue() != -1){
            Portal primaryPortal = pm.getPortalWithGen(primaryPortalId.getValue(), primaryPortalChunkPos.getValue(), primaryPortalZone.getValue());
            if (primaryPortal != null){
                primaryPortal.startDestruction();
            }
        }
        if (secondaryPortalId.getValue() != -1){
            Portal secondaryPortal = pm.getPortalWithGen(secondaryPortalId.getValue(), secondaryPortalChunkPos.getValue(), secondaryPortalZone.getValue());
            if (secondaryPortal != null){
                secondaryPortal.startDestruction();
            }
        }

        primaryPortalId.attribute.setValue(-1);
        secondaryPortalId.attribute.setValue(-1);

        if (!GameSingletons.isClient || GameSingletons.client().getLocalPlayer() != player){
            ServerSingletons.getConnection(player).send(new ContainerSyncPacket(0, player.inventory));
        }
    }
}
