package xyz.bluspring.crimeutils.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers
import xyz.bluspring.crimeutils.CrimeUtils
import xyz.bluspring.crimeutils.client.renderer.IndestructibleSpawnerRenderer

class CrimeUtilsClient : ClientModInitializer {
    override fun onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), CrimeUtils.INDESTRUCTIBLE_SPAWNER)
        BlockEntityRenderers.register(CrimeUtils.INDESTRUCTIBLE_SPAWNER_BLOCK_ENTITY, ::IndestructibleSpawnerRenderer)
    }
}