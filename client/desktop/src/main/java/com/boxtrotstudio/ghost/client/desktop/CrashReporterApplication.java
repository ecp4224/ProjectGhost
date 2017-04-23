package com.boxtrotstudio.ghost.client.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import java.io.*;
import java.nio.charset.Charset;

class CrashReporterApplication extends LwjglApplication {

    CrashReporterApplication(ApplicationListener listener, LwjglApplicationConfiguration config) {
        super(listener, config);
    }

    void initLogging() {
        mainLoopThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                String text = errors.toString();

                try {
                    FileOutputStream out = new FileOutputStream("crash-" + System.currentTimeMillis() + ".txt");
                    out.write(text.getBytes(Charset.defaultCharset()));
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
