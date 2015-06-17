var net = require('net');
var ghost = require('./ghost.js');
var events =  require('events');
var util =    require('util');
var hasStarted = false;
var worker = null;

function ClientHandler(session, port, host) {
    events.EventEmitter.call(this);
    this.connected = false;
    this.port = port;
    this.host = host;
    this.session = session;
}

util.inherits(ClientHandler, events.EventEmitter);

ClientHandler.prototype.connect = function() {
    var _this = this;

    this.client = net.connect({ host: this.host, port: this.port }, function() {
        _this.connected = true;

        var sessionPacket = new Buffer(37);
        sessionPacket[0] = 0x00;
        sessionPacket.write(_this.session, 1, _this.session.length, 'ascii');

        _this.write(sessionPacket);

        _this.once('ok', function(e) {
            if (e.isOk) {
                _this.emit('session');
            } else {
                _this.emit('badsession');
            }
        });
    });

    this.client.on('close', function() {
        _this.connected = false;
    })
};

ClientHandler.prototype.isConnected = function() {
    return this.connected;
};

ClientHandler.prototype.prepare = function(callback) {
    if (!this.isConnected()) {
        this.connect();
    }

    var _this = this;

    this.client.on('data', function(data) {
        var opCode = data[0];

        switch (opCode) {
            case 0x01: //OK Packet
                var value = data[1];
                _this.emit('ok', {
                    handler: _this,
                    isOk: value == 1
                });
                break;
            case 0x02: //OnMatchFound Packet
                //This data isn't really needed...the game client will get this data again
                break;
            case 0x10: //SpawnEntity Packet
                //This will be used to determine the enemies username and the allies username.
                //Since this data is always sent after the OnMatchFound Packet

                //The game client will get this data again

                var type = data[1];

                var nameLength = data[4];
                var name = data.toString('ascii', 5, 5 + nameLength);

                if (type == 0) {
                    _this.emit('allyFound', name);
                } else if (type == 1) {
                    _this.emit('enemyFound', name);
                }
                break;
            case 0x15: //Notification Packet
                var nid = data.readInt32LE(1);
                var isRequest = data[5] == 1;
                var titleLength = data.readInt32LE(6);
                var descLength = data.readInt32LE(10);
                var title = data.toString('ascii', 14, 14 + titleLength);
                var desc = data.toString('ascii', 14 + titleLength, 14 + titleLength + descLength);

                _this.emit('notification', {
                    id: nid,
                    isRequest: isRequest,
                    title: title,
                    description: desc
                });
                break;
            case 0x16: //Remove Notification Packet
                var rid = data.readInt32LE(1);
                _this.emit('notificationRemoved', rid);
                break;
            case 0x19: //Ping Packet
                var pingValue = data.readInt32LE(1);
                console.log("Got ping value " + pingValue);
                break;
        }
    });
};

ClientHandler.prototype.write = function(data) {
    if (this.isConnected()) {
        this.client.write(data);
    }
};

ClientHandler.prototype.respondToRequest = function(id, result) {
    var data = new Buffer(6);
    data[0] = 0x17;
    data.writeInt32LE(id, 1);
    data[5] = (result ? 1 : 0);

    this.write(data);
};

ClientHandler.prototype.requestSetDisplayName = function(username, callback) {
    var data = new Buffer(2 + username.length);
    data[0] = 0x14;
    data[1] = username.length;
    data.write(username);

    this.write(data);

    this.once('ok', callback);
};

ClientHandler.prototype.ping = function() {
    if (!this.pingCount) {
        this.pingCount = 0;
    }

    this.pingCount++;

    var ping = new Buffer(32);
    ping[0] = 0x19;
    ping.writeInt32LE(this.pingCount, 1);

    this.write(ping);
};

ClientHandler.prototype.joinQueue = function(type) {
    var data = new Buffer(2);
    data[0] = 0x05;
    data[1] = type;

    this.write(data);
};

ClientHandler.prototype.leaveQueue = function(type) {
    var data = new Buffer(2);
    data[0] = 0x20;
    data[1] = type;
    
    this.write(data);
};

ClientHandler.prototype.disconnect = function() {
    this.client.end();
};

ClientHandler.prototype.setAbility = function(id) {
    var data = new Buffer(2);
    data[0] = 0x22;
    data[1] = id;

    this.write(data);
};


module.exports = {
    start: function(session, port, host) {
        if (!session) {
            throw 'no session!';
        }

        if (hasStarted) {
            return
        }

        var client = new ClientHandler(session, port, host);
        client.prepare();
        return client;
    }
};
