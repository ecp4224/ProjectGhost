package me.eddiep.ghost.client.core.sprites

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.core.sprites.effects.OrbitEffect
import me.eddiep.ghost.client.utils.Constants
import java.util.*
import kotlin.properties.Delegates

open class NetworkPlayer(id: Short, texture: String) : Entity(texture, id) {
    val orbits: ArrayList<OrbitEffect> = ArrayList()
    var frozen: Boolean = false
    lateinit var body: Body;

    var lives : Byte by Delegates.observable(3.toByte()) {
        d, old, new ->
        updateLifeBalls()
    }

    var oColor : Color? = null;

    override fun tick() {
        super.tick()

        val pos = Vector3(centerX, (y + (height / 2f)), 0f)

        body.setTransform(pos.x, pos.y, Math.toRadians(rotation.toDouble()).toFloat())
    }

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

        val playerDef = BodyDef()

        val pos = Vector3(centerX, (y + (height / 2f)), 0f)

        body = Ghost.getInstance().world.createBody(playerDef)

        val wallBox = CircleShape()
        wallBox.radius = width / 2f

        body.createFixture(wallBox, 0.0f)

        wallBox.dispose()

        body.setTransform(pos.x, pos.y, Math.toRadians(rotation.toDouble()).toFloat())
    }

    fun updateLifeBalls() {
        lifeBall.forEach {
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