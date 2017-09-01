package com.boxtrotstudio.ghost.client;

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.boxtrotstudio.ghost.client.core.logic.Handler
import com.boxtrotstudio.ghost.client.core.logic.LogicHandler
import com.boxtrotstudio.ghost.client.core.logic.Logical
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.Scene
import com.boxtrotstudio.ghost.client.utils.GlobalOptions
import java.util.*

class GhostClient(var handler : Handler) : ApplicationAdapter() {
    var backColor: Color = Color.BLACK
    public lateinit var batch : SpriteBatch; //We need to delay this
    private var loaded : Boolean = false;
    lateinit var camera : OrthographicCamera; //We need to delay this
    lateinit var viewport: Viewport; //We need to delay this
    private val logicalHandler = LogicHandler()
    private val scenes = ArrayList<Scene>()
    private val bodies = ArrayList<Body>()
    public lateinit var world : World;
    private lateinit  var fpsText: Text
    private lateinit var pingText: Text
    private lateinit var debugRenderer: Box2DDebugRenderer

    override fun create() {
        batch = SpriteBatch()
        camera = OrthographicCamera(1280f, 720f)
        camera.setToOrtho(false, 1280f, 720f)

        viewport = ScalingViewport(Scaling.stretch, 1280f, 720f, camera)

        debugRenderer= Box2DDebugRenderer();

        world = World(Vector2(0f, 0f), true)
        logicalHandler.init()

        handler.start()

        val widthMult = Gdx.graphics.width / 1280f
        val heightMult = Gdx.graphics.height / 720f

        fpsText = Text(16, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))
        fpsText.x = 40f * widthMult
        fpsText.y = 20f * heightMult
        fpsText.text = "FPS: 0"
        fpsText.load()

        pingText = Text(16, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"))
        pingText.x = 40f * widthMult
        pingText.y = (20f + 16) * heightMult
        pingText.text = "Ping: 0"
        pingText.load()
    }


    public fun createBody(bodyDef: BodyDef) : Body {
        val body = world.createBody(bodyDef)
        bodies.add(body)
        return body
    }

    public fun clearBodies() {
        bodies.forEach {
            world.destroyBody(it)
        }
        bodies.clear()
    }

    override fun render() {
        try {
            _render()
            //debugRenderer.render(world, camera.combined)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    fun _render() {
        Gdx.gl.glClearColor(backColor.r, backColor.g, backColor.b, backColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        logicalHandler.tick(handler, world)

        camera.update()

        batch.projectionMatrix = camera.combined;


        for (scene in scenes) {
            if (scene.isVisible) {
                scene.render(camera, batch)
                batch.color = Color.WHITE //reset color
            }
        }

        renderText()
    }

    fun renderText() {
        val showFPS = GlobalOptions.getOptions().displayFPS()
        val showPing = GlobalOptions.getOptions().displayPing()

        if (!showFPS && !showPing)
            return

        batch.begin()


        if (showFPS) {
            fpsText.text = "FPS: " + Gdx.graphics.framesPerSecond
            fpsText.draw(batch)
        }

        if (showPing) {
            pingText.text = "Ping: " + Ghost.latency + "ms"
            pingText.draw(batch)
        }

        batch.end()
    }

    public fun addScene(scene: Scene) {
        if (!scenes.contains(scene)) {
            Gdx.app.postRunnable {
                scene.init()
                scenes.add(scene)

                Collections.sort(scenes, { o1, o2 -> o2.requestedOrder() - o1.requestedOrder() })
            }
        }
    }

    public fun removeScene(scene: Scene) {
        if (scenes.contains(scene)) {
            scene.dispose()
            Gdx.app.postRunnable {
                scenes.remove(scene)
            }
        }
    }

    public fun addLogical(logic: Logical) {
        logicalHandler.addLogical(logic)
    }

    public fun removeLogical(logic: Logical) {
        logicalHandler.removeLogical(logic)
    }

    fun clearScreen() {
        Gdx.app.postRunnable {
            for (scene in scenes) {
                removeScene(scene)
            }
            logicalHandler.clear()
        }
    }

    override fun dispose() {
        world.dispose()

        for (scene in scenes) {
            removeScene(scene)
        }
    }
}
