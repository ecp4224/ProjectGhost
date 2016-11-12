package com.boxtrotstudio.ghost.game.match.entities.map;

import com.boxtrotstudio.ghost.game.match.entities.BaseEntity;
import com.boxtrotstudio.ghost.game.match.entities.BaseTypeableEntity;
import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.game.match.world.map.WorldMap;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.game.match.world.map.ItemSpawn;
import com.boxtrotstudio.ghost.game.match.world.map.Light;

import java.awt.*;

public class MapEntityFactory {

    public static Entity createEntity(World world, WorldMap.EntityLocation info) {
        Entity entity;
        switch (info.getId()) {
            case 81: //mirror
                entity = new MirrorEntity();
                break;
            case 80: //light
                entity = new WallEntity(false);
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
            case 85:
                entity = new WallEntity(true);
                break;
            case 86:
                entity = new RadiusLightEntity();
                break;
            case 87:
                entity = new RectLightEntity();
                break;
            case 88:
                entity = new BottomlessPitEntity();
                break;
            case 89:
            case 90:
                int team = info.getId() == 89 ? 1 : 2;
                entity = new FlagEntity(team);
                break;
            case -1:
                //light
                float   x = info.getX(),
                        y = info.getY(),
                        radius = 250f,
                        intensity = 1f;

                if (info.hasExtra("radius")) {
                    radius = Float.parseFloat(info.getExtra("radius"));
                }
                if (info.hasExtra("intensity")) {
                    intensity = Float.parseFloat(info.getExtra("intensity"));
                }

                Color color = new Color(info.getColor()[0] / 255f, info.getColor()[1] / 255f, info.getColor()[2] / 255f, intensity);

                Light light = new Light(x, y, radius, intensity, color);

                if (info.hasExtra("shadows")) {
                    light.setShadows(info.getExtra("shadows").equalsIgnoreCase("true"));
                }
                if (info.hasExtra("cone")) {
                    if (info.getExtra("cone").equalsIgnoreCase("true")) {
                        float directionDegrees = 270f,
                                coneDegrees = 30f;
                        light.setRadius(350f);

                        if (info.hasExtra("directionDegrees")) {
                            directionDegrees = Float.parseFloat(info.getExtra("directionDegrees"));
                        }

                        if (info.hasExtra("coneDegrees")) {
                            coneDegrees = Float.parseFloat(info.getExtra("coneDegrees"));
                        }

                        light.turnIntoConeLight(directionDegrees, coneDegrees);
                    }
                }
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
            case -3:
                entity = new BaseTypeableEntity(-3);
                entity.requestTicks(false);
                entity.sendUpdates(false);
                entity.setName(info.getExtra("name"));
                entity.hasLighting(info.getExtra("lighting").equalsIgnoreCase("true"));
                break;
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
