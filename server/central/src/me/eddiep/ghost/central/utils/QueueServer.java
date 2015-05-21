package me.eddiep.ghost.central.utils;

public class QueueServer {
    private byte queueId;
    private String internalName;
    private String ip;
    private String queueName;

    private QueueServer() { }


    public byte getQueueId() {
        return queueId;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getIp() {
        return ip;
    }

    public String getQueueName() {
        return queueName;
    }
}
