package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.match.world.map.ItemSpawn;
import me.eddiep.ghost.game.match.world.map.Light;
import me.eddiep.ghost.game.match.world.map.WorldMap;
import me.eddiep.ghost.utils.Vector2f;

import java.awt.*;

public class MapEntityFactory {

    public static Entity createEntity(World world, WorldMap.EntityLocation info) {
        Entity entity;
        switch (info.getId()) {
            case 81: //mirror
                entity = new MirrorEntity();
                break;
            case 80: //light
                entity = new WallEntity();
                break;
            case 82:
                entity = new OneWayMirrorEntity();
                break;
            case 83:
                entity = new RadiusSlowFieldEntity();
                break;
            case 84:
                entity = new RectSlowFieldEntity();
                break;
            case -1:
                //light
                float   x = info.getX(),
                        y = info.getY(),
                        radius = Float.parseFloat(info.getExtra("radius")),
                        intensity = Float.parseFloat(info.getExtra("intensity"));
                Color color = new Color (
                        Integer.parseInt(info.getExtra("red")),
                        Integer.parseInt(info.getExtra("green")),
                        Integer.parseInt(info.getExtra("blue"))
                );

                Light light = new Light(x, y, radius, intensity, color);
                world.spawnLight(light);
                return null;
            case -2: //item spawn
                String temp;
                if ((temp = info.getExtra("items")) != null) {
                    String[] itemList = temp.split(",");
                    int[] idList = new int[itemList.length];
                    for (int i = 0; i < itemList.length; i++) {
                        idList[i] = Integer.parseInt(itemList[i].trim());
                    }

                    ItemSpawn spawn = new ItemSpawn(info.getX(), info.getY(), idList);
                    world.addItemSpawn(spawn);
                    return null;
                } else {
                    ItemSpawn spawn = new ItemSpawn(info.getX(), info.getY());
                    world.addItemSpawn(spawn);
                    return null;
                }
            default:
                entity = null;
        }
        if (entity == null)
            return null;

        entity.setPosition(new Vector2f(info.getX(), info.getY()));
        entity.setRotation(info.getRotation());
        entity.setWidth(info.getWidth());
        entity.setHeight(info.getHeight());

        return entity;
    }
}