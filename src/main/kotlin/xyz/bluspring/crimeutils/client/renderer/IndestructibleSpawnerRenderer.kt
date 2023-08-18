package xyz.bluspring.crimeutils.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.util.Mth
import xyz.bluspring.crimeutils.block.entity.IndestructibleSpawnerBlockEntity

class IndestructibleSpawnerRenderer(context: BlockEntityRendererProvider.Context) : BlockEntityRenderer<IndestructibleSpawnerBlockEntity> {
    private val entityRenderer: EntityRenderDispatcher? = context.entityRenderer

    override fun render(
        spawnerBlockEntity: IndestructibleSpawnerBlockEntity,
        f: Float,
        poseStack: PoseStack,
        multiBufferSource: MultiBufferSource,
        i: Int,
        j: Int
    ) {
        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)
        val baseSpawner = spawnerBlockEntity.getSpawner()
        val entity = baseSpawner.getOrCreateDisplayEntity(spawnerBlockEntity.level!!)
        if (entity != null) {
            var g = 0.53125f
            val h = Math.max(entity.bbWidth, entity.bbHeight)
            if (h.toDouble() > 1.0) {
                g /= h
            }
            poseStack.translate(0.0, 0.4000000059604645, 0.0)
            poseStack.mulPose(
                Vector3f.YP.rotationDegrees(
                    Mth.lerp(
                        f.toDouble(),
                        baseSpawner.getoSpin(),
                        baseSpawner.spin
                    ).toFloat() * 10.0f
                )
            )
            poseStack.translate(0.0, -0.20000000298023224, 0.0)
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-30.0f))
            poseStack.scale(g, g, g)
            entityRenderer!!.render(entity, 0.0, 0.0, 0.0, 0.0f, f, poseStack, multiBufferSource, i)
        }
        poseStack.popPose()
    }
}