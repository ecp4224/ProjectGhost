package com.boxtrotstudio.ghost.client.core.render.text;

import com.boxtrotstudio.ghost.client.core.render.Text;

public enum TextOptions implements TextOption {

    /*
        public static final int SHADOW = (1 << 0);
    public static final int BOLD = (1 << 1);
    public static final int ITALIC = (1 << 2);
    public static final int TUTORIAL = (1 << 3);
     */

    SHADOW(1 << 0) {
        @Override
        public void apply(Text text) {
            //TODO Implement shadows
        }
    },
    BOLD(1 << 1) {
        @Override
        public void apply(Text text) {
            //TODO Implement bold
        }
    },
    ITALIC(1 << 2) {
        @Override
        public void apply(Text text) {
            //TODO Implement italic
        }
    },
    TUTORIAL(1 << 3) {
        @Override
        public void apply(Text text) {
            System.out.println("TUTORIAL TEXT");
        }
    };

    int flag;
    TextOptions(int flag) { this.flag = flag; }

    public int getFlag() {
        return flag;
    }
}
