package me.eddiep.ghost.client.core.game;

import me.eddiep.ghost.client.core.animations.Animation;
import me.eddiep.ghost.client.core.sprites.InputEntity;
import me.eddiep.ghost.client.core.sprites.NetworkPlayer;
import me.eddiep.ghost.client.utils.Global;

import java.io.IOException;
import java.nio.file.Files;

public class CharacterCreator {
    private String name;
    private byte id;
    private Animation[] animations;
    private Skin[] skins;

    private volatile Skin currentSkin;

    public static CharacterCreator create(Characters character, String skinName) throws IOException {
        String json = new String(Files.readAllBytes(character.getCharacterFile().toPath()));

        CharacterCreator creator = Global.GSON.fromJson(json, CharacterCreator.class);

        Skin skin = creator.skins[0];
        for (Skin s : creator.skins) {
            if (s.getName().equals(skinName))
                skin = s;
        }

        if (skin != null) {
            skin.applyTo(creator);
            creator.currentSkin = skin;
        }

        return creator;
    }

    public static InputEntity createPlayer(Characters character, String skinName, short id) throws IOException {
        CharacterCreator creator = create(character, skinName);

        InputEntity entity = new InputEntity(id, creator.currentSkin.getTexturFile());
        entity.attachAnimations(creator.animations);

        return entity;
    }

    public static NetworkPlayer createNetworkPlayer(Characters character, String skinName, short id) throws IOException {
        CharacterCreator creator = create(character, skinName);

        NetworkPlayer entity = new NetworkPlayer(id, creator.currentSkin.getTexturFile());
        entity.attachAnimations(creator.animations);

        return entity;
    }

    private CharacterCreator() { }

    public String getName() {
        return name;
    }

    public byte getId() {
        return id;
    }

    public Animation[] getAnimations() {
        return animations;
    }

    public Skin[] getSkins() {
        return skins;
    }

    public Skin getCurrentSkin() {
        return currentSkin;
    }
}
