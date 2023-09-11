package xyz.bluspring.crimeutils.util

import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.nbt.CompoundTag

class IntComponent : Component {
    var value = 0

    override fun readFromNbt(tag: CompoundTag) {
    }

    override fun writeToNbt(tag: CompoundTag) {
    }
}