package me.eddiep.ghost.client.core.game;

import me.eddiep.ghost.client.core.animations.Animation;
import me.eddiep.ghost.client.core.animations.AnimationVariant;

public class Skin {
    private String name = "DEFAULT";
    private String texture_file = "";
    private String variant_name = "DEFAULT";
    private String character_name = "";

    private Skin() { }

    public String getName() {
        return name;
    }

    public String getTexturFile() {
        return texture_file;
    }

    public String getVarianName() {
        return variant_name;
    }

    public String getCharacterName() {
        if (character_name == null)
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
