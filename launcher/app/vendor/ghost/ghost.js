/*
   Ghost HTTP API and TCP API
*/
var domain = "45.55.160.242";
var httpPort = "8080";
var tcpPort = 2547;

process.argv.forEach(function (val, index, array) {
    if (val == "-d" || val == "--domain")
        domain = array[index + 1];
    else if (val == "-hp" || val == "-httpPort")
        httpPort = array[index + 1];
    else if (val == "-tp" || val == "--tcpPort")
        tcpPort = array[index + 1];
});

var http =    require('http');
var util =    require('util');
var events =  require('events');
var tcp = require('./tcpclient.js');
var querystring = require('querystring');

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
    }
};
