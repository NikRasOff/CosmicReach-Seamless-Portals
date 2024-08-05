package com.nikrasoff.seamlessportals.extras;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.PooledBlockPosition;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.ItemBlock;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.items.ItemRenderer;
import finalforeach.cosmicreach.settings.Controls;
import finalforeach.cosmicreach.ui.UI;
import finalforeach.cosmicreach.world.Chunk;
import finalforeach.cosmicreach.world.Zone;

public class ExtraPortalUtils {
    private static final Ray ray = new Ray();
    private static final Array<BlockPosition> toVisit = new Array<>();
    private static final Vector3 workingPos = new Vector3();
    private static final Queue<BlockPosition> blockQueue = new Queue<>();
    private static final Array<BlockPosition> positionsToFree = new Array<>();
    private static final BoundingBox tmpBoundingBox = new BoundingBox();
    private static final Array<BoundingBox> tmpBoundingBoxes = new Array(BoundingBox.class);
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
            Array.ArrayIterator var3 = tmpBoundingBoxes.iterator();

            BoundingBox bb;
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                bb = (BoundingBox)var3.next();
            } while(!Intersector.intersectRayBounds(ray, bb, intersection));

            return true;
        }
    }

    public static RaycastOutput raycast(Zone zone, Vector3 origin, Vector3 direction, float length){
        boolean raycastHit = false;
        BlockPosition hitBlockPos = null;
        BlockPosition lastBlockPosAtPoint = null;
        BlockPosition lastBlockPosInQueue = null;
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

                    curBlockPos = (BlockPosition)blockQueue.removeFirst();
                    blockState = curBlockPos.getBlockState();
                } while(!blockState.hasEmptyModel() && !intersectsWithBlock(blockState, curBlockPos));
                if (hitBlockPos == null && blockState.canRaycastForBreak()) {
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

        Vector3 blockPosVec = new Vector3(hitBlockPos.getGlobalX() + 0.5F, hitBlockPos.getGlobalY() + 0.5F, hitBlockPos.getGlobalZ() + 0.5F);

        return new RaycastOutput(intersection, DirectionVector.getClosestDirection(intersection.cpy().sub(blockPosVec)), hitBlockPos);
    }
}
