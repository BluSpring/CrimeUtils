package xyz.bluspring.crimeutils.block.entity

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.BaseSpawner
import net.minecraft.world.level.Level
import net.minecraft.world.level.SpawnData
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import xyz.bluspring.crimeutils.CrimeUtils

class IndestructibleSpawnerBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(CrimeUtils.INDESTRUCTIBLE_SPAWNER_BLOCK_ENTITY, pos, state) {
    private val spawner: BaseSpawner = object : BaseSpawner() {
        override fun broadcastEvent(level: Level, blockPos: BlockPos, i: Int) {
            level.blockEvent(blockPos, Blocks.SPAWNER, i, 0)
        }

        override fun setNextSpawnData(level: Level?, blockPos: BlockPos, spawnData: SpawnData) {
            super.setNextSpawnData(level, blockPos, spawnData)
            if (level != null) {
                val blockState = level.getBlockState(blockPos)
                level.sendBlockUpdated(blockPos, blockState, blockState, 4)
            }
        }
    }

    override fun load(compoundTag: CompoundTag) {
        super.load(compoundTag)
        spawner.load(level, worldPosition, compoundTag)
    }

    override fun saveAdditional(compoundTag: CompoundTag) {
        super.saveAdditional(compoundTag)
        spawner.save(compoundTag)
    }

    fun clientTick(
        level: Level,
        blockPos: BlockPos,
        blockState: BlockState,
        spawnerBlockEntity: IndestructibleSpawnerBlockEntity
    ) {
        spawnerBlockEntity.spawner.clientTick(level, blockPos)
    }

    fun serverTick(
        level: Level,
        blockPos: BlockPos,
        blockState: BlockState,
        spawnerBlockEntity: IndestructibleSpawnerBlockEntity
    ) {
        spawnerBlockEntity.spawner.serverTick(level as ServerLevel, blockPos)
    }

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(): CompoundTag {
        val compoundTag = saveWithoutMetadata()
        compoundTag.remove("SpawnPotentials")
        return compoundTag
    }

    override fun triggerEvent(i: Int, j: Int): Boolean {
        return if (spawner.onEventTriggered(level!!, i)) true else super.triggerEvent(i, j)
    }

    override fun onlyOpCanSetNbt(): Boolean {
        return true
    }

    fun getSpawner(): BaseSpawner {
        return spawner
    }
}