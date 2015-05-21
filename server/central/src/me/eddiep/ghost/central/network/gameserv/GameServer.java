package me.eddiep.ghost.central.network.gameserv;

public class GameServer {
    private String secret;

    private String ip;
    private int port;
    private byte id;
    private byte categoryId;
    private boolean isRanked;
    private GameServerConnection connection;
    private QueueInfo queueInfo;

    private GameServer() { }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public byte getId() {
        return id;
    }

    public byte getCategoryId() {
        return categoryId;
    }

    public boolean isRanked() {
        return isRanked;
    }

    public String getQueueName() {
        return QueueNamer.queueNameFrom(id);
    }

    public String getCatagoryName() {
        return QueueNamer.categoryNameFrom(categoryId);
    }

    public String getSecret() {
        return secret;
    }

    public GameServerConnection getConnection() {
        return connection;
    }

    void setConnection(GameServerConnection connection) {
        this.connection = connection;
    }

    public void setQueueInfo(QueueInfo queueInfo) {
        this.queueInfo = queueInfo;
    }

    public QueueInfo getQueueInfo() {
        return queueInfo;
    }
}
