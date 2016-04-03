package com.boxtrotstudio.ghost.client.core.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.boxtrotstudio.ghost.client.core.game.sprites.*;
import com.boxtrotstudio.ghost.client.core.render.Text;
import com.boxtrotstudio.ghost.client.utils.NetworkUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class EntityFactory {
    private static HashMap<Short, EntityCreator> ENTITIES = new HashMap<Short, EntityCreator>();

    static {
        ENTITIES.put((short)-3, new TextEntityCreator());
        ENTITIES.put((short)2, new ClassEntityCreator(Bullet.class));
        ENTITIES.put((short)5, new ImageEntityCreator("sprites/boomerang.png", 0.75f));
        ENTITIES.put((short)6, new ClassEntityCreator(BoomerangLine.class));
        ENTITIES.put((short)10, new ImageEntityCreator("sprites/can_of_gummy.png"));
        ENTITIES.put((short)11, new ImageEntityCreator("sprites/can_of_yummy.png"));
        ENTITIES.put((short)12, new ImageEntityCreator("sprites/can_of_scummy.png"));
        ENTITIES.put((short)13, new ImageEntityCreator("sprites/can_of_dummy.png"));
        ENTITIES.put((short)14, new ImageEntityCreator("sprites/can_of_chummy.png"));
        ENTITIES.put((short)15, new ImageEntityCreator("sprites/can_of_glummy.png"));
        ENTITIES.put((short)16, new ImageEntityCreator("sprites/party_cannon.png"));
        ENTITIES.put((short)83, new ImageEntityCreator("sprites/bottomlesspit.png"));
        ENTITIES.put((short)80, new ClassEntityCreator(Wall.class));
        ENTITIES.put((short)81, new ClassEntityCreator(Mirror.class));
        ENTITIES.put((short)82, new ClassEntityCreator(OneWayMirror.class));
    }

    public static Entity createEntity(short type, short id, float x, float y, float rotation, String name) {
        if (!ENTITIES.containsKey(type))
            return null;
        Entity e = ENTITIES.get(type).create(id, rotation, name);
        e.setCenter(x, y);
        return e;
    }

    public static Entity createEntity(short type, short id, float x, float y, float width, float height, float rotation, String name) {
        if (!ENTITIES.containsKey(type))
            return null;
        Entity e = ENTITIES.get(type).create(id, rotation, name);

        if (width != -1 && height != -1)
            e.setSize(width, height);

        e.setCenter(x, y);
        return e;
    }

    public static Entity createEntity(short type, short id) {
        if (!ENTITIES.containsKey(type))
            return null;
        return ENTITIES.get(type).create(id, 0f, "");
    }

    private interface EntityCreator {
        Entity create(short id, float rotation, String name);
    }

    private static class ClassEntityCreator implements EntityCreator {
        private Class<? extends Entity> class_;

        public ClassEntityCreator(Class<? extends Entity> class_) {
            this.class_ = class_;
        }

        @Override
        public Entity create(short id, float rotation, String name) {
            try {
                Entity e = class_.getConstructor(short.class).newInstance(id);
                e.setRotation((float) Math.toDegrees(rotation));
                return e;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static class ImageEntityCreator implements EntityCreator {
        private final String path;
        private float scale = 1f;

        public ImageEntityCreator(String path) {
            this.path = path;
        }

        public ImageEntityCreator(String path, float scale) {
            this.path = path;
            this.scale = scale;
        }

        @Override
        public Entity create(short id, float rotation, String name) {
            Entity e = Entity.fromImage(path, id);
            e.setScale(scale);
            e.setRotation((float) Math.toDegrees(rotation));

            return e;
        }
    }

    private static class TextEntityCreator implements EntityCreator {

        @Override
        public Entity create(short id, float rotation, String name) {
            byte[] temp = NetworkUtils.double2ByteArray(rotation);

            int size = NetworkUtils.byteArray2Int(new byte[] { temp[0], temp[1], temp[2], temp[3] });
            int color888 = NetworkUtils.byteArray2Int(new byte[] { temp[4], temp[5], temp[6], temp[7] });

            Text text = new Text(size, new Color(color888), Gdx.files.internal("fonts/INFO56_0.ttf"));
            text.setText(name);

            return text.toEntity(id);
        }
    }
}
