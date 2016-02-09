package me.eddiep.ghost.client.core.game

import com.badlogic.gdx.utils.TimeUtils
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.core.logic.Logical
import me.eddiep.ghost.client.utils.PFunction
import me.eddiep.ghost.client.utils.PRunnable

class DynamicAnimation(var action: PRunnable<Long>) : Logical {
    private var _startTime : Long = 0L
    private lateinit var _stopFun : PFunction<Void, Boolean>
    private var _ended : Runnable? = null
    private var disposed: Boolean = false

    public val hasEnded : Boolean
        get() = disposed

    public val elaspe: Long
        get() = TimeUtils.millis() - _startTime

    constructor(action: PRunnable<Long>, duration: Long) :this(action) {
        _startTime = TimeUtils.millis()
        _stopFun = PFunction {
            v ->
            TimeUtils.millis() - _startTime >= duration
        }
    }

    constructor(action: PRunnable<Long>, stop: PFunction<Void, Boolean>) :this(action) {
        _stopFun = stop
    }

    fun until(stop: PFunction<Void, Boolean>) : DynamicAnimation {
        _stopFun = stop
        return this
    }

    fun onEnded(end: Runnable) : DynamicAnimation {
        _ended = end
        return this
    }

    fun start() : DynamicAnimation {
        _startTime = TimeUtils.millis()
        Ghost.getInstance().addLogical(this)
        return this
    }

    fun end() : DynamicAnimation {
        Ghost.getInstance().removeLogical(this)
        disposed = true

        _ended?.run()

        return this
    }

    override fun tick() {
        if (hasEnded)
            return

        action.run(elaspe)

        if (_stopFun.run(null))
            end()
    }

    override fun dispose() { }
}
