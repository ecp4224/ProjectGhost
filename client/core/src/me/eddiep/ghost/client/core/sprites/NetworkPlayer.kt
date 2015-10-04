package me.eddiep.ghost.client.core.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.utils.Constants
import kotlin.properties.Delegates

open class NetworkPlayer(id: Short, name: String) : Entity("sprites/ball.png", id) {
    var frozen: Boolean = false

    var lives : Byte by Delegates.observable(3.toByte()) {
        d, old, new ->
        updateLifeBalls()
    }

    var oColor : Color? = null;

    var dead : Boolean by Delegates.observable(false) {
        d, old, new ->
        if (!old && new) {
            oColor = color;
            color = Color(234 / 255f, 234 / 255f, 32 / 255f, 1f)
        } else if (old && !new) {
            color = oColor;
        }
    }

    private var lifeBall: Array<Entity?> = arrayOfNulls(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives.toInt())

    override fun onLoad() {
        super.onLoad()

        setScale(0.75f)

        updateLifeBalls()
    }

    fun updateLifeBalls() {
        lifeBall forEach {
            if (it != null) {
                deattach(it)
                Ghost.getInstance().removeEntity(it)
            }
        }

        lifeBall = arrayOfNulls<Entity>(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives.toInt())

        for (i in 0..lives-1) {
            val temp: Entity = Entity.fromImage("sprites/ball.png")

            var newX = centerX - ((width / 1.5f) / 2f)
            newX += (((width / 1.5f) / (Constants.MAX_LIVES - 1)) * i)

            temp.setScale(0.2f)
            temp.setCenter(newX, centerY - 40f)
            temp.color = Color(20 / 255f, 183 / 255f, 52 / 255f, 1f)
            temp.setAlpha(color.a)

            Ghost.getInstance().addEntity(temp)

            attach(temp)

            lifeBall[i] = temp
        }
    }
}