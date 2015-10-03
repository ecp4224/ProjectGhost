package me.eddiep.ghost.client.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.google.common.io.Files
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.Handler
import me.eddiep.ghost.client.core.Entity
import me.eddiep.ghost.client.core.MatchHistory
import me.eddiep.ghost.client.core.Text
import me.eddiep.ghost.client.utils.Global
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.zip.GZIPInputStream


class ReplayHandler(public var Path: String?) : Handler {

    private var entities : HashMap<Short, Entity> = HashMap<Short, Entity>()

    lateinit var ReplayData : MatchHistory
    private var loaded : Boolean = false
    private var paused : Boolean = false
    private var cursor : Int = 0
    private var lastUpdate : Long = 0
    private val timelineSize : Int = 100 //placeholder for timeline length

    override fun start() {
        var loadingText : Text = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))
        loadingText.x = 512f
        loadingText.y = 360f
        Ghost.getInstance().addEntity(loadingText)

        Thread(Runnable {
            var data = Files.toByteArray(File(Path))
            var json : String?

            var compressed = ByteArrayInputStream(data)
            var zip = GZIPInputStream(compressed)
            var result = ByteArrayOutputStream()

            zip.copyTo(result)
            json = result.toString("ASCII")

            compressed.close()
            zip.close()
            result.close()

            ReplayData = Global.GSON.fromJson(json, MatchHistory::class.java)

            loaded = true

            Ghost.getInstance().removeEntity(loadingText)
        }).start()
    }

    override fun tick() {
       if(!loaded || (CheckKeyboard() || paused)) return

       //TODO: implement ShowUpdate

       if(cursor + 1 < timelineSize)
            cursor++
    }

    private fun CheckKeyboard() : Boolean {
        //TODO: implement Sharp2D ButtonChecker
        return false
    }



}

