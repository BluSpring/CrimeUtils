package xyz.bluspring.crimeutils

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fonnymunkey.simplehats.common.init.ModRegistry
import fonnymunkey.simplehats.common.item.HatItem
import fonnymunkey.simplehats.util.HatEntry
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.Wolf
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Rarity
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.entity.EntityTypeTest
import org.slf4j.LoggerFactory
import xyz.bluspring.crimeutils.block.IndestructibleSpawnerBlock
import xyz.bluspring.crimeutils.block.entity.IndestructibleSpawnerBlockEntity
import java.io.File
import java.util.*

class CrimeUtils : ModInitializer {
    private val logger = LoggerFactory.getLogger("CrimeUtils")
    val HOWL_HEALTH_UUID = UUID.fromString("db6c76a4-3c25-4d85-afa2-4cef30539772")
    val HOWL_STRENGTH_UUID = UUID.fromString("3ee527dc-e43a-4ee8-beef-8e2611c429b2")
    val HOWL_TOUGHNESS_UUID = UUID.fromString("06e9e214-8906-402d-ac25-0f647f293d90")

    var minZombieSpawns = 15
    var maxZombieSpawns = 35
    var spawnWeight = 137

    val gson = GsonBuilder().setPrettyPrinting().create()
    val configFile = File(FabricLoader.getInstance().configDir.toFile(), "crimeutils.json")

    fun loadConfig() {
        if (!configFile.exists()) {
            saveConfig()
            return
        }

        val json = JsonParser.parseString(configFile.readText()).asJsonObject

        if (json.has("min_zombie_spawns"))
            minZombieSpawns = json.get("min_zombie_spawns").asInt

        if (json.has("max_zombie_spawns"))
            maxZombieSpawns = json.get("max_zombie_spawns").asInt

        if (json.has("spawn_weight"))
            spawnWeight = json.get("spawn_weight").asInt
    }

    fun saveConfig() {
        if (!configFile.exists())
            configFile.createNewFile()

        val json = JsonObject()
        json.addProperty("min_zombie_spawns", minZombieSpawns)
        json.addProperty("max_zombie_spawns", maxZombieSpawns)
        json.addProperty("spawn_weight", spawnWeight)

        configFile.writeText(gson.toJson(json))
    }

    override fun onInitialize() {
        loadConfig()

        BiomeModifications.addSpawn({
            it.biome.mobSettings.getMobs(MobCategory.MONSTER).unwrap().any { a -> a.type == EntityType.ZOMBIE }
        }, MobCategory.MONSTER, EntityType.ZOMBIE, spawnWeight, minZombieSpawns, maxZombieSpawns)

        ServerTickEvents.END_WORLD_TICK.register { level ->
            for (entity in level.getEntities(EntityTypeTest.forClass(Wolf::class.java)) {
                it.hasCustomName() && it.customName?.string == HOWL_NAME && !it.attributes.hasModifier(
                    Attributes.MAX_HEALTH,
                    HOWL_HEALTH_UUID
                )
            }) {
                applyHowlHealth(entity)
            }
        }

        EntityTrackingEvents.START_TRACKING.register { entity, player ->
            if (entity !is Wolf)
                return@register

            if (!entity.hasCustomName())
                return@register

            if (entity.customName?.string != HOWL_NAME)
                return@register

            if (!entity.attributes.hasModifier(Attributes.MAX_HEALTH, HOWL_HEALTH_UUID)) {
                applyHowlHealth(entity)
            }
        }

        ModRegistry.hatList.add(BEAF_HAT)
    }

    fun applyHowlHealth(entity: Wolf) {
        logger.info("Detected a Howl dog without any health modifiers, applying health modifier.")

        entity.getAttribute(Attributes.MAX_HEALTH)?.addPermanentModifier(
            AttributeModifier(HOWL_HEALTH_UUID,
                "HowlHealthModifier", HOWL_HEALTH, AttributeModifier.Operation.ADDITION
            )
        )

        entity.getAttribute(Attributes.ATTACK_DAMAGE)?.addPermanentModifier(
            AttributeModifier(
                HOWL_STRENGTH_UUID,
                "HowlStrengthModifier", HOWL_DAMAGE, AttributeModifier.Operation.ADDITION
            )
        )

        entity.getAttribute(Attributes.ARMOR)?.addPermanentModifier(
            AttributeModifier(
                HOWL_TOUGHNESS_UUID,
                "HowlToughnessModifier", HOWL_ARMOR, AttributeModifier.Operation.ADDITION
            )
        )

        entity.health = entity.maxHealth
    }

    companion object {
        const val MOD_ID = "crimecraft"
        const val HOWL_NAME = "\uE43F7 Howl"

        const val HOWL_HEALTH = 1024.0
        const val HOWL_DAMAGE = 4.56
        const val HOWL_ARMOR = 25.6

        @JvmField
        val INDESTRUCTIBLE_SPAWNER = Registry.register(Registry.BLOCK,
            ResourceLocation(MOD_ID, "indestructible_spawner"),
            IndestructibleSpawnerBlock(
                BlockBehaviour.Properties.copy(Blocks.SPAWNER)
                    .strength(-1.0f, 3600000.0f)
            )
        )

        @JvmField
        val INDESTRUCTIBLE_SPAWNER_ITEM = Registry.register(Registry.ITEM,
            ResourceLocation(MOD_ID, "indestructible_spawner"),
            BlockItem(INDESTRUCTIBLE_SPAWNER, FabricItemSettings().stacksTo(64).tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC))
        )

        @JvmField
        val INDESTRUCTIBLE_SPAWNER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
            ResourceLocation(MOD_ID, "indestructible_spawner"),
            FabricBlockEntityTypeBuilder.create(::IndestructibleSpawnerBlockEntity, INDESTRUCTIBLE_SPAWNER).build()
        )

        @JvmField
        val BEAF_HAT_ENTRY = HatEntry("beaf_hat")

        @JvmField
        val BEAF_HAT = Registry.register(Registry.ITEM,
            ResourceLocation(MOD_ID, "beaf_hat"),
            HatItem(BEAF_HAT_ENTRY)
        )

        fun isDarkEnoughToSpawn(
            world: ServerLevelAccessor,
            blockPos: BlockPos,
            randomSource: RandomSource
        ): Boolean {
            return world.getBrightness(LightLayer.BLOCK, blockPos) <= randomSource.nextInt(4)
        }

        @JvmStatic
        fun checkCustomZombieSpawnRules(entityType: EntityType<out Mob>, serverLevelAccessor: ServerLevelAccessor, mobSpawnType: MobSpawnType, blockPos: BlockPos, randomSource: RandomSource): Boolean {
            return serverLevelAccessor.difficulty != Difficulty.PEACEFUL && isDarkEnoughToSpawn(
                serverLevelAccessor,
                blockPos,
                randomSource
            ) && Mob.checkMobSpawnRules(entityType, serverLevelAccessor, mobSpawnType, blockPos, randomSource);
        }
    }
}