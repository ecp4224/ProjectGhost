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

var inQueue = false;
var foundEnemy1 = false;
var canCancel = true;

$("#homeBtn").click(function() {
    if (!inQueue) {
        window.location.replace("menu.html");
    } else if (canCancel) {
        $("#playBtn").show();
        $("#homeBtn").text("Home");

        $("#queues").show();
        $("#queueText").show();
        $("#loader").hide();
    }
});

$("#playBtn").click(function() {
    var type = $("#queues").val();
    console.log("Join queue " + type);
    ghost.joinQueue(type);

    $("#playBtn").hide();
    $("#homeBtn").text("Cancel");

    $("#queues").hide();
    $("#queueText").hide();

    $("#loader").show();

    inQueue = true;

    ghost.handler().on('allyFound', function(name) {
        $("#name_a_2").show();
        $("#a2_name").text(name);
        canCancel = false;
    });

    ghost.handler().on('enemyFound', function(name) {
        if (!foundEnemy1) {
            $("#name_o_1").show();
            $("#o1_name").text(name);
        } else {
            $("#name_o_2").show();
            $("#o2_name").text(name);
        }

        canCancel = false;
    });
});