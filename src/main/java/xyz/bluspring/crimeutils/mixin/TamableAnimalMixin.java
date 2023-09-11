package xyz.bluspring.crimeutils.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.crimeutils.CrimeUtils;
import xyz.bluspring.crimeutils.extensions.HowlEntity;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends Animal implements HowlEntity {
    @Unique
    private int version = -1;

    protected TamableAnimalMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

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

    @Inject(method = "tame", at = @At(value = "HEAD"))
    public void cc$markAsPersistent(Player player, CallbackInfo ci) {
        this.setPersistenceRequired();
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
