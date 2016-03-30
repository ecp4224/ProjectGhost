package me.eddiep.ghost.client.handlers.scenes

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import me.eddiep.ghost.client.core.render.Drawable
import me.eddiep.ghost.client.core.render.Text
import me.eddiep.ghost.client.core.render.scene.AbstractScene
import java.lang.management.MemoryUsage
import java.util.*

class StatsScene(val shots: Int, val hits: Int, val hatTrick: Boolean, val itemUsage: Int) : AbstractScene() {
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

        //rgb(23,104,178)
        val itemUsageIcon = Text(72, Color(23f/255f, 104f/255f, 178f/255f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"), "\uf06b");
        itemUsageIcon.x = 895f
        itemUsageIcon.y = 720 - 450f
        itemUsageIcon.text = "\uf06b"
        itemUsageIcon.load()

        if (this.hatTrick) {
            val hatTrick = Text(72, Color(252f / 255f, 231f / 255f, 80f / 255f, 1f), Gdx.files.internal("fonts/fontawesome.ttf"), "\uf005");
            hatTrick.x = 640f
            hatTrick.y = 450f
            hatTrick.text = "\uf005"
            hatTrick.load()
            toDraw.add(hatTrick)
        }


        toDraw.add(shotsIcon)
        toDraw.add(hitsIcon)
        toDraw.add(itemUsageIcon)

        val accuracy = if (shots != 0) ((hits.toDouble() / shots.toDouble()) * 100.0).toInt() else 0;

        val shotsText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        shotsText.x = 365f
        shotsText.y = 720 - 520f
        shotsText.text = "$shots Shot" + if (shots > 1) "s" else ""
        shotsText.load()

        val hitsText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        hitsText.x = 640f
        hitsText.y = 720 - 520f
        hitsText.text = "$accuracy% Accuracy"
        hitsText.load()

        val itemUsageText = Text(36, Color.WHITE, Gdx.files.internal("fonts/INFO56_0.ttf"));
        itemUsageText.x = 895f
        itemUsageText.y = 720 - 520f
        itemUsageText.text = "$itemUsage Item" + if (itemUsage > 1) "s" else "" + " Used"
        itemUsageText.load()

        toDraw.add(shotsText)
        toDraw.add(hitsText)
        toDraw.add(itemUsageText)
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
