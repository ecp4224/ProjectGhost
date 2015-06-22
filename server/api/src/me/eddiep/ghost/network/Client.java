package me.eddiep.ghost.network;

import java.io.IOException;
import java.net.InetAddress;

public abstract class Client<T extends Server> {
    private User user;
    protected InetAddress IpAddress;
    protected int port = -1;
    protected T socketServer;
    protected boolean connected = true;

    public Client(User player, T server) throws IOException {
        this.user = player;
        this.socketServer = server;

        this.user.setClient(this);
    }

    public abstract void listen();

    public InetAddress getIpAddress() {
        return IpAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (this.port != -1)
            throw new IllegalStateException("This client already has a UDP Port!");

        this.port = port;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.IpAddress = ipAddress;
    }

    public boolean isLoggedIn() {
        return port != -1 && IpAddress != null;
    }

    public User getUser() {
        return user;
    }

    public void disconnect() throws IOException {
        connected = false;

        if (user != null) {
            user.disconnected();
        }
        user = null;

        onDisconnect();
    }

    protected abstract void onDisconnect() throws IOException;

    public abstract void write(byte[] data) throws IOException;

    public abstract int read(byte[] into, int offset, int length) throws IOException;

    public T getServer() {
        return socketServer;
    }
}
