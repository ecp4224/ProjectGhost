package me.eddiep.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import me.eddiep.ghost.client.core.render.Drawable
import me.eddiep.ghost.client.core.render.Text
import me.eddiep.ghost.client.core.render.scene.AbstractScene
import java.util.*

class StatsScene(val shots: Int, val hits: Int, val hatTrick: Boolean) : AbstractScene() {
    private var toDraw: ArrayList<Drawable> = ArrayList()
    override fun onInit() {
        //1st icon = 365,450
        //2nd icon = 640,450
        //3rd icon = 895,450

        //val shotsIcon = Text(36, Color(0.674509804f, 0f, 0f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"));
        val shotsIcon = Text(72, Color.WHITE, Gdx.files.internal("fonts/fontawesome.ttf"), "\uf05b");
        shotsIcon.x = 365f
        shotsIcon.y = 720 - 450f
        shotsIcon.text = "\uf05b"
        shotsIcon.load()

        val hitsIcon = Text(72, Color(45/255f, 140f/255f, 7f/255f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"), "\uf140");
        hitsIcon.x = 640f
        hitsIcon.y = 720 - 450f
        hitsIcon.text = "\uf140"
        hitsIcon.load()

        val hatTrick = Text(72, if (this.hatTrick) Color(252f/255f, 231f/255f, 80f/255f, 1f) else Color(0.674509804f, 0f, 0f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"), "\uf005\uf00d");
        hatTrick.x = 895f
        hatTrick.y = 720 - 450f
        if (this.hatTrick)
            hatTrick.text = "\uf005"
        else
            hatTrick.text = "\uf00d"
        hatTrick.load()

        toDraw.add(shotsIcon)
        toDraw.add(hitsIcon)
        toDraw.add(hatTrick)

        val shotsText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        shotsText.x = 365f
        shotsText.y = 720 - 520f
        shotsText.text = "$shots Shots"
        shotsText.load()

        val hitsText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        hitsText.x = 640f
        hitsText.y = 720 - 520f
        hitsText.text = "$hits Hits"
        hitsText.load()

        val hatTrickText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        hatTrickText.x = 895f
        hatTrickText.y = 720 - 520f
        hatTrickText.text = if (this.hatTrick) "Hat Trick!" else "No Hat Trick"
        hatTrickText.load()

        toDraw.add(shotsText)
        toDraw.add(hitsText)
        toDraw.add(hatTrickText)
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch) {
        batch.begin()
        for (drawable in toDraw) {
            drawable.draw(batch)
        }
        batch.end()
    }

    override fun dispose() {
        toDraw.clear()
    }

}
