package xyz.bluspring.crimeutils.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.crimeutils.CrimeUtils;
import xyz.bluspring.crimeutils.extensions.HowlEntity;

@Mixin(TamableAnimal.class)
public class TamableAnimalMixin implements HowlEntity {
    @Unique
    private int version = -1;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void appendHowlSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        if (CrimeUtils.isHowl((TamableAnimal) (Object) this)) {
            compoundTag.putInt("CCHowlVersion", CrimeUtils.HOWL_VERSION);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void loadHowlSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        if (CrimeUtils.isHowl((TamableAnimal) (Object) this)) {
            version = compoundTag.getInt("CCHowlVersion");
        }
    }

    @Override
    public int getCcVersion() {
        return version;
    }

    @Override
    public void ccUpdateVersion() {
        version = CrimeUtils.HOWL_VERSION;
    }
}
