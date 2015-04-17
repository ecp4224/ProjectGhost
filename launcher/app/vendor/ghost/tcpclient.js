var tcpWorker = require('webworker-threads').Worker;
var net = require('net');
var ghost = require('./ghost.js');
var events =  require('events');
var util =    require('util');
var createClient = require('flow-tcp-client');
var hasStarted = false;
var worker = null;

function ClientHandler(session) {
    events.EventEmitter.call(this);
    this.client = createClient();
    this.client.host(ghost.ghostDomain)
        .port(ghost.ghostTcpPort);
    this.stream = null;
    this.session = session;
}

util.inherits(ClientHandler, events.EventEmitter);

ClientHandler.prototype.connect = function() {
    this.client.connect();
    this.stream = this.client.stream();
};

ClientHandler.prototype.isConnected = function() {
    return this.client.status();
};

ClientHandler.prototype.prepare = function(callback) {
    if (!this.isConnected()) {
        this.connect();
    }

    var _this = this;
    this.stream.on('readable', function() {
        var sessionPacket = new Buffer(37);
        sessionPacket[0] = 0x00;
        sessionPacket.write(_this.session, 1, _this.session.length, 'ascii');

        _this.write(sessionPacket);

        callback();
    });
};

ClientHandler.prototype.readData = function() {
    while (this.isConnected()) {
        var opCode = this.blockRead(1);
        if (opCode) {
            opCode = opCode[0];

            switch (opCode) {
                case 0x01:
                    var value = this.blockRead(1)[0];
                    this.emit('ok', { isOk: value == 1 });
                    break;
            }
        }
    }
};

ClientHandler.prototype.blockRead = function(size) {
    while (true) {
        var data = this.stream.read(size);
        if (null !== data) {
            return data;
        }
    }
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

    this.once('on', callback);
};


module.exports = {
    start: function(session) {
        if (!session) {
            throw 'no session!';
        }

        if (hasStarted) {
            return
        }

        var connection = new ClientHandler(session);
        connection.prepare(function() {
            worker = new Worker(function() {
                this.onmessage = function(event) {
                    event.data.readData();
                };
            });

            worker.postMessage(connection);
        });

        return connection;
    }
};
