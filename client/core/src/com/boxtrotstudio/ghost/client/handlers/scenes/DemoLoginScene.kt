package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.core.render.scene.Scene
import com.boxtrotstudio.ghost.client.network.PlayerClient
import com.boxtrotstudio.ghost.client.network.packets.SessionPacket
import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*

class DemoLoginScene : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var stage: Stage;
    private lateinit var username: TextField;
    private var textReference: Scene? = null;
    override fun onInit() {
        header = Text(72, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-SemiBold.ttf"));
        header.x = 640f
        header.y = 520f
        header.text = "Choose a Username"
        header.load()
        header.onClick {
            System.out.println("IT WORKS!")
        }

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        var table = Table()
        table.width = 200f
        table.height = 300f
        table.x = 640f - (table.width / 2f)
        table.y = 300f - (table.height / 2f)
        stage.addActor(table)

        username = TextField("", skin)
        username.messageText = "Username"

        val loginButton = TextButton("Login", skin)

        username.setAlignment(Align.center)

        table.add(username).width(130f).height(30f).padBottom(30f)
        table.row()
        table.add(loginButton).width(100f).height(35f)

        loginButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Thread(Runnable {
                    val text = TextOverlayScene("Please wait", "Creating Session..", true)
                    textReference = text
                    Thread.sleep(100)
                    Gdx.app.postRunnable {
                        text.requestOrder(-2)
                        Ghost.getInstance().addScene(text)
                        isVisible = false
                    }
                    Ghost.Session = createOfflineSession(Ghost.getIp(), username.text)
                    if (Ghost.Session != null)
                        connectWithSession(Ghost.Session, text)
                    else {
                        text.setHeaderText("Failed to connect!");
                        text.setSubText("Could not create session..")
                        Thread(Runnable {
                            Thread.sleep(3000)
                            text.replaceWith(this@DemoLoginScene)
                        }).start()
                    }
                }).start()
            }
        })

        //table.debug = true
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        header.draw(batch)
        batch.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
        if (textReference != null) {
            Ghost.getInstance().removeScene(textReference as Scene)
        }
    }

    private fun createOfflineSession(ip: String, username: String): String? {
        val store = BasicCookieStore()
        val client = HttpClientBuilder.create().setDefaultCookieStore(store).build()

        val post = HttpPost("http://$ip:8080/api/accounts/login")
        val parms = ArrayList<NameValuePair>()
        parms.add(BasicNameValuePair("username", username))
        parms.add(BasicNameValuePair("password", "offline"))
        var session: String? = null
        try {
            val entity = UrlEncodedFormEntity(parms)
            post.entity = entity

            val response = client.execute(post)
            if (response.statusLine.statusCode == 202) {
                for (cookie in store.cookies) {
                    if (cookie.name == "session") {
                        session = cookie.value
                        break
                    }
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: ClientProtocolException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return session
    }

    private fun connectWithSession(session: String, text: TextOverlayScene) {
        Ghost.matchmakingClient = PlayerClient.connect(Ghost.getIp())
        if (!Ghost.matchmakingClient.isConnected) {
            text.setHeaderText("Failed to connect!");
            text.setSubText("Could not connect to server..")
            Thread(Runnable {
                Thread.sleep(3000)
                text.replaceWith(this)
            }).start()
            return
        }

        val packet = SessionPacket()
        packet.writePacket(Ghost.matchmakingClient, session);
        if (!Ghost.matchmakingClient.ok()) {
            text.setHeaderText("Failed to connect!");
            text.setSubText("Could not connect to server..")
            throw IOException("Bad session!");
        }
        Ghost.matchmakingClient.isValidated = true

        Thread(Runnable {
            Thread.sleep(5000)
            Gdx.app.postRunnable {

                val menu = MenuScene()
                menu.requestOrder(-2)
                text.replaceWith(menu)
            }
        }).start()
    }
}