var ghost = require('./vendor/ghost/ghost.js');

$(document).ready(function() {
    var type = document.location.hash.substring(1);
    ghost.queues(function(queues) {
        var options = queues[type];
        for (var i in options) {
            $("#queues").append('<option value=' + options[i].type + '>' + options[i].name + '</option>');
        }
    }, function(err) {

    });
});

$("#homeBtn").click(function() {
    window.location.replace("menu.html");
});