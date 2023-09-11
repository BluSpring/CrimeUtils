package xyz.bluspring.crimeutils.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NaturalSpawner.class)
public interface NaturalSpawnerAccessor {
    @Invoker
    static BlockPos callGetTopNonCollidingPos(LevelReader levelReader, EntityType<?> entityType, int i, int j) {
        throw new UnsupportedOperationException();
    }
}
