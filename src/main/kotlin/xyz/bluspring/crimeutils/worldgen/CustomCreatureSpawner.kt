package xyz.bluspring.crimeutils.worldgen

import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.SpawnPlacements
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.NaturalSpawner
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.phys.AABB
import org.slf4j.LoggerFactory
import xyz.bluspring.crimeutils.mixin.NaturalSpawnerAccessor
import kotlin.math.pow

object CustomCreatureSpawner {
    private val logger = LoggerFactory.getLogger(CustomCreatureSpawner::class.java)
    // ticks (https://mapmaking.fr/tick/)
    private const val SPAWN_COOLDOWN = 6_000 // 5 minutes
    private const val MOB_CAP_RESCAN_COOLDOWN = 24_000 // 20 minutes
    private val MIN_DISTANCE_SQR = 64.0.pow(2.0)

    fun checkSpawningCapability(level: ServerLevel, chunk: LevelChunk) {
        // Ensure no players are too close
        val chunkWorldPos = chunk.pos.worldPosition
        if (PlayerLookup.tracking(level, chunk.pos).any {
            it.distanceToSqr(chunkWorldPos.x.toDouble(), chunkWorldPos.y.toDouble(), chunkWorldPos.z.toDouble()) <= MIN_DISTANCE_SQR
        })
            return

        val lastSpawnCom = SpawnerComponents.TIME_SINCE_LAST_SPAWN.get(chunk)
        val lastSpawn = lastSpawnCom.value

        if (level.server.tickCount - lastSpawn < SPAWN_COOLDOWN)
            return

        val mobCountCom = SpawnerComponents.CURRENT_CHUNK_CAP.get(chunk)

        val nextMobCapScanCom = SpawnerComponents.RESCAN_CHUNK_CAP_AT.get(chunk)
        val nextMobCapScan = nextMobCapScanCom.value
        val currentMobCount = if (level.server.tickCount > nextMobCapScan) {
            val entities = level.getEntities(null, AABB(chunk.pos.worldPosition, BlockPos(chunk.pos.maxBlockX, 170, chunk.pos.maxBlockZ))) {
                it.type.category == MobCategory.CREATURE
            }

            entities.size.apply {
                mobCountCom.value = this
                nextMobCapScanCom.value = level.server.tickCount + MOB_CAP_RESCAN_COOLDOWN
            }
        } else mobCountCom.value

        // mobcap lmao
        if (currentMobCount >= MobCategory.CREATURE.maxInstancesPerChunk)
            return

        spawnCreaturesInChunk(level, chunk)
    }

    fun spawnCreaturesInChunk(level: ServerLevel, chunk: LevelChunk) {
        // Recreate how MC does animal generation
        val biomeHolder = level.getBiome(chunk.pos.worldPosition.atY(level.maxBuildHeight - 1))

        val mobSettings = biomeHolder.value().mobSettings
        val spawnList = mobSettings.getMobs(MobCategory.CREATURE)

        if (spawnList.isEmpty)
            return

        val random = level.getRandom()
        val minX = chunk.pos.minBlockX
        val minZ = chunk.pos.minBlockZ
        // Not present in Vanilla, but is a fairly small micro-optimization
        // to slightly reduce on the amount of allocations done while respawning animals
        // frequently.
        val mutablePos = BlockPos.MutableBlockPos()

        while (random.nextFloat() < mobSettings.creatureProbability) {
            val spawnerDataProvider = spawnList.getRandom(random)

            if (!spawnerDataProvider.isPresent)
                continue

            val spawnerData = spawnerDataProvider.get()
            val total = spawnerData.minCount + random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount)

            var x = minX + random.nextInt(16)
            var z = minZ + random.nextInt(16)
            val initialX = x
            val initialZ = z

            for (i in 0 until total) {
                var successfulSpawn = false

                var j = 0
                while (!successfulSpawn && j < 4) {
                    ++j

                    val pos = NaturalSpawnerAccessor.callGetTopNonCollidingPos(level, spawnerData.type, x, z)
                    if (spawnerData.type.canSummon() && NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(spawnerData.type), level, pos, spawnerData.type)) {
                        val width = spawnerData.type.width
                        val posX = Mth.clamp(x.toDouble(), minX + width.toDouble(), minX + 16.0 - width)
                        val posZ = Mth.clamp(z.toDouble(), minZ + width.toDouble(), minZ + 16.0 - width)

                        if (!level.noCollision(spawnerData.type.getAABB(posX, pos.y.toDouble(), posZ)) || !SpawnPlacements.checkSpawnRules(spawnerData.type, level, MobSpawnType.CHUNK_GENERATION, mutablePos.set(posX.toInt(), pos.y, posZ.toInt()), random))
                            continue

                        // Not in Vanilla, but this is a check just to ensure animals don't spawn in places like
                        // the safe zone, or directly inside buildings.
                        val skyLight = level.getBrightness(LightLayer.SKY, mutablePos)
                        val blockLight = level.getBrightness(LightLayer.BLOCK, mutablePos)
                        if (skyLight < 14 || blockLight > 7)
                            continue

                        val entity = try {
                            spawnerData.type.create(level)
                        } catch (e: Exception) {
                            logger.warn("Failed to create mob", e)
                            continue
                        } ?: continue

                        entity.moveTo(posX, pos.y.toDouble(), posZ, random.nextFloat() * 360F, 0F)

                        if (entity is Mob && entity.checkSpawnRules(level, MobSpawnType.CHUNK_GENERATION) && entity.checkSpawnObstruction(level)) {
                            level.addFreshEntityWithPassengers(entity)
                            successfulSpawn = true
                        }
                    }

                    x += random.nextInt(5) - random.nextInt(5)
                    z += random.nextInt(5) - random.nextInt(5)

                    while (x < minX || x >= minX + 16 || z < minZ || z >= minZ + 16) {
                        x = initialX + random.nextInt(5) - random.nextInt(5)
                        z = initialZ + random.nextInt(5) - random.nextInt(5)
                    }
                }
            }
        }
    }
}