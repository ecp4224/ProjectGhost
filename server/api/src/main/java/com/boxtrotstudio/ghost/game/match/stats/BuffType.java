package com.boxtrotstudio.ghost.game.match.stats;

public enum BuffType {

    Addition(1),
    Subtraction(2),
    Percentage(4),
    PercentAddition(Percentage.id | Addition.id),
    PercentSubtraction(Percentage.id | Subtraction.id);

    int id;

    BuffType(int id) {
        this.id = id;
    }
}

