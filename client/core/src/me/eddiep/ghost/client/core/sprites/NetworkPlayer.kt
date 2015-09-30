package me.eddiep.ghost.client.core.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import me.eddiep.ghost.client.core.Entity

class NetworkPlayer(id: Short, name: String) : Entity(id) {

    lateinit var lifeBall : Array<Entity>;

    override fun onLoad() {
        texture = Texture(Gdx.files.internal("sprites/ball.png"))

        scale(0.75f)
    }

    fun updateLifeBalls() {
        if (lifeBall != null) {
            lifeBall forEach {

            }
        }
    }
}