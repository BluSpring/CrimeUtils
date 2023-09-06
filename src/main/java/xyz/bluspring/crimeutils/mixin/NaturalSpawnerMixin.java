package xyz.bluspring.crimeutils.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
    @Inject(method = "spawnForChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
    private static void spawnForChunk(ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnState spawnState, boolean spawnCreatures, boolean spawnMonsters, boolean creatureCooldown, CallbackInfo callback) {
        // remove 400 tick delay for spawning animals
        if (!creatureCooldown && spawnCreatures) {
            if (((SpawnStateAccessor) spawnState).callCanSpawnForCategory(MobCategory.CREATURE, levelChunk.getPos())) {
                spawnCategoryForChunk(MobCategory.CREATURE, serverLevel, levelChunk, (entityType, blockPos, chunkAccess) -> ((SpawnStateAccessor) spawnState).callCanSpawn(entityType, blockPos, chunkAccess), (mob, chunkAccess1) -> ((SpawnStateAccessor) spawnState).callAfterSpawn(mob, chunkAccess1));
            }
        }
    }

    @Shadow
    public static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {
        throw new IllegalStateException();
    }
}
