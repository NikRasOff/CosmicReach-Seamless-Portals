package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalChunkBatch;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalZoneRenderer;
import finalforeach.cosmicreach.rendering.BatchedZoneRenderer;
import finalforeach.cosmicreach.rendering.ChunkBatch;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BatchedZoneRenderer.class)
public abstract class BatchedZoneRendererMixin implements IPortalZoneRenderer {
    @Final
    @Shadow private IntArray layerNums;
    @Final
    @Shadow private IntSet seenLayerNums;
    @Final
    @Shadow private IntMap<Array<ChunkBatch>> layers;
    @Final
    @Shadow private IntMap<Boolean> layerWritesToDepth;
    @Shadow public boolean drawDebugLines;

    @Shadow
    protected abstract void getChunksToRender(Zone zone, Camera worldCamera);
    @Shadow
    protected abstract void requestMeshes();
    @Shadow
    protected abstract void disposeUnusedBatches(boolean unloadAll);
    @Shadow
    protected abstract void addMeshDatasToChunkBatches();
    @Shadow
    protected abstract void drawDebugLines(Camera worldCamera);

    public void cosmicReach_Seamless_Portals$renderThroughPortal(Zone z, Camera camera){
        Gdx.gl.glEnable(2929);
        Gdx.gl.glDepthFunc(513);
        Gdx.gl.glEnable(2884);
        Gdx.gl.glCullFace(1029);
        Gdx.gl.glEnable(3042);
        Gdx.gl.glBlendFunc(770, 771);
        this.getChunksToRender(z, camera);
        this.requestMeshes();
        this.disposeUnusedBatches(false);
        this.addMeshDatasToChunkBatches();
        ChunkBatch.lastBoundShader = null;

        for(int layerNum : this.layerNums.items) {
            if (this.seenLayerNums.contains(layerNum)) {
                Array<ChunkBatch> layer = (Array)this.layers.get(layerNum);
                if (layer != null) {
                    Gdx.gl.glDepthMask((Boolean)this.layerWritesToDepth.get(layerNum, true));
                    Array.ArrayIterator var8 = layer.iterator();

                    while(var8.hasNext()) {
                        ChunkBatch batch = (ChunkBatch)var8.next();
                        ((IPortalChunkBatch)batch).cosmicReach_Seamless_Portals$renderThroughPortal(z, camera);
                    }
                }
            }
        }

        if (ChunkBatch.lastBoundShader != null) {
            ChunkBatch.lastBoundShader.unbind();
        }

        if (this.drawDebugLines) {
            this.drawDebugLines(camera);
        }

        Gdx.gl.glActiveTexture(33984);
        Gdx.gl.glBindTexture(3553, 0);
    }
}
