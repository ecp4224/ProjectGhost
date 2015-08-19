package me.eddiep.ghost.game.match.entities.stats;

public enum BuffType {

    Addition(1),
    Subtraction(2),
    Percentage(4),
    PercentAddition(Percentage.id | Addition.id),
    PercentSubtraction(Percentage.id | Addition.id);

    int id;

    BuffType(int id) {
        this.id = id;
    }
}

