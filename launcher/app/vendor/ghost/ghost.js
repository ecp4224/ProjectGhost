/*
   Ghost HTTP API and TCP API
*/
var domain = "login.ghost.algorithmpurple.io";
var mmdomain = "127.0.0.1";
var httpPort = "80";
var tcpPort = 2178;

var http =         require('http');
var util =         require('util');
var events =       require('events');
var tcp =          require('./tcpclient.js');
var querystring =  require('querystring');
var cp =           require("child_process");
var process =      require("process");

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

LoginHandler.prototype.register = function(email) {
    var post = "email=" + email + "&username=" + this.username + "&password=" + this.password;

    var post_options = {
        host: domain,
        port: httpPort,
        path: "/api/v1/auth/register",
        method: "POST",
        headers: {
            'Content-Length': post.length
        }
    };

    var _this = this;

    var request = http.request(post_options, function(res) {
        if (res.statusCode == 422) {
            var data = "";
            res.on('data', function(chunk) {
                data += chunk;
            });
            res.on('end', function() {
                var data = JSON.parse(data);
                _this.emit("failed", data.message);
            });
        } else {
            _this.emit("registered");
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
        path: "/api/v1/auth/login",
        method: "POST",
        headers: {
            'Content-Length': post.length
        }
    };

    var _this = this;

    var request = http.request(post_options, function(res) {
        var data = "";
        res.on('data', function(chunk) {
            data += chunk;
        });
        res.on('end', function() {
            if (res.statusCode == 422) {
                _this.emit('loginFailed', data);
            } else {
                var obj = JSON.parse(data);
                if (obj.success) {
                    _this.user.session = obj.session_id;
                    _this.user.isLoggedIn = true;

                    http.get("http://" + domain + "/api/v1/user/info?session_id=" + _this.user.session, function(res) {
                        var body = "";
                        res.on('data', function(chunk) {
                            body += chunk;
                        });
                        res.on('end', function() {
                            console.log(body);
                        });
                    }).on('error', function(e) {
                        console.log("Error getting info " + e);
                    });

                    _this.emit('login');
                } else {
                    _this.emit('loginFailed', obj.message);
                }
            }
        });
    });

    request.write(post);
    request.end();
};

LoginHandler.prototype.connect = function() {
    if (!this.user || !this.user.isLoggedIn) {
        throw 'not logged in!';
    }

    var _this = this;
    this.client = tcp.start(this.user.session, tcpPort, mmdomain);
    this.client.on('session', function() {
        _this.emit('connect');
    });
};

LoginHandler.prototype.registerAndConnect = function(email, callback) {
    var _this = this;

    this.register(email);
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

    processArgs: function(argv) {
        for (var i = 0; i < argv.length; i++) {
            var val = argv[i];
            if (val == "-d" || val == "--domain")
                domain = argv[i + 1];
            else if (val == "-hp" || val == "-httpPort")
                httpPort = argv[i + 1];
            else if (val == "-tp" || val == "--tcpPort")
                tcpPort = argv[i + 1];
        }
    },

    login: function(username, password) {
        var handler = new LoginHandler(username, password);

        handler.loginAndConnect();
        return handler;
    },

    register: function(username, password, email) {
        var handler = new LoginHandler(username, password);

        handler.registerAndConnect(email);
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

    launch: function(ended) {
        if (!storedHandler) {
            throw "Invalid login handler found!";
        } else if (!storedHandler.user) {
            throw "Not logged in!";
        } else if (!storedHandler.user.session) {
            throw "Invalid login session found!";
        }

        var os = process.platform;

        storedHandler.client.disconnect(); //Disconnect from TCP

        if (os == "win32") {
           var proc = cp.exec("\"game.exe\" \"" + domain + "\" \"" + storedHandler.user.session + "\" -f");

            proc.on('exit', ended);
            proc.on('end', ended);
        } else if (os == "darwin") {
            //TODO Mac
        } else if (os == "linux") {
            //TODO Linux
        } else {
            throw "Unsupported platform!";
        }
    }
};
