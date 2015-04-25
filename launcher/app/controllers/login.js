export var login = {
    version: function() {
        var util = require('util'),
            pkginfo = require('./package.json');

        return pkginfo ? pkginfo.version : "Unknown Version";
    },
    submit: function(user, pass, login, failed) {
        var ghost = require('./vendor/ghost/ghost.js');

        var handler = ghost.login(user, pass);
        handler.on('connect', function() {
            ghost.saveHandler(handler);
            login();
        });
        handler.on('loginFailed', function() {
            failed();
        });
    }
};


