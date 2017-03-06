package com.boxtrotstudio.ghost.matchmaking;

import com.amazonaws.auth.AWSCredentials;
import me.eddiep.jconfig.system.Config;
import me.eddiep.jconfig.system.annotations.DefaultValue;
import me.eddiep.jconfig.system.annotations.Getter;

public interface ServerConfig extends Config, AWSCredentials {

    @Getter(property = "serverPort")
    @DefaultValue(value = "2547")
    int getServerPort();

    @Getter(property = "serverIp")
    @DefaultValue(value = "")
    String getServerIP();

    @Getter(property = "secret")
    @DefaultValue(value = "super_secret_12345")
    String getServerSecret();

    @Getter(property = "adminSecret")
    @DefaultValue(value = "super_admin_1221")
    String getAdminSecret();

    @Getter(property = "hostLoginServer")
    @DefaultValue(value = "false")
    boolean hostLoginServer();

    @Getter(property = "defaultStream")
    @DefaultValue(value = "4")
    int defaultStream();

    @Getter(property = "useAWS")
    boolean useAWS();

    @Getter(property = "awsFleetID")
    String targetAWSFleetID();

    @Getter(property = "awsAccessKeyID")
    String getAWSAccessKeyId();

    @Getter(property = "awsSecretKey")
    String getAWSSecretKey();
}
