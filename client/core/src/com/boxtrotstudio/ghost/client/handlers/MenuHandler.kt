package com.boxtrotstudio.ghost.client.handlers

import com.badlogic.gdx.Gdx
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.handlers.scenes.*
import com.boxtrotstudio.ghost.client.network.packets.JoinQueuePacket
import com.boxtrotstudio.ghost.client.utils.GlobalOptions
import com.boxtrotstudio.ghost.client.utils.P2Runnable
import com.boxtrotstudio.ghost.client.utils.Timer
import java.util.*

class MenuHandler : ReplayHandler(null) {
    private var ended = false
    private var nextReplay = false
    private var endedWhen = 0L
    private var allLoaded = false

    override fun start() {
        val loading = LoadingScene()
        Ghost.getInstance().addScene(loading)
        loading.setLoadedCallback(Runnable {
            var replays = Gdx.files.local("replays")

            var files = replays.list { file, s -> s.endsWith(".mdata") }

            val random = Random()
            if (files.size > 0) {
                Path = files[random.nextInt(files.size)].path()

                world = SpriteScene()
                val blurred = BlurredScene(world, 17f) //Wrap the world in a Blurred scene to make background
                blurred.requestOrder(1)

                Ghost.getInstance().addScene(blurred)

                loadReplay()
            }

            val menuWorld = if (Ghost.matchmakingClient == null) LoginScene() else MenuScene()
            menuWorld.requestOrder(-2)
            Ghost.getInstance().addScene(menuWorld)
            Ghost.getInstance().removeScene(loading)
            allLoaded = true
        })
    }

    override fun tick() {
        if (!allLoaded)
            return

        if (cursor?.isPresent == false && nextReplay) {
            world.clear()
            entities.clear()
            nextReplay = false
        }

        super.tick()



        if (cursor?.isPresent == true) {
            if (!ended) {
                ended = true
                endedWhen = System.currentTimeMillis()
            } else if (ended && System.currentTimeMillis() - endedWhen >= 5000) {
                var replays = Gdx.files.local("replays")

                var files = replays.list { file, s -> s.endsWith(".mdata") }

                val random = Random()
                if (files.size > 0) {
                    Path = files[random.nextInt(files.size)].path()
                    loadReplay()
                }
                ended = false
                nextReplay = true
            }
        }
    }


}
