package me.eddiep.ghost.client.network;

import java.io.IOException;

public interface Client {
    void write(byte[] data) throws IOException;

    void flush() throws IOException;

    int read(byte[] data, int index, int length)  throws IOException;

    void disconnect() throws IOException;
}
