package me.eddiep.ghost.client.network;

import java.io.IOException;

public class PlayerClient implements Client {
    @Override
    public void write(byte[] data) throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public int read(byte[] data, int index, int length) throws IOException {
        return 0;
    }

    @Override
    public void disconnect() throws IOException {

    }
}
