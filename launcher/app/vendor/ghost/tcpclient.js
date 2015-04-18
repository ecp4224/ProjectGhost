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
        })
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
            case 0x01:
                var value = data[1];
                _this.emit('ok', {
                    handler: _this,
                    isOk: value == 1
                });
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

    this.once('ok', callback);
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
