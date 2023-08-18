package xyz.bluspring.crimeutils.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.crimeutils.CrimeUtils;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsMixin {
    @ModifyVariable(method = "register", at = @At("HEAD"), argsOnly = true)
    private static SpawnPlacements.SpawnPredicate<?> crimecraft$useCustomZombieSpawner(SpawnPlacements.SpawnPredicate<?> original, EntityType<?> type) {
        if (type != EntityType.ZOMBIE)
            return original;

        return (SpawnPlacements.SpawnPredicate<Zombie>) CrimeUtils::checkCustomZombieSpawnRules;
    }
}
