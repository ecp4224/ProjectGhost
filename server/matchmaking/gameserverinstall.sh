#!/bin/bash
#
rm -rf ProjectGhost-Server
apt-get -y update
apt-get -y install zip git mongodb openjdk-8-jdk openjdk-8-jre
curl -s https://get.sdkman.io | bash
source "/root/.sdkman/bin/sdkman-init.sh"
sdk install gradle 3.1
git clone https://eddiepenta:changemepls@git.aaronash.ninja/boxtrotstudio/ProjectGhost-Server.git
echo \{\"sqlDriver\":\"me.eddiep.ghost.test.network.sql.impl.MongoDB\",\"hiresTimer\":\"true\",\"serverIp\":\"\",\"serverMaxBacklog\":\"10000\",\"serverPort\":\"0\",\"tickGroupSize\":\"10\",\"matchmakingIP\":\"mm.projectghost.io\",\"matchmakingPort\":\"2547\",\"matchmakingSecret\":\"31JHZwy8ZCOh8HMTJyAYtuQ4NtcJ1Elm\",\"serverID\":\"1\",\"heartbeatInterval\":\"300\"\} > ProjectGhost-Server/server/gameserver/server.json