/*
   Ghost HTTP API and TCP API
*/
var domain = "45.55.160.242";
var httpPort = "8080";
var tcpPort = "2546";

process.argv.forEach(function (val, index, array) {
    if (val == "-d" || val == "--domain")
        domain = array[index + 1];
    else if (val == "-hp" || val == "-httpPort")
        httpPort = array[index + 1];
    else if (val == "-tp" || val == "--tcpPort")
        tcpPort = array[index + 1];
});

var user = { };
var http = require('http');
module.exports = {
    login: function(username, password, callback, err) {
        if (user.isLoggedIn) {
            user = { };
            user.isLoggedIn = false;
        }

        var post = "username=" + username + "&password=" + password;

        var post_options = {
            host: domain,
            port: httpPort,
            path: "/api/accounts/login",
            method: "PORT"
        };

        var request = http.request(post_options, function(res) {
            if (res.statusCode == 202) {
                var data = "";
                res.on('data', function (chunk) {
                    data += chunk;
                });
                res.on('end', function () {
                    user.isLoggedIn = true;
                    user.username = username;
                    user.stats = JSON.parse(data);
                    if (callback != null)
                        callback(user);
                });
            } else {
                if (err != null) {
                    var e = "";
                    res.on('data', function (chunk) {
                        e += chunk;
                    });
                    res.on('end', function (chunk) {
                        err(e, res.statusCode);
                    });
                }
            }
        });

        request.write(post);
        request.end();
    }

};
