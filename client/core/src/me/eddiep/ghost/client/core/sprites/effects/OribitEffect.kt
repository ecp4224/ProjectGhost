package me.eddiep.ghost.client.core.sprites.effects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.TimeUtils
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.core.Logical
import me.eddiep.ghost.client.core.sprites.NetworkPlayer
import me.eddiep.ghost.client.utils.Global
import me.eddiep.ghost.client.utils.Vector2f

class OrbitEffect(val owner: NetworkPlayer) : Logical {
    val SPEED = (2.0*Math.PI)/65.0

    var startPos: Double = 0.0
    var count = 0.0

    public fun begin() {
        startPos = Global.RANDOM.nextDouble()*(2.0*Math.PI)
        Ghost.getInstance().addLogical(this)

        owner.orbits.add(this)
    }

    public fun end() {
        Ghost.getInstance().removeLogical(this)
        owner.orbits.remove(this)
    }

    override fun tick() {
        count += SPEED

        if (Math.abs(owner.color.a) < 0.05f)
            return; //Don't spawn particles when you can't see them!

        var x = Math.cos(count + startPos) * (owner.width / 2.0)
        var y = Math.sin(count + startPos) * (owner.height / 2.0)

        x += owner.centerX
        y += owner.centerY

        val spawnCount = Global.RANDOM.nextInt(8)

        for (i in 0..spawnCount-1) {
            val sprite = OrbitSprite(count + startPos - 1.57079633, owner.color.a)
            sprite.setCenter(x.toFloat(), y.toFloat())

            Ghost.getInstance().addEntity(sprite)

            owner.attach(sprite)
        }
    }

    override fun dispose() { }
}

class OrbitSprite(val baseDirection: Double, val alpha: Float) : Entity("sprites/ball.png", 0) {
    val direction = baseDirection + (Global.RANDOM.nextDouble()*0.34906585)
    val speed = Global.RANDOM.nextDouble()
    val duration = Global.rand(300, 1100).toFloat()
    val startAlpha = alpha
    var start = 0L

    override fun onLoad() {
        super.onLoad()

        velocity = Vector2f((Math.cos(direction)*speed).toFloat(), (Math.sin(direction)*speed).toFloat())

        setScale(Global.RANDOM.nextFloat()*(0.13f-0.05f)+0.05f)
        color = Color(222/255f, 248/255f, 9/255f, alpha)
    }

    override fun tick() {
        super.tick()

        if (start == 0L)
            start = TimeUtils.millis()

        val tempa = Entity.ease(startAlpha, 0f, duration, (TimeUtils.millis() - start).toFloat())
        setAlpha(tempa)

        if (tempa == 0f)
            Ghost.getInstance().removeEntity(this)
    }
}

