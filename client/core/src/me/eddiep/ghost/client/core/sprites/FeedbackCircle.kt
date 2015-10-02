package me.eddiep.ghost.client.core.sprites

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.TimeUtils
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.DynamicAnimation
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.utils.PFunction
import me.eddiep.ghost.client.utils.PRunnable

class FeedbackCircle : Entity("sprites/ball.png", 0) {

    override fun onLoad() {
        super.onLoad()

        setScale(0.375f)

        color = Color(72 / 255f, 170 / 255f, 45 / 255f, 1f)

        var start = 0.375f

        var entity = Entity.fromImage("sprites/ball.png")
        entity.setScale(0.325f)
        entity.color = Color.BLACK
        entity.setCenter(centerX, centerY)

        attach(entity)
        Ghost.getInstance().addEntity(entity)

        val animation = DynamicAnimation(PRunnable {
            dur ->
            val temp = Entity.ease(start, 0.0001f, 350f, dur.toFloat())

            setScale(temp)
            entity.setScale(temp - 0.05f)
        })
        animation.until(PFunction {
            FeedbackCircle@scaleX == 0.0001f //it doesn't matter if we check scaleX or scaleY because it's uniform
        }).onEnded(Runnable {
            Ghost.getInstance().removeEntity(entity)
            Ghost.getInstance().removeEntity(this)
        }).start()
    }
}