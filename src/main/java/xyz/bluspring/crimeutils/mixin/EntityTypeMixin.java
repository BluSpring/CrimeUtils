package xyz.bluspring.crimeutils.mixin;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.crimeutils.CrimeUtilsConfig;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void cc$customZombieRegister(String string, EntityType.Builder<T> builder, CallbackInfoReturnable<EntityType<T>> cir) {
        if (string.equals("zombie")) {
            cir.setReturnValue(
                    (EntityType<T>) Registry.register(Registry.ENTITY_TYPE, string,
                            EntityType.Builder.of(
                                    (type, level) -> new Zombie((EntityType) type, level),
                                    CrimeUtilsConfig.getZombieCategory()
                            )
                                    .sized(0.6F, 1.95F)
                                    .clientTrackingRange(8)
                                    .build(string)
                    ));
        } else if (string.equals("chicken")) {
            cir.setReturnValue(
                    (EntityType<T>) Registry.register(Registry.ENTITY_TYPE, string,
                            EntityType.Builder.of(
                                            (type, level) -> new Chicken((EntityType) type, level),
                                            CrimeUtilsConfig.getZombieCategory()
                                    )
                                    .sized(0.4F, 0.7F)
                                    .clientTrackingRange(10)
                                    .build(string)
                    ));
        }
    }
}
