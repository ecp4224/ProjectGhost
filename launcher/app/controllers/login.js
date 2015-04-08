export var login = {
    version: function() {
        var util = require('util'),
            pkginfo = require('./package.json');

        return pkginfo ? pkginfo.version : "Unknown Version";
    },
    submit: function() {

    }
}


