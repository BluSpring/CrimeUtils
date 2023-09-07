package xyz.bluspring.crimeutils.mixin;

import net.fabricmc.fabric.impl.biome.modification.BiomeModificationContextImpl;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeModificationContextImpl.class)
public interface BiomeModificationContextImplAccessor {
    @Accessor
    Biome getBiome();
}
