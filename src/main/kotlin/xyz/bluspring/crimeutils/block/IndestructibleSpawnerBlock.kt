package xyz.bluspring.crimeutils.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SpawnerBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import xyz.bluspring.crimeutils.CrimeUtils
import xyz.bluspring.crimeutils.block.entity.IndestructibleSpawnerBlockEntity

class IndestructibleSpawnerBlock(properties: Properties) : SpawnerBlock(properties) {
    override fun newBlockEntity(blockPos: BlockPos, blockState: BlockState): BlockEntity {
        return IndestructibleSpawnerBlockEntity(blockPos, blockState)
    }

    override fun <T : BlockEntity> getTicker(
        level: Level,
        blockState: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return createTickerHelper(
            blockEntityType,
            CrimeUtils.INDESTRUCTIBLE_SPAWNER_BLOCK_ENTITY,
            if (level.isClientSide)
                BlockEntityTicker { l, blockPos, s, blockEntity -> blockEntity.clientTick(l, blockPos, s, blockEntity) }
            else
                BlockEntityTicker { l, blockPos, s, blockEntity -> blockEntity.serverTick(l, blockPos, s, blockEntity) }
        )
    }
}