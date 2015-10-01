package me.eddiep.ghost.client.core.sprites

import com.badlogic.gdx.graphics.Color
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Logical

class InputEntity(id: Short) : NetworkPlayer(id, "") {
    var fireRateStat: Double = 0.0
    var speedStat: Double = 0.0

    override fun load() {
        super.load()

        color = Color(0f, 81 / 255f, 197 / 255f, 1f)
    }

    override fun tick() {
        super.tick()

        checkMouse()
    }

    private fun checkMouse() {

    }
}
