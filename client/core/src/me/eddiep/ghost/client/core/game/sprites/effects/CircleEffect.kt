package me.eddiep.ghost.client.core.game.sprites.effects

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.TimeUtils
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.game.Entity
import me.eddiep.ghost.client.core.logic.Logical
import me.eddiep.ghost.client.core.render.Blend
import me.eddiep.ghost.client.handlers.scenes.SpriteScene
import me.eddiep.ghost.client.utils.Global
import me.eddiep.ghost.client.utils.Vector2f

class CircleEffect : Effect {
    override fun begin(duration: Int, size: Int, x: Float, y: Float, rotation: Double, world: SpriteScene) {
        val stage1Duration = rotation.toLong()
        val stage2Duration = duration - stage1Duration

        val emittor = CircleEmittor(stage1Duration, stage2Duration, x, y, size, world)
        Ghost.getInstance().addLogical(emittor)
    }
}

class CircleEmittor(val stage1: Long, val stage2: Long, val x: Float, val y: Float, val radius: Int, val world: SpriteScene) : Logical {
    val DURATION = 100L
    val startPos = Global.RANDOM.nextInt();

    var start: Long = 0L
    var lastSpawn: Long = 0L
    var isStage2: Boolean = false
    var totalSpawn = 0
    var cursor = 0

    override fun tick() {
        if (start == 0L) {
            start = TimeUtils.millis()
        }
        if (TimeUtils.millis() - lastSpawn < DURATION)
            return

        var spawnCount: Int
        if (isStage2) {
            spawnCount = Global.RANDOM.nextInt(30 - 10) + 10
            spawnCount = Math.min(totalSpawn - cursor, spawnCount)
            cursor += spawnCount
        } else {
            spawnCount = Global.RANDOM.nextInt(9 - -1) + -1
        }

        for (i in 0..spawnCount-1) {
            if (!isStage2) {
                val p = CircleParticle(this)
                p.setCenter(x, y)
                p.setBlend(Blend(GL20.GL_SRC_ALPHA, GL20.GL_ONE));

                world.addEntity(p)
            } else {
                val xd = Global.RANDOM.nextInt()

                val tempx = x + Math.cos(xd.toDouble())*radius
                val tempy = y + Math.sin(xd.toDouble())*radius

                val p = CircleParticle(this)
                p.setCenter(tempx.toFloat(), tempy.toFloat())
                p.setBlend(Blend(GL20.GL_SRC_ALPHA, GL20.GL_ONE));

                world.addEntity(p)
            }
        }

        lastSpawn = TimeUtils.millis()

        if (!isStage2 && TimeUtils.millis() - start >= stage1) {
            isStage2 = true
            start = TimeUtils.millis()
            totalSpawn = Global.rand(300, 400)
        } else if (isStage2 && TimeUtils.millis() - start >= stage2) {
            Ghost.getInstance().removeLogical(this)
        }
    }

    override fun dispose() { }
}

class CircleParticle(val emittor: CircleEmittor): Entity("sprites/ball.png", 0) {
    val speed: Double = (2.0*Math.PI)/(Global.rand((emittor.stage1/2).toInt(), emittor.stage1.toInt())/16.0)
    var counter: Double = 0.0
    var isStage2 = emittor.isStage2
    val startPos = emittor.startPos

    var _target: Vector2f = Vector2f.ZERO
    var duration = 0f
    var start = 0L
    var sx = 0f
    var sy = 0f

    override fun onLoad() {
        super.onLoad()

        setScale(Global.RANDOM.nextFloat()*(0.3f - 0.15f)+0.15f)

        color = if (!isStage2) Color(36 / 255f, 81 / 255f, 163 / 255f, 1f) else Color(170 / 255f, 19 / 255f, 27 / 255f, 1f)
    }

    override fun tick() {
        super.tick()

        if (!isStage2 && emittor.isStage2) {
            isStage2 = true
            color = Color(170 / 255f, 19 / 255f, 27 / 255f, 1f)
        }

        if (isStage2) {
            if (_target == Vector2f.ZERO) {
                val x = centerX
                val y = centerY

                val tx = emittor.x - x
                val ty = emittor.y - y
                val angle = Math.atan2(ty.toDouble(), tx.toDouble())
                val dis = Global.rand(300, 500)

                _target = Vector2f(x + (dis * Math.cos(angle).toFloat()), y + (dis * Math.sin(angle).toFloat()))
                duration = Global.rand(50, 700).toFloat()
                start = TimeUtils.millis()
                sx = x
                sy = y
            }

            val tempx = Entity.ease(sx, _target.x, duration, (TimeUtils.millis() - start).toFloat())
            val tempy = Entity.ease(sy, _target.y, duration, (TimeUtils.millis() - start).toFloat())
            val a = Entity.ease(1f, 0f, duration / 1.5f, (TimeUtils.millis() - start).toFloat())

            setAlpha(a)
            setCenter(tempx, tempy)

            if (centerX == _target.x && centerY == _target.y) {
                parentScene.removeEntity(this)
            }
        } else {
            counter += speed

            val tempx = (emittor.x + Math.cos(startPos + counter)*emittor.radius).toFloat()
            val tempy = (emittor.y + Math.sin(startPos + counter)*emittor.radius).toFloat()

            setCenter(tempx, tempy)
        }
    }
}

