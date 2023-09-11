package xyz.bluspring.crimeutils.worldgen

import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import net.minecraft.resources.ResourceLocation
import xyz.bluspring.crimeutils.util.IntComponent

class SpawnerComponents : ChunkComponentInitializer {
    override fun registerChunkComponentFactories(registry: ChunkComponentFactoryRegistry) {
        registry.register(TIME_SINCE_LAST_SPAWN) { IntComponent() }
        registry.register(CURRENT_CHUNK_CAP) { IntComponent() }
        registry.register(RESCAN_CHUNK_CAP_AT) { IntComponent() }
    }

    companion object {
        val TIME_SINCE_LAST_SPAWN = ComponentRegistry.getOrCreate(ResourceLocation("crimecraft", "last_spawn"), IntComponent::class.java)
        val CURRENT_CHUNK_CAP = ComponentRegistry.getOrCreate(ResourceLocation("crimecraft", "entity_count"), IntComponent::class.java)
        val RESCAN_CHUNK_CAP_AT = ComponentRegistry.getOrCreate(ResourceLocation("crimecraft", "reset_at"), IntComponent::class.java)
    }
}