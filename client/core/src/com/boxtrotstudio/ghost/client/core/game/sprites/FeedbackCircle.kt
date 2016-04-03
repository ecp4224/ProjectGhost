package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.core.game.DynamicAnimation
import com.boxtrotstudio.ghost.client.core.game.Entity
import com.boxtrotstudio.ghost.client.utils.PFunction
import com.boxtrotstudio.ghost.client.utils.PRunnable

class FeedbackCircle : Entity("sprites/ball.png", 0) {

    override fun onLoad() {
        super.onLoad()

        setScale(0.375f)

        color = Color(72 / 255f, 170 / 255f, 45 / 255f, 1f)

        var start = 0.375f

        var entity = fromImage("sprites/ball.png")
        entity.setScale(0.325f)
        entity.color = Color.BLACK
        entity.setCenter(centerX, centerY)

        attach(entity)
        parentScene.addEntity(entity)

        val animation = DynamicAnimation(PRunnable {
            dur ->
            val temp = ease(start, 0.0001f, 350f, dur.toFloat())

            setScale(temp)
            entity.setScale(temp - 0.05f)
        })
        animation.until(PFunction {
            FeedbackCircle@scaleX == 0.0001f //it doesn't matter if we check scaleX or scaleY because it's uniform
        }).onEnded(Runnable {
            parentScene.removeEntity(entity)
            parentScene.removeEntity(this)
        }).start()
    }
}