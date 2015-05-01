/*
   Ghost HTTP API and TCP API
*/
var domain = "45.55.160.242";
var httpPort = "8080";
var tcpPort = 2547;

var http =         require('http');
var util =         require('util');
var events =       require('events');
var tcp =          require('./tcpclient.js');
var querystring =  require('querystring');
var cp =           require("child_process");
var process =      require("process");

process.argv.forEach(function (val, index, array) {
    if (val == "-d" || val == "--domain")
        domain = array[index + 1];
    else if (val == "-hp" || val == "-httpPort")
        httpPort = array[index + 1];
    else if (val == "-tp" || val == "--tcpPort")
        tcpPort = array[index + 1];
});

function LoginHandler(username, password) {
    events.EventEmitter.call(this);

    this.username = username;
    this.password = password;
    this.user = { };
    this.client = {
        _isConnected: false
    };
}

util.inherits(LoginHandler, events.EventEmitter);

LoginHandler.prototype.register = function() {
    var post = "username=" + this.username + "&password=" + this.password;

    var post_options = {
        host: domain,
        port: httpPort,
        path: "/api/accounts/register",
        method: "POST",
        headers: {
            'Content-Length': post.length
        }
    };

    var _this = this;

    var request = http.request(post_options, function(res) {
        if (res.statusCode == 202) {
            _this.emit("registered");
        } else {
            var data = "";
            res.on('data', function(chunk) {
                data += chunk;
            });
            res.on('end', function() {
                _this.emit("failed", data);
            });
        }
    });

    request.write(post);
    request.end();
};

LoginHandler.prototype.login = function() {
    if (this.user.isLoggedIn) {
        this.user = { };
        user.isLoggedIn = false;
    }

    var post = querystring.stringify({
        'username': this.username,
        'password': this.password
    });
    //var post = "username=" + this.username + "&password=" + this.password;

    var post_options = {
        host: domain,
        port: httpPort,
        path: "/api/accounts/login",
        method: "POST",
        headers: {
            'Content-Length': post.length
        }
    };

    var _this = this;

    var request = http.request(post_options, function(res) {
        if (res.statusCode == 202) {
            var data = "";
            res.on('data', function (chunk) {
                data += chunk;
            });
            res.on('end', function () {
                _this.user.isLoggedIn = true;
                _this.user.username = _this.username;
                _this.user.stats = JSON.parse(data);

                var cookies = res.headers['set-cookie'];
                var session;
                for (var i in cookies) {
                    if (cookies[i].split('=')[0] == "session") {
                        session = cookies[i].split('=')[1].split(';')[0].trim();
                        break;
                    }
                }

                _this.user.session = session;
                console.log(_this.user.session);

                _this.emit("login");
            });
        } else {
            var e = "";
            res.on('data', function (chunk) {
                e += chunk;
            });
            res.on('end', function () {
                _this.emit("loginFailed", e, res.statusCode);
            });
        }
    });

    request.write(post);
    request.end();
};

LoginHandler.prototype.connect = function() {
    if (!this.user || !this.user.isLoggedIn) {
        throw 'not logged in!';
    }

    var _this = this;
    this.client = tcp.start(this.user.session, tcpPort, domain);
    this.client.on('session', function() {
        _this.emit('connect');
    });
};

LoginHandler.prototype.registerAndConnect = function(callback) {
    var _this = this;

    this.register();
    this.once('registered', function() {
        _this.loginAndConnect(callback);
    });
};

LoginHandler.prototype.loginAndConnect = function(callback) {
    var _this = this;

    this.login();
    this.once('login', function() {
        _this.connect();
        if (callback != null)
            callback();
    })
};

LoginHandler.prototype.claimDisplayname = function() {
    var displayName;
    if (arguments.length > 0) {
        displayName = arguments[0];
    } else return;

    var _this = this;

    this.client.requestSetDisplayName(displayName, function() {
        _this.emit('displayname', { displayName: displayName });
    });
};

var storedHandler = null;
var pingId;

module.exports = {
    ghostDomain: domain,
    ghostHttpPort: httpPort,
    ghostTcpPort: tcpPort,

    login: function(username, password) {
        var handler = new LoginHandler(username, password);

        handler.loginAndConnect();
        return handler;
    },

    register: function(username, password, displayName) {
        var handler = new LoginHandler(username, password, displayName);

        handler.registerAndConnect();
        return handler;
    },

    saveHandler: function(handler) {
        storedHandler = handler;
    },

    startPing: function() {
        if (!storedHandler) {
            throw "Invalid login handler found!"
        }

        if (pingId) {
            clearInterval(pingId);
        }
        pingId = setInterval(function() {
            storedHandler.client.ping();
        }, 300);
    },

    user: function() {
        return storedHandler.user;
    },

    handler: function() {
        return storedHandler;
    },

    client: function() {
        return storedHandler.client;
    },

    queues: function(callback, err) {
        http.get("http://" + domain + ":" + httpPort + "/api/queues", function(res) {
            var body = "";
            res.on('data', function(chunk) {
                body += chunk;
            });
            res.on('end', function() {
                callback(JSON.parse(body));
            });
        }).on('error', function(e) {
            err(e);
        });
    },

    joinQueue: function(type) {
        if (!storedHandler) {
            throw "Invalid login handler found!";
        }

        storedHandler.client.joinQueue(type);
    },

    launch: function() {
        if (!storedHandler) {
            throw "Invalid login handler found!";
        } else if (!storedHandler.user) {
            throw "Not logged in!";
        } else if (!storedHandler.user.session) {
            throw "Invalid login session found!";
        }

        var os = process.platform;

        storedHandler.client.disconnect(); //Disconnect from TCP

        //TODO Make callback for when game closes
        //TODO Maybe hide current node window?

        if (os == "win32") {
           cp.execFile("game/game.exe \""  + domain + "\" \"" + storedHandler.user.session + "\"");
        } else if (os == "darwin") {
            //TODO Mac
        } else if (os == "linux") {
            //TODO Linux
        } else {
            throw "Unsupported platform!";
        }
    }
};
