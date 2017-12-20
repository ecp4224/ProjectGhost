package com.boxtrotstudio.ghost.client.core.game;

import com.boxtrotstudio.ghost.client.core.game.animations.Animation;
import com.boxtrotstudio.ghost.client.core.game.animations.AnimationVariant;

public class Skin {
    private String name = "DEFAULT";
    private String texture_file = "";
    private String variant_name = "DEFAULT";
    private String character_name = "";

    private Skin() { }

    public String getName() {
        return name;
    }

    public String getTextureFile() {
        return texture_file;
    }

    public String getVariantName() {
        return variant_name;
    }

    public String getCharacterName() {
        if (character_name == null || character_name.equals(""))
            return name;

        return character_name;
    }

    public void applyTo(CharacterCreator character) {
        for (Animation animation : character.getAnimations()) {
            AnimationVariant variant = animation.getVariant(variant_name);
            if (variant != null) {
                animation.applyVariant(variant);
            }
        }
    }
}
