package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.items.UnstableHandheldPortalGen;
import com.nikrasoff.seamlessportals.networking.packets.PortalAnimationPacket;
import com.nikrasoff.seamlessportals.networking.packets.UpdatePortalPacket;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import com.nikrasoff.seamlessportals.portals.Portal;
import com.nikrasoff.seamlessportals.portals.PortalManager;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.PooledBlockPosition;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.packets.items.ContainerSyncPacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;
import io.github.puzzle.cosmic.api.util.DataPointUtil;
import io.github.puzzle.cosmic.impl.data.point.DataPointManifest;
import io.github.puzzle.cosmic.impl.data.point.single.IntegerDataPoint;
import io.github.puzzle.cosmic.impl.data.point.single.LongDataPoint;
import io.github.puzzle.cosmic.impl.data.point.single.StringDataPoint;
import io.github.puzzle.cosmic.impl.data.point.single.Vector3DataPoint;

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
        // I have next to no idea how this works as I simply stole the code from BlockSelection and repurposed it here
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

    private static HPGPortal fixedGetPortal(EntityUniqueId id, Vector3 chunkPos, String zone){
        if (id.getTime() == -1){
            return null;
        }
        return (HPGPortal) SeamlessPortals.portalManager.getPortalWithGen(id, chunkPos, zone);
    }

    public static void fireHpg(Player player, boolean isSecondPortal, ItemStack hpgItemStack){
        // ah yes, the CODE MONOLITH, my favorite!
        boolean unstable = hpgItemStack.getItem() instanceof UnstableHandheldPortalGen;
        DataPointManifest hpgManifest = (DataPointManifest) DataPointUtil.getManifestFromStack(hpgItemStack);

        if (!hpgManifest.has("portal1Chunk")){
            hpgManifest.put("portal1Chunk", new Vector3DataPoint(new Vector3()));
        }
        if (!hpgManifest.has("portal1IdTime")){
            hpgManifest.put("portal1IdTime", new LongDataPoint((long) -1));
        }
        if (!hpgManifest.has("portal1IdRand")){
            hpgManifest.put("portal1IdRand", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal1IdNum")){
            hpgManifest.put("portal1IdNum", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal1Zone")){
            hpgManifest.put("portal1Zone", new StringDataPoint(GameSingletons.world.defaultZoneId));
        }
        Vector3DataPoint primaryPortalChunkPos = (Vector3DataPoint) hpgManifest.get("portal1Chunk");
        IntegerDataPoint primaryPortalIdRand = (IntegerDataPoint) hpgManifest.get("portal1IdRand");
        LongDataPoint primaryPortalIdTime = (LongDataPoint) hpgManifest.get("portal1IdTime");
        IntegerDataPoint primaryPortalIdNum = (IntegerDataPoint) hpgManifest.get("portal1IdNum");
        StringDataPoint primaryPortalZone = (StringDataPoint) hpgManifest.get("portal1Zone");

        if (!hpgManifest.has("portal2Chunk")){
            hpgManifest.put("portal2Chunk", new Vector3DataPoint(new Vector3()));
        }
        if (!hpgManifest.has("portal2IdTime")){
            hpgManifest.put("portal2IdTime", new LongDataPoint((long) -1));
        }
        if (!hpgManifest.has("portal2IdRand")){
            hpgManifest.put("portal2IdRand", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal2IdNum")){
            hpgManifest.put("portal2IdNum", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal2Zone")){
            hpgManifest.put("portal2Zone", new StringDataPoint(GameSingletons.world.defaultZoneId));
        }
        Vector3DataPoint secondaryPortalChunkPos = (Vector3DataPoint) hpgManifest.get("portal2Chunk");
        IntegerDataPoint secondaryPortalIdRand = (IntegerDataPoint) hpgManifest.get("portal2IdRand");
        LongDataPoint secondaryPortalIdTime = (LongDataPoint) hpgManifest.get("portal2IdTime");
        IntegerDataPoint secondaryPortalIdNum = (IntegerDataPoint) hpgManifest.get("portal2IdNum");
        StringDataPoint secondaryPortalZone = (StringDataPoint) hpgManifest.get("portal2Zone");

        workingPos.set(player.getPosition()).add(player.getEntity().viewPositionOffset);
        ray.set(player.getPosition(), player.getEntity().viewPositionOffset);

        // |  Fun idea,
        // |  Completely broken in practice
        // \/ Turn on and try for yourself

//        Vector3 workingView = player.getEntity().viewDirection.cpy();
//        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
//        for (Portal portal : portals){
//            if (Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3())){
//                workingPos.set(portal.getPortaledPos(workingPos));
//                workingView.set(portal.getPortaledVector(workingView));
//                break;
//            }
//        }

        RaycastOutput result = raycast(player.getZone(), workingPos, player.getEntity().viewDirection, 1000F);
        if (!isSecondPortal){
            if (result != null){
                EntityUniqueId id1 = new EntityUniqueId();
                id1.set(primaryPortalIdTime.getValue(), primaryPortalIdRand.getValue(), primaryPortalIdNum.getValue());
                EntityUniqueId id2 = new EntityUniqueId();
                id2.set(secondaryPortalIdTime.getValue(), secondaryPortalIdRand.getValue(), secondaryPortalIdNum.getValue());
                HPGPortal prPortal = fixedGetPortal(id1, primaryPortalChunkPos.getValue(), primaryPortalZone.getValue());
                HPGPortal secPortal = fixedGetPortal(id2, secondaryPortalChunkPos.getValue(), secondaryPortalZone.getValue());
                if (prPortal == null){
                    Vector3 upDir = getUpVectorForPortals(result.hitNormal(), player);
                    HPGPortal newPortal = HPGPortal.createNewPortal(new Vector2(1, 2), result.hitNormal().getVector().cpy().scl(-1), upDir, getPositionForPortals(result.hitPos(), result.hitNormal()), false, unstable, player.getZone());
                    if (newPortal == null) return;
                    primaryPortalIdTime.setValue(newPortal.uniqueId.getTime());
                    primaryPortalIdRand.setValue(newPortal.uniqueId.getRand());
                    primaryPortalIdNum.setValue(newPortal.uniqueId.getNumber());
                    primaryPortalChunkPos.getValue().set(Math.floorDiv((int) newPortal.position.x, 16), Math.floorDiv((int) newPortal.position.y, 16), Math.floorDiv((int) newPortal.position.z, 16));
                    primaryPortalZone.setValue(player.zoneId);
                    if (secondaryPortalIdTime.getValue() != -1){
                        if (secPortal == null){
                            secondaryPortalIdTime.setValue((long) -1);
                        }
                        else{
                            newPortal.linkPortal(secPortal);
                            secPortal.linkPortal(newPortal);
                            if (GameSingletons.isClient){
                                secPortal.playAnimation("rebind");
                            }
                            if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                                ServerSingletons.SERVER.broadcast(secPortal.zone, new PortalAnimationPacket(secPortal.uniqueId, "rebind"));
                            }
                        }
                    }
                    player.getZone().addEntity(newPortal);
                    Portal.portalOpenSound.playGlobalSound3D(newPortal.zone, newPortal.position);
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                        ServerSingletons.SERVER.broadcast(newPortal.zone, new PortalAnimationPacket(newPortal.uniqueId, "start"));
                    }
                }
                else{
                    Vector3 originalPos = prPortal.position.cpy();
                    Vector3 originalDir = prPortal.viewDirection.cpy();
                    Vector3 originalUpVector = prPortal.upVector.cpy();

                    prPortal.setPosition(getPositionForPortals(result.hitPos(), result.hitNormal()));
                    prPortal.viewDirection = result.hitNormal().getVector().cpy().scl(-1);
                    prPortal.upVector = getUpVectorForPortals(result.hitNormal(), player);

                    if (!prPortal.figureOutPlacement(player.getZone(), 0.5f, 0.5f, 1f, 1f)){
                        prPortal.setPosition(originalPos);
                        prPortal.viewDirection.set(originalDir);
                        prPortal.upVector.set(originalUpVector);
                        return;
                    }
                    Portal.portalCloseSound.playGlobalSound3D(prPortal.zone, originalPos);
                    Portal.portalOpenSound.playGlobalSound3D(prPortal.zone, prPortal.position);

                    if (GameSingletons.isClient){
                        if (secPortal != null){
                            secPortal.playAnimation("rebind");
                        }
                        prPortal.playAnimation("start");
                    }
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                        ServerSingletons.SERVER.broadcast(prPortal.zone, new UpdatePortalPacket(prPortal));
                        if (secPortal != null){
                            ServerSingletons.SERVER.broadcast(secPortal.zone, new PortalAnimationPacket(secPortal.uniqueId, "rebind"));
                        }
                        ServerSingletons.SERVER.broadcast(prPortal.zone, new PortalAnimationPacket(prPortal.uniqueId, "start"));
                    }
                }
            }
        }
        else {
            if (result != null){
                EntityUniqueId id1 = new EntityUniqueId();
                id1.set(primaryPortalIdTime.getValue(), primaryPortalIdRand.getValue(), primaryPortalIdNum.getValue());
                EntityUniqueId id2 = new EntityUniqueId();
                id2.set(secondaryPortalIdTime.getValue(), secondaryPortalIdRand.getValue(), secondaryPortalIdNum.getValue());
                HPGPortal secPortal = fixedGetPortal(id2, secondaryPortalChunkPos.getValue(), primaryPortalZone.getValue());
                HPGPortal prPortal = fixedGetPortal(id1, primaryPortalChunkPos.getValue(), secondaryPortalZone.getValue());

                if (secPortal == null){
                    Vector3 upDir = getUpVectorForPortals(result.hitNormal(), player);
                    HPGPortal newPortal = HPGPortal.createNewPortal(new Vector2(1, 2), result.hitNormal().getVector().cpy(), upDir, getPositionForPortals(result.hitPos(), result.hitNormal()), true, unstable, player.getZone());
                    if (newPortal == null) {
                        return;
                    }
                    secondaryPortalIdTime.setValue(newPortal.uniqueId.getTime());
                    secondaryPortalIdRand.setValue(newPortal.uniqueId.getRand());
                    secondaryPortalIdNum.setValue(newPortal.uniqueId.getNumber());
                    secondaryPortalChunkPos.getValue().set(Math.floorDiv((int) newPortal.position.x, 16), Math.floorDiv((int) newPortal.position.y, 16), Math.floorDiv((int) newPortal.position.z, 16));
                    secondaryPortalZone.setValue(player.zoneId);
                    if (primaryPortalIdTime.getValue() != -1){
                        if (prPortal == null){
                            primaryPortalIdTime.setValue((long) -1);
                        }
                        else{
                            newPortal.linkPortal(prPortal);
                            prPortal.linkPortal(newPortal);
                            if (GameSingletons.isClient){
                                prPortal.playAnimation("rebind");
                            }
                            if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                                ServerSingletons.SERVER.broadcast(prPortal.zone, new PortalAnimationPacket(prPortal.uniqueId, "rebind"));
                            }
                        }
                    }
                    player.getZone().addEntity(newPortal);
                    Portal.portalOpenSound.playGlobalSound3D(newPortal.zone, newPortal.position);
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null){
                        ServerSingletons.SERVER.broadcast(newPortal.zone, new PortalAnimationPacket(newPortal.uniqueId, "start"));
                    }
                }
                else{
                    Vector3 originalPos = secPortal.position.cpy();
                    Vector3 originalDir = secPortal.viewDirection.cpy();
                    Vector3 originalUpVector = secPortal.upVector.cpy();

                    secPortal.setPosition(getPositionForPortals(result.hitPos(), result.hitNormal()));
                    secPortal.viewDirection = result.hitNormal().getVector().cpy();
                    secPortal.upVector = getUpVectorForPortals(result.hitNormal(), player);

                    if (!secPortal.figureOutPlacement(player.getZone(), 0.5f, 0.5f, 1f, 1f)){
                        secPortal.setPosition(originalPos);
                        secPortal.viewDirection.set(originalDir);
                        secPortal.upVector.set(originalUpVector);
                        return;
                    }
                    Portal.portalCloseSound.playGlobalSound3D(secPortal.zone, originalPos);
                    Portal.portalOpenSound.playGlobalSound3D(secPortal.zone, secPortal.position);

                    if (GameSingletons.isClient){
                        if (prPortal != null){
                            prPortal.playAnimation("rebind");
                        }
                        secPortal.playAnimation("start");
                    }
                    if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
                        ServerSingletons.SERVER.broadcast(secPortal.zone, new UpdatePortalPacket(secPortal));
                        if (prPortal != null){
                            ServerSingletons.SERVER.broadcast(prPortal.zone, new PortalAnimationPacket(prPortal.uniqueId, "rebind"));
                        }
                        ServerSingletons.SERVER.broadcast(secPortal.zone, new PortalAnimationPacket(secPortal.uniqueId, "start"));
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
                newPos.y = (float) (Math.round(newPos.y * 2) / 2.0);
                newPos.z = (float) (Math.round(newPos.z * 2) / 2.0);
                return newPos;
            }
            case "posZ", "negZ" -> {
                Vector3 newPos = pos.cpy().add(normal.getVector().cpy().scl(0.05F));
                newPos.y = (float) (Math.round(newPos.y * 2) / 2.0);
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
        DataPointManifest hpgManifest = (DataPointManifest) DataPointUtil.getManifestFromStack(hpgItemStack);

        if (!hpgManifest.has("portal1Chunk")){
            hpgManifest.put("portal1Chunk", new Vector3DataPoint(new Vector3()));
        }
        if (!hpgManifest.has("portal1IdTime")){
            hpgManifest.put("portal1IdTime", new LongDataPoint((long) -1));
        }
        if (!hpgManifest.has("portal1IdRand")){
            hpgManifest.put("portal1IdRand", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal1IdNum")){
            hpgManifest.put("portal1IdNum", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal1Zone")){
            hpgManifest.put("portal1Zone", new StringDataPoint(GameSingletons.world.defaultZoneId));
        }
        Vector3DataPoint primaryPortalChunkPos = (Vector3DataPoint) hpgManifest.get("portal1Chunk");
        IntegerDataPoint primaryPortalIdRand = (IntegerDataPoint) hpgManifest.get("portal1IdRand");
        LongDataPoint primaryPortalIdTime = (LongDataPoint) hpgManifest.get("portal1IdTime");
        IntegerDataPoint primaryPortalIdNum = (IntegerDataPoint) hpgManifest.get("portal1IdNum");
        StringDataPoint primaryPortalZone = (StringDataPoint) hpgManifest.get("portal1Zone");

        if (!hpgManifest.has("portal2Chunk")){
            hpgManifest.put("portal2Chunk", new Vector3DataPoint(new Vector3()));
        }
        if (!hpgManifest.has("portal2IdTime")){
            hpgManifest.put("portal2IdTime", new LongDataPoint((long) -1));
        }
        if (!hpgManifest.has("portal2IdRand")){
            hpgManifest.put("portal2IdRand", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal2IdNum")){
            hpgManifest.put("portal2IdNum", new IntegerDataPoint(-1));
        }
        if (!hpgManifest.has("portal2Zone")){
            hpgManifest.put("portal2Zone", new StringDataPoint(GameSingletons.world.defaultZoneId));
        }
        Vector3DataPoint secondaryPortalChunkPos = (Vector3DataPoint) hpgManifest.get("portal2Chunk");
        IntegerDataPoint secondaryPortalIdRand = (IntegerDataPoint) hpgManifest.get("portal2IdRand");
        LongDataPoint secondaryPortalIdTime = (LongDataPoint) hpgManifest.get("portal2IdTime");
        IntegerDataPoint secondaryPortalIdNum = (IntegerDataPoint) hpgManifest.get("portal2IdNum");
        StringDataPoint secondaryPortalZone = (StringDataPoint) hpgManifest.get("portal2Zone");

        EntityUniqueId id1 = new EntityUniqueId();
        id1.set(primaryPortalIdTime.getValue(), primaryPortalIdRand.getValue(), primaryPortalIdNum.getValue());
        EntityUniqueId id2 = new EntityUniqueId();
        id2.set(secondaryPortalIdTime.getValue(), secondaryPortalIdRand.getValue(), secondaryPortalIdNum.getValue());
        PortalManager pm = SeamlessPortals.portalManager;

        if (primaryPortalIdTime.getValue() != -1){
            Portal primaryPortal = pm.getPortalWithGen(id1, primaryPortalChunkPos.getValue(), primaryPortalZone.getValue());
            if (primaryPortal != null){
                primaryPortal.startDestruction();
            }
        }
        if (secondaryPortalIdTime.getValue() != -1){
            Portal secondaryPortal = pm.getPortalWithGen(id2, secondaryPortalChunkPos.getValue(), secondaryPortalZone.getValue());
            if (secondaryPortal != null){
                secondaryPortal.startDestruction();
            }
        }

        primaryPortalIdTime.setValue((long) -1);
        secondaryPortalIdTime.setValue((long) -1);

        if (!GameSingletons.isClient || GameSingletons.client().getLocalPlayer() != player){
            ServerSingletons.getConnection(player).send(new ContainerSyncPacket(0, player.inventory));
        }
    }

    private static void initVectorArray(Vector3[] vector3s){
        for(int i = 0; i < vector3s.length; ++i) {
            vector3s[i] = new Vector3();
        }
    }

    public static boolean intersectOrientedBounds(OrientedBoundingBox bounds1, BoundingBox bounds2){
        // Curse you, multithreading!
        // Had to do this abomination to get it to consistently work
        Vector3[] tempAxes = new Vector3[15];
        Vector3[] tmpVectors = new Vector3[9];
        initVectorArray(tmpVectors);
        Vector3[] aAxes = new Vector3[3];
        initVectorArray(aAxes);
        aAxes[0].set(bounds1.transform.val[0], bounds1.transform.val[1], bounds1.transform.val[2]).nor();
        aAxes[1].set(bounds1.transform.val[4], bounds1.transform.val[5], bounds1.transform.val[6]).nor();
        aAxes[2].set(bounds1.transform.val[8], bounds1.transform.val[9], bounds1.transform.val[10]).nor();
        tempAxes[0] = aAxes[0];
        tempAxes[1] = aAxes[1];
        tempAxes[2] = aAxes[2];
        tempAxes[3] = Vector3.X;
        tempAxes[4] = Vector3.Y;
        tempAxes[5] = Vector3.Z;
        tempAxes[6] = tmpVectors[0].set(aAxes[0]).crs(Vector3.X);
        tempAxes[7] = tmpVectors[1].set(aAxes[0]).crs(Vector3.Y);
        tempAxes[8] = tmpVectors[2].set(aAxes[0]).crs(Vector3.Z);
        tempAxes[9] = tmpVectors[3].set(aAxes[1]).crs(Vector3.X);
        tempAxes[10] = tmpVectors[4].set(aAxes[1]).crs(Vector3.Y);
        tempAxes[11] = tmpVectors[5].set(aAxes[1]).crs(Vector3.Z);
        tempAxes[12] = tmpVectors[6].set(aAxes[2]).crs(Vector3.X);
        tempAxes[13] = tmpVectors[7].set(aAxes[2]).crs(Vector3.Y);
        tempAxes[14] = tmpVectors[8].set(aAxes[2]).crs(Vector3.Z);
        Vector3[] aVertices = bounds1.getVertices();
        Vector3[] bVertices = new Vector3[8];
        initVectorArray(bVertices);
        bounds2.getCorner000(bVertices[0]);
        bounds2.getCorner001(bVertices[1]);
        bounds2.getCorner010(bVertices[2]);
        bounds2.getCorner011(bVertices[3]);
        bounds2.getCorner100(bVertices[4]);
        bounds2.getCorner101(bVertices[5]);
        bounds2.getCorner110(bVertices[6]);
        bounds2.getCorner111(bVertices[7]);
        return Intersector.hasOverlap(tempAxes, aVertices, bVertices);
    }
}
