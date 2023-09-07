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

        configFile.writeText(gson.toJson(json))
    }

    var minZombieSpawns = 15
    var maxZombieSpawns = 35
    var spawnWeight = 137
    var maxZombiesPerChunk = 75
    var animalSpawnWeightMultiplier = 9.5

    @JvmStatic
    @get:JvmName("getZombieCategory")
    val ZOMBIE_CATEGORY: MobCategory

    init {
        loadConfig()

        ZOMBIE_CATEGORY = MobCategoryExtension.create("CC_ZOMBIES", "cc_zombies", maxZombiesPerChunk, false, false, 128);
    }
}