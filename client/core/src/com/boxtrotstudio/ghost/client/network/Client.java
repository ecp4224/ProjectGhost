package com.boxtrotstudio.ghost.client.network;

import java.io.IOException;

public interface Client {
    void writeUDP(byte[] data) throws IOException;

    void write(byte[] data) throws IOException;

    void flush() throws IOException;

    int read(byte[] data, int index, int length) throws IOException;

    void disconnect() throws IOException;
}