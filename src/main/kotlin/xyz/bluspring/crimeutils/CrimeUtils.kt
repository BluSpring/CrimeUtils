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
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.*
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
import xyz.bluspring.crimeutils.extensions.HowlEntity
import xyz.bluspring.enumextension.extensions.MobCategoryExtension
import java.io.File
import java.util.*

class CrimeUtils : ModInitializer {
    private val logger = LoggerFactory.getLogger("CrimeUtils")
    val HOWL_HEALTH_UUID = UUID.fromString("db6c76a4-3c25-4d85-afa2-4cef30539772")
    val HOWL_STRENGTH_UUID = UUID.fromString("3ee527dc-e43a-4ee8-beef-8e2611c429b2")
    val HOWL_TOUGHNESS_UUID = UUID.fromString("06e9e214-8906-402d-ac25-0f647f293d90")
    val HOWL_SPEED_UUID = UUID.fromString("e5f79ef7-2f03-42fa-bacc-5577f7afb3ad")

    val HOWL_HEALTH_MODIFIER = AttributeModifier(HOWL_HEALTH_UUID,
        "HowlHealthModifier", HOWL_HEALTH, AttributeModifier.Operation.ADDITION
    )
    val HOWL_STRENGTH_MODIFIER = AttributeModifier(
        HOWL_STRENGTH_UUID,
        "HowlStrengthModifier", HOWL_DAMAGE, AttributeModifier.Operation.ADDITION
    )
    val HOWL_TOUGHNESS_MODIFIER = AttributeModifier(
        HOWL_TOUGHNESS_UUID,
        "HowlToughnessModifier", HOWL_ARMOR, AttributeModifier.Operation.ADDITION
    )

    val trackedZombieIds = mutableListOf<UUID>()

    override fun onInitialize() {
        BiomeModifications.addSpawn({
            it.biome.mobSettings.getMobs(MobCategory.MONSTER).unwrap().any { a -> a.type == EntityType.ZOMBIE }
        }, ZOMBIE_CATEGORY, EntityType.ZOMBIE, spawnWeight, minZombieSpawns, maxZombieSpawns)

        ServerTickEvents.END_WORLD_TICK.register { level ->
            for (entity in level.getEntities(EntityTypeTest.forClass(Wolf::class.java)) {
                isHowl(it)
            }) {
                if (!isHowlMatchingVersion(entity))
                    applyHowlHealth(entity)

                applyHowlEffects(entity)
            }
        }

        EntityTrackingEvents.START_TRACKING.register { entity, player ->
            /*if (entity is Zombie) {
                if (!trackedZombieIds.contains(entity.uuid))
                    trackedZombieIds.add(entity.uuid)
            }*/

            if (entity !is Wolf)
                return@register

            // i screwed up... oops.
            // let's fix that.
            if (!isHowl(entity) && entity is HowlEntity && (entity.ccVersion != -1 || entity.attributes.hasModifier(Attributes.MAX_HEALTH, HOWL_HEALTH_UUID))) {
                logger.info("Blu fucked up, there accidentally exists a broken Howl. We're rectifying that.")

                entity.getAttribute(Attributes.MAX_HEALTH)?.removeModifier(HOWL_HEALTH_UUID)
                entity.getAttribute(Attributes.ATTACK_DAMAGE)?.removeModifier(HOWL_STRENGTH_UUID)
                entity.getAttribute(Attributes.ARMOR)?.removeModifier(HOWL_TOUGHNESS_UUID)
                entity.removeAllEffects()

                return@register
            }

            if (isHowl(entity) && !isHowlMatchingVersion(entity))
                applyHowlHealth(entity)
        }

        /*ServerLivingEntityEvents.AFTER_DEATH.register { entity, _ ->
            if (entity is Zombie) {
                trackedZombieIds.remove(entity.uuid)
            }
        }*/

        ModRegistry.hatList.add(BEAF_HAT)
    }

    fun applyHowlHealth(entity: Wolf) {
        logger.info("Detected a Howl dog without any health modifiers, applying health modifier.")

        entity.getAttribute(Attributes.MAX_HEALTH)?.removeModifier(HOWL_HEALTH_UUID)
        entity.getAttribute(Attributes.ATTACK_DAMAGE)?.removeModifier(HOWL_STRENGTH_UUID)
        entity.getAttribute(Attributes.ARMOR)?.removeModifier(HOWL_TOUGHNESS_UUID)

        entity.getAttribute(Attributes.MAX_HEALTH)?.addPermanentModifier(HOWL_HEALTH_MODIFIER)
        entity.getAttribute(Attributes.ATTACK_DAMAGE)?.addPermanentModifier(HOWL_STRENGTH_MODIFIER)
        entity.getAttribute(Attributes.ARMOR)?.addPermanentModifier(HOWL_TOUGHNESS_MODIFIER)

        entity.health = entity.maxHealth

        if (entity is HowlEntity)
            entity.ccUpdateVersion()
    }

    fun applyHowlEffects(entity: Wolf) {
        entity.forceAddEffect(MobEffectInstance(MobEffects.REGENERATION, 1_000_000_000, 255, false, false), null)
    }

    companion object {
        const val MOD_ID = "crimecraft"
        const val HOWL_NAME = "\uE43F7 Howl"
        const val HOWL_VERSION = 1

        const val HOWL_HEALTH = 1024.0
        const val HOWL_DAMAGE = 4.56
        const val HOWL_ARMOR = 25.6

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

            if (json.has("max_zombies_per_chunk"))
                maxZombiesPerChunk = json.get("max_zombies_per_chunk").asInt

            saveConfig()
        }

        fun saveConfig() {
            if (!configFile.exists())
                configFile.createNewFile()

            val json = JsonObject()
            json.addProperty("min_zombie_spawns", minZombieSpawns)
            json.addProperty("max_zombie_spawns", maxZombieSpawns)
            json.addProperty("spawn_weight", spawnWeight)
            json.addProperty("max_zombies_per_chunk", maxZombiesPerChunk)

            configFile.writeText(gson.toJson(json))
        }

        var minZombieSpawns = 15
        var maxZombieSpawns = 35
        var spawnWeight = 137
        var maxZombiesPerChunk = 75

        @JvmStatic
        @get:JvmName("getZombieCategory")
        val ZOMBIE_CATEGORY: MobCategory

        init {
            loadConfig()

            ZOMBIE_CATEGORY = MobCategoryExtension.create("CC_ZOMBIES", "cc_zombies", maxZombiesPerChunk, false, false, 128);
        }

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
        val BEAF_HAT_ENTRY = HatEntry("beaf_hat", Rarity.COMMON, 5)

        @JvmField
        val BEAF_HAT = Registry.register(Registry.ITEM,
            ResourceLocation(MOD_ID, "beaf_hat"),
            HatItem(BEAF_HAT_ENTRY)
        )

        @JvmStatic
        fun isHowl(entity: LivingEntity): Boolean {
            return entity is Wolf && entity.hasCustomName() && entity.customName?.string == HOWL_NAME
        }

        @JvmStatic
        fun isHowlMatchingVersion(entity: LivingEntity): Boolean {
            return isHowl(entity) && entity is HowlEntity && entity.ccVersion == HOWL_VERSION
        }

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