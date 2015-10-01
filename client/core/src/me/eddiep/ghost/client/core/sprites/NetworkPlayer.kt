package me.eddiep.ghost.client.core.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.utils.Constants
import kotlin.properties.Delegates

open class NetworkPlayer(id: Short, name: String) : Entity(id) {
    var lives : Int by Delegates.observable(3) {
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

    private lateinit var lifeBall: Array<Entity?>

    override fun onLoad() {
        texture = Texture(Gdx.files.internal("sprites/ball.png"))

        scale(0.75f)

        updateLifeBalls()
    }

    fun updateLifeBalls() {
        if (lifeBall != null) {
            lifeBall forEach {
                if (it != null) {
                    deattach(it)
                    Ghost.getInstance().removeEntity(it)
                }
            }
        }

        lifeBall = arrayOfNulls<Entity>(if (lives < Constants.MAX_LIVES) Constants.MAX_LIVES else lives)

        for (i in 0..lives) {
            val temp : Entity = Entity.fromImage("sprites/ball.png")
            temp.scale(0.2f)
            temp.x = x - ((width / 1.5f) / 2f)
            temp.y = y + 40f
            temp.color = Color(20 / 255f, 183 / 255f, 52 / 255f, 1f)
            temp.setAlpha(color.a)

            temp.x += (((width / 1.5f) / (Constants.MAX_LIVES - 1)) * i)

            Ghost.getInstance().addEntity(temp)

            attach(temp)
        }
    }
}