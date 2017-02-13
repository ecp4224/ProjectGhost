package com.boxtrotstudio.ghost.gameserver.api;

import me.eddiep.ubot.module.Logger;

public class UBotLogger implements Logger {
    @Override
    public void log(String s) {
        System.out.println("[UBot] " + s);
    }

    @Override
    public void warning(String s) {
        System.err.println("[UBot] " + s);
    }

    @Override
    public void init() { }

    @Override
    public void dispose() { }
}
