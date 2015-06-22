package me.eddiep.ghost.game.ranking;

public enum RankType {
    Scrub(Integer.MIN_VALUE, 199),
    MLGPro(2600, Integer.MAX_VALUE),
    UNKNOWN(0, 0);

    int min, max;
    RankType(int min, int max) { this.min = min; this.max = max; }

    public static RankType fromInt(int rating) {
        for (RankType t : RankType.values()) {
            if (rating >= t.min && rating <= t.max)
                return t;
        }
        return UNKNOWN;
    }
}
