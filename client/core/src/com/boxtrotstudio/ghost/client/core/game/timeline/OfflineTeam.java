package com.boxtrotstudio.ghost.client.core.game.timeline;

import java.util.HashMap;

public class OfflineTeam {
    private String[] usernames;
    private Long[] playerIds;
    private HashMap<Long, Byte> weapons = new HashMap<>();
    private HashMap<Long, Integer> lives = new HashMap<>();
    private int teamNumber;

    private OfflineTeam() { }

    public String[] getUsernames() {
        return usernames;
    }

    public Long[] getPlayerIds() {
        return playerIds;
    }

    public int getTeamLength() {
        return usernames.length;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public byte getWeaponFor(long id) {
        return weapons.get(id);
    }

    public byte getWeaponFor(String username) {
        for (int i = 0; i < usernames.length; i++) {
            if (usernames[i].equals(username)) {
                long id = playerIds[i];
                return getWeaponFor(id);
            }
        }
        return -1;
    }

    public int getLivesFor(long id) {
        return lives.get(id);
    }

    public boolean containsName(String name) {
        for (String n : usernames) {
            if (n.equals(name))
                return true;
        }
        return false;
    }

    public boolean containsID(long id) {
        for (long pID : playerIds) {
            if (id == pID)
                return true;
        }
        return false;
    }
}
