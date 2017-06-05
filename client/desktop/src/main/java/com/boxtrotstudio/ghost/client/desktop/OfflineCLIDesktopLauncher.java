package com.boxtrotstudio.ghost.client.desktop;

import org.apache.commons.cli.ParseException;

public class OfflineCLIDesktopLauncher extends DesktopLauncher {

    public static void main(String[] args) throws ParseException {
        DesktopLauncher.main(new String[] { "107.170.23.29", "--test", "--cli" });
    }
}
