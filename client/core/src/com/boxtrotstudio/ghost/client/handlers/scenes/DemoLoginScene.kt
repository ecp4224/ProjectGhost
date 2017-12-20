package com.boxtrotstudio.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.boxtrotstudio.ghost.client.Ghost
import com.boxtrotstudio.ghost.client.core.render.Background
import com.boxtrotstudio.ghost.client.core.render.Text
import com.boxtrotstudio.ghost.client.core.render.scene.AbstractScene
import com.boxtrotstudio.ghost.client.core.render.scene.Scene
import com.boxtrotstudio.ghost.client.network.PlayerClient
import com.boxtrotstudio.ghost.client.network.packets.SessionPacket
import com.boxtrotstudio.ghost.client.utils.Constants
import com.boxtrotstudio.ghost.client.utils.PFunction
import com.boxtrotstudio.ghost.client.utils.PRunnable
import com.boxtrotstudio.ghost.client.utils.WebUtils
import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

class DemoLoginScene : AbstractScene() {
    private lateinit var header: Text;
    private lateinit var stage: Stage;
    private lateinit var username: TextField;
    private lateinit var email: TextField;
    private var textReference: Scene? = null;
    private lateinit var background: Sprite
    override fun onInit() {
        background = Sprite(Ghost.ASSETS.get("sprites/ui/select/select_background.png", Texture::class.java))
        background.setCenter(1280f / 2f, 720f / 2f)

        header = Text(62, Color.WHITE, Gdx.files.internal("fonts/7thservicebold.ttf"));
        header.x = 625f
        header.y = 520f
        header.text = "Pick\nA\n Username"
        header.load()
        header.onClick {
            System.out.println("IT WORKS!")
        }

        stage = Stage(
                Ghost.getInstance().viewport,
                Ghost.getInstance().batch
        )
        attachStage(stage)

        val skin = Skin(Gdx.files.internal("sprites/ui/uiskin.json"))

        Ghost.setStage(stage, skin)

        var table = Table()
        table.width = 800f
        table.height = 600f
        table.x = 640f - (table.width / 2f)
        table.y = 320f - (table.height / 2f)

        val shadow = Container(null)
        shadow.width = table.width
        shadow.height = table.height
        shadow.x = table.x
        shadow.y = table.y - 20f

        stage.addActor(table)
        stage.addActor(shadow)

        username = TextField("", skin)
        username.messageText = "Username"


        email = TextField("", skin)
        email.messageText = "Email (optional)"

        val loginButton = TextButton("Login", skin)

        username.setAlignment(Align.center)
        email.setAlignment(Align.center)

        table.add(username).width(130f).height(30f).padBottom(15f)
        table.row()
        table.add(email).width(130f).height(30f).padBottom(30f)
        table.row()
        table.add(loginButton).width(100f).height(35f)

        loginButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (email.text == "") {
                    Ghost.createQuestionDialog("Reserve Username?", "If you enter an email, you can reserve this username!\nWe won't do anything else with it we promise.\nDo you want to go back and enter an email?",
                            PRunnable { ok ->
                                if (!ok) {
                                    login()
                                }
                            })
                } else {
                    var emailText = email.text
                    if (!WebUtils.isValidEmail(emailText)) {
                        Ghost.createInfoDialog("Invalid Email", "You entered an invalid email :/\nPlease enter a valid email..", null)
                    } else {
                        try {
                            Files.write(Paths.get("emails.txt"), emailText.toByteArray(), StandardOpenOption.APPEND);
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        Ghost.createInfoDialog("Username Reserved", "Your email will only be used to reserve your username.\nYou will receive an email on how to activate your account after the event.", Runnable {

                            login()
                        })
                    }
                }
            }
        })

        //table.debug = true
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        background.draw(batch)
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

    private fun login() {
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
                    text.replaceWith(DemoLoginScene())
                }).start()
            }
        }).start()
    }

    private fun createOfflineSession(ip: String, username: String): String? {
        val store = BasicCookieStore()
        val client = HttpClientBuilder.create().setDefaultCookieStore(store).build()
        val rawIp = ip.split(":")[0]
        val post = HttpPost("http://$rawIp:8080/api/accounts/login")
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
                        Ghost.username = username
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
                text.replaceWith(DemoLoginScene())
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