package xyz.bluspring.crimeutils.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public class MonsterMixin {
    @Inject(method = "getWalkTargetValue", at = @At("RETURN"), cancellable = true)
    public void doNotAffectByDay(BlockPos blockPos, LevelReader levelReader, CallbackInfoReturnable<Float> cir) {
        if (((Monster) (Object) this) instanceof Zombie) {
            cir.setReturnValue(1f);
        }
    }
}
