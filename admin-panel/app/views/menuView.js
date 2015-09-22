var http = require('http');
var domain = "127.0.0.1";
var playerChart;
var AUTH_CODE = "rCh9yvT7WL4Q1TG6ySxlOB8At19jvDdb";

var fetchInfo = function(callback) {
    var options = {
        host: domain,
        path: '/admin/info',
        port: '8080',
        headers: { 'X-AdminKey': AUTH_CODE}
    };

    var req = http.request(options, function(res) {
        if (res.statusCode == 401) {
            alert("Auth failed with server!");
            process.exit();
            return;
        }

        var data = '';
        res.on('data', function(chunk) {
            data += chunk;
        });

        res.on('end', function() {
            var info = JSON.parse(data);
            callback(info);
        });
    });
    req.on('error', function(e) {
        alert("Could not connect to server!\n" + e);
        process.exit();
    });
    req.end();
};

var fetchServers = function(callback) {
    var options = {
        host: domain,
        path: '/admin/servers',
        port: '8080',
        headers: { 'X-AdminKey': AUTH_CODE}
    };

    var req = http.request(options, function(res) {
        if (res.statusCode == 401) {
            alert("Auth failed with server!");
            process.exit();
            return;
        }

        var data = '';
        res.on('data', function(chunk) {
            data += chunk;
        });

        res.on('end', function() {
            var info = JSON.parse(data);
            callback(info);
        });
    });
    req.on('error', function(e) {
        alert("Could not connect to server!\n" + e);
        process.exit();
    });
    req.end();
};



var updateChart = function() {
    fetchInfo(function(info) {
        var date = new Date();
        var time = date.getHours() + ":" + date.getMinutes();

        playerChart.addData([info.playersInQueue], time);
    });
};

var toStream = function(level) {
    switch (level) {
        case 0:
            return 'Test';
        case 1:
            return 'Alpha';
        case 2:
            return 'Beta';
        case 3:
            return 'Buffered';
        case 4:
            return 'Live';
        default:
            return 'UNKNOWN';
    }
};

var toQueue = function(queue) {
    switch (queue) {
        case 1:
            return 'Original';
        case 2:
            return 'Laser';
        case 3:
            return 'Weapon Select';
        case 4:
            return '2v2 Weapon Select';
        case 5:
            return 'Dash';
        case 6:
            return 'Tutorial';
        case 7:
            return 'Boomerrang';
        case 8:
            return 'Ranked';
        case 254:
            return 'Test';
        default:
            return 'UNKNOWN';
    }
};

var updateServers = function() {
    var list = $('#server-list');
    fetchServers(function(servers) {
        list.empty();
        servers.forEach(function(server) {
            var html = '<li><div class="row server"><div class="small-3 columns"><i class="fa fa-power-off ' + (server.config.server ? toStream(server.config.streamLevel).toLowerCase() : 'offline') + '"></i>' +
                '</div><div class="small-8 columns"><div class="row"><div class="small-5 columns test">' +
                '<p>Name: ' + server.config.internal_name + '</p>' +
                '<p>ID: ' + server.config.id + '</p>' +
                '<p>Stream: ' + toStream(server.config.streamLevel) + '</p>' +
                '<p>Queue: ' + toQueue(server.config.queueServing) + '</p>' +
                '</div>' +
                '<div class="small-5 columns">' +
                '<p>IP: ' + server.config.ip + '</p>' +
                '<p>Port: ' + server.config.port + '</p>' +
                '<p>Match Count: ' + (server.config.server ? server.config.server.matchCount : 'offline') + '</p>' +
                '<p>Player Count: ' + (server.config.server ? server.config.server.playerCount : 'offline') + '</p>' +
                '</div>' +
                '</div>' +
                ' </div>' +
                '</div>' +
                '</li>';

            var element = $.parseHTML(html);
            list.append(element);
        });
    });
};

$(document).ready(function() {
    $('#loginLoading').foundation('reveal', 'open');

    fetchInfo(function(info) {
        var ctx = $('#playersOnline').get(0).getContext("2d");

        var date = new Date();
        var time = date.getHours() + ":" + date.getMinutes();

        var data = {
            labels: [ time ],
            datasets: [
                {
                    label: time,
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: [info.playersInQueue]
                }
            ]
        };

        playerChart = new Chart(ctx).Line(data, {
            bezierCurve: false
        });

        $('#matches').text('Matches: ' + info.matchCount);
        $('#serverCount').text('Servers: ' + info.connectedServers);

        setInterval(updateChart, 60000);
        setInterval(updateServers, 10000);

        $('#loginLoading').foundation('reveal', 'close');
    })
});