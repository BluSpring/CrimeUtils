package xyz.bluspring.crimeutils.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.crimeutils.CrimeUtils;
import xyz.bluspring.crimeutils.block.entity.IndestructibleSpawnerBlockEntity;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {
    @Shadow public abstract EntityType<?> getType(@Nullable CompoundTag compoundTag);

    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void useCustomSpawnerToSpawn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir, Level level, ItemStack itemStack, BlockPos blockPos, Direction direction, BlockState blockState) {
        BlockEntity blockEntity;
        if (blockState.is(CrimeUtils.INDESTRUCTIBLE_SPAWNER) && (blockEntity = level.getBlockEntity(blockPos)) instanceof IndestructibleSpawnerBlockEntity) {
            BaseSpawner baseSpawner = ((IndestructibleSpawnerBlockEntity) blockEntity).getSpawner();
            EntityType<?> entityType = this.getType(itemStack.getTag());
            baseSpawner.setEntityId(entityType);
            blockEntity.setChanged();
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            level.gameEvent(useOnContext.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            itemStack.shrink(1);
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }
}
