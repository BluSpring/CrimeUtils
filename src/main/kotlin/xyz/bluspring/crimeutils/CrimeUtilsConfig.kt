package xyz.bluspring.crimeutils

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.world.entity.MobCategory
import xyz.bluspring.enumextension.extensions.MobCategoryExtension
import java.io.File

object CrimeUtilsConfig {
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

        if (json.has("animal_spawn_weight_multiplier"))
            animalSpawnWeightMultiplier = json.get("animal_spawn_weight_multiplier").asDouble

        if (json.has("use_alternative_spawning"))
            useAlternativeSpawning = json.get("use_alternative_spawning").asBoolean

        if (json.has("howl_version"))
            currentHowlVersion = json.get("howl_version").asInt

        if (json.has("howl_health_add"))
            howlHealthAddition = json.get("howl_health_add").asDouble

        if (json.has("howl_strength_add"))
            howlStrengthAddition = json.get("howl_strength_add").asDouble

        if (json.has("howl_armor_add"))
            howlArmorAddition = json.get("howl_armor_add").asDouble

        if (json.has("howl_speed_mult"))
            howlSpeedMultiplier = json.get("howl_speed_mult").asDouble

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
        json.addProperty("animal_spawn_weight_multiplier", animalSpawnWeightMultiplier)
        json.addProperty("use_alternative_spawning", useAlternativeSpawning)

        json.addProperty("howl_version", currentHowlVersion)
        json.addProperty("howl_health_add", howlHealthAddition)
        json.addProperty("howl_strength_add", howlStrengthAddition)
        json.addProperty("howl_armor_add", howlArmorAddition)
        json.addProperty("howl_speed_mult", howlSpeedMultiplier)

        configFile.writeText(gson.toJson(json))
    }

    var minZombieSpawns = 15
    var maxZombieSpawns = 35
    var spawnWeight = 137
    var maxZombiesPerChunk = 75
    var animalSpawnWeightMultiplier = 9.5
    var useAlternativeSpawning = false

    var currentHowlVersion = 2
    var howlHealthAddition = 1024.0
    var howlStrengthAddition = 6.96
    var howlArmorAddition = 25.6
    var howlSpeedMultiplier = 1.1

    @JvmStatic
    @get:JvmName("getZombieCategory")
    val ZOMBIE_CATEGORY: MobCategory

    init {
        loadConfig()

        ZOMBIE_CATEGORY = MobCategoryExtension.create("CC_ZOMBIES", "cc_zombies", maxZombiesPerChunk, false, false, 128);
    }
}