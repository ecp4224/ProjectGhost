package me.eddiep.ghost.client.handlers

import com.badlogic.gdx.Gdx
import me.eddiep.ghost.client.Ghost
import me.eddiep.ghost.client.handlers.scenes.BlurredScene
import me.eddiep.ghost.client.handlers.scenes.MenuScene
import me.eddiep.ghost.client.handlers.scenes.SpriteScene
import java.util.*

class MenuHandler : ReplayHandler(null) {
    private var ended = false
    private var nextReplay = false
    private var endedWhen = 0L
    private var allLoaded = false

    override fun start() {
        Ghost.loadGameAssets(Ghost.ASSETS) //Force this since we don't have a loading screen

        var replays = Gdx.files.local("replays")

        var files = replays.list { file, s -> s.endsWith(".mdata") }

        val random = Random()
        if (files.size > 0) {
            Path = files[random.nextInt(files.size)].path()

            world = SpriteScene()
            val blurred = BlurredScene(world, 17f) //Wrap the world in a Blurred scene to make background
            blurred.requestOrder(-1)

            Ghost.getInstance().addScene(blurred)

            loadReplay()
        }

        val menuWorld = MenuScene()
        menuWorld.requestOrder(-2)
        Ghost.getInstance().addScene(menuWorld)
    }

    override fun tick() {
        if (!Ghost.ASSETS.update()) {
            return;
        }

        world.rayHandler?.setAmbientLight(0.4f)

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
