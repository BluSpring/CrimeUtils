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
import xyz.bluspring.crimeutils.CrimeUtilsConfig;
import xyz.bluspring.crimeutils.worldgen.CustomCreatureSpawner;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
    @Inject(method = "spawnForChunk", at = @At("HEAD"))
    private static void cc$useAlternativeSpawningForAnimals(ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnState spawnState, boolean bl, boolean bl2, boolean bl3, CallbackInfo ci) {
        if (CrimeUtilsConfig.INSTANCE.getUseAlternativeSpawning()) {
            CustomCreatureSpawner.INSTANCE.checkSpawningCapability(serverLevel, levelChunk);
        }
    }

    @Shadow
    public static void spawnCategoryForChunk(MobCategory mobCategory, ServerLevel serverLevel, LevelChunk levelChunk, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {
        throw new IllegalStateException();
    }
}
