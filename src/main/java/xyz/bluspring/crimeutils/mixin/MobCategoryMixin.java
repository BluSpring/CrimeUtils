package xyz.bluspring.crimeutils.mixin;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobCategory.class)
public abstract class MobCategoryMixin {
    @Inject(method = "getMaxInstancesPerChunk", at = @At("HEAD"), cancellable = true)
    private void cc$useCustomMaxAnimalSpawn(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this == MobCategory.CREATURE) {
            cir.setReturnValue(35);
        }
    }
}
