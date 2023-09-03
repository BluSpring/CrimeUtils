package xyz.bluspring.crimeutils.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.crimeutils.CrimeUtils;

@Mixin(NameTagItem.class)
public class NameTagItemMixin {
    @Inject(method = "interactLivingEntity", at = @At("HEAD"))
    public void ignoreIfHowl(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!CrimeUtils.isHowl(livingEntity))
            return;

        cir.setReturnValue(InteractionResult.PASS);
    }
}
