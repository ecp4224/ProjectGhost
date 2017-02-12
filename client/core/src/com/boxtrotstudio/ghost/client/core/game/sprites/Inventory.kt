package com.boxtrotstudio.ghost.client.core.game.sprites

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.boxtrotstudio.ghost.client.core.game.SpriteEntity
import com.boxtrotstudio.ghost.client.core.game.Item
import com.boxtrotstudio.ghost.client.core.render.Text

class Inventory : SpriteEntity("sprites/inv.png", 0) {
    val inventory: Array<ItemHolder?> = arrayOfNulls(2);

    init {
        super.lightable = true
    }

    public fun setSlot1(id: Short) {
        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)

        val item = Item.getItem(id);
        val entity = item.createEntity(-5);
        val text = Text(16, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-Regular.ttf"))
        var temp = 1024f - 852f
        text.x = (1280f - (1024f - 854f)) * widthMult
        text.y = (100f - (entity.height * 1.3f)) * heightMult
        text.text = item.name

        entity.setScale(2f)
        entity.setCenter(1280f - temp, 100f)

        parentScene.addEntity(entity)
        parentScene.addEntity(text)

        inventory[0] = ItemHolder(entity, text, item);
    }

    public fun clearSlot1() {
        clearSlot(0)
    }

    public fun setSlot2(id: Short) {
        val widthMult = (Gdx.graphics.width / 1280f)
        val heightMult = (Gdx.graphics.height / 720f)
        val item = Item.getItem(id);
        val entity = item.createEntity(-5);
        val text = Text(16, Color.WHITE, Gdx.files.internal("fonts/TitilliumWeb-Regular.ttf"))
        var temp = 1024f - 950f
        text.x = (1280 - (1024 - 947f)) * widthMult
        text.y = (100f - (entity.height * 1.3f)) * heightMult
        text.text = item.name


        entity.setScale(2f)
        entity.setCenter(1280f - temp, 100f)

        parentScene.addEntity(entity)
        parentScene.addEntity(text)

        inventory[1] = ItemHolder(entity, text, item);
    }

    public fun clearSlot2() {
        clearSlot(1)
    }

    public fun clearSlot(slot: Int) {
        if (inventory[slot] != null) {
            val entity = inventory[slot]?.entity;
            if (entity != null)
                parentScene.removeEntity(entity)

            val text = inventory[slot]?.text
            if (text != null)
                parentScene.removeEntity(text)

            inventory[slot] = null
        }
    }

    public fun hasItem(slot: Int): Boolean {
        return inventory[slot] != null
    }

    public fun getItem(slot: Int): Short {
        if (!hasItem(slot))
            return -1

        return inventory[slot]?.item?.id as Short
    }

    class ItemHolder(val entity: SpriteEntity, val text: Text, val item: Item) { }
}
