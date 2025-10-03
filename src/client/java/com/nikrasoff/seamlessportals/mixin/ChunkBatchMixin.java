package com.nikrasoff.seamlessportals.mixin;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalChunkBatch;
import finalforeach.cosmicreach.rendering.ChunkBatch;
import finalforeach.cosmicreach.rendering.meshes.IGameMesh;
import finalforeach.cosmicreach.rendering.meshes.MeshData;
import finalforeach.cosmicreach.rendering.shaders.GameShader;
import finalforeach.cosmicreach.world.Zone;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkBatch.class)
public abstract class ChunkBatchMixin implements IPortalChunkBatch {
    @Shadow long seenCount;
    @Shadow
    static long seenStep;
    @Shadow boolean needToRebuild;
    @Final
    @Shadow
    static MeshData combined;
    @Shadow IGameMesh mesh;
    @Shadow
    public static GameShader lastBoundShader;
    @Shadow GameShader shader;
    @Shadow private static int uniformLocationBatchPosition;
    @Shadow BoundingBox boundingBox;
    @Shadow boolean seen;
    @Shadow Array<MeshData> meshDatasToAdd;

    @Shadow abstract void rebuildMesh(MeshData combined);

    public void cosmicReach_Seamless_Portals$renderThroughPortal(Zone zone, Camera worldCamera){
        if (this.seenCount == seenStep) {
            if (this.needToRebuild) {
                this.rebuildMesh(combined);
                this.needToRebuild = false;
            }

            if (this.mesh != null) {
                this.mesh.setAutoBind(false);
                if (lastBoundShader != this.shader) {
                    lastBoundShader = this.shader;
                    lastBoundShader.bind(worldCamera);

                    // Some math to trick the chunk shader
                    Matrix4 newViewMat = new Matrix4();
                    newViewMat.setToLookAt(Vector3.Zero, Vector3.Zero.cpy().add(worldCamera.direction), worldCamera.up);
                    Matrix4 combMat = worldCamera.projection.cpy();
                    Matrix4.mul(combMat.val, newViewMat.val);
                    lastBoundShader.shader.setUniformMatrix("u_projViewTrans", combMat);

                    lastBoundShader.bindOptionalUniform3f("cameraPosition", worldCamera.position);
                    uniformLocationBatchPosition = this.shader.getUniformLocation("u_batchPosition");
                }

                float bx = this.boundingBox.min.x - worldCamera.position.x;
                float by = this.boundingBox.min.y - worldCamera.position.y;
                float bz = this.boundingBox.min.z - worldCamera.position.z;
                this.shader.bindOptionalUniform3f(uniformLocationBatchPosition, bx, by, bz);
                this.mesh.bind(this.shader.shader);
                this.mesh.render(this.shader.shader, 4);
                this.mesh.unbind(this.shader.shader);
            }
        }

        this.seen = false;
        this.meshDatasToAdd.size = 0;
    }
}
