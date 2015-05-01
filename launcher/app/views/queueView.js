var ghost = require('./vendor/ghost/ghost.js');
var queueType = document.location.hash.substring(1);

$(document).ready(function() {
    ghost.queues(function(queues) {
        var options = queues[queueType];
        for (var i in options) {
            $("#queues").append('<option value=' + options[i].type + '>' + options[i].name + '</option>');
        }
    }, function(err) {

    });
});

var inQueue = false;
var currentQueue;
var foundEnemyCount = 0;
var canCancel = true;

$("#homeBtn").click(function() {
    if (!inQueue) {
        window.location.replace("menu.html");
    } else if (canCancel) {
        var type = $("#queues").val();
        ghost.client().leaveQueue(type);
        
        ghost.client().on("ok", function(obj) {
            if (obj.isOk) {
                $("#playBtn").show();
                $("#homeBtn").text("Home");
        
                $("#queues").show();
                $("#queueText").show();
                $("#loader").hide();

                inQueue = false;
            } else {
                alert("Failed to leave queue!");
            }
        });
    }
});

$("#playBtn").click(function() {
    var type = $("#queues").val();
    console.log("Join queue " + type);
    ghost.client().joinQueue(type);

    ghost.queues(function(queues) {
        var options = queues[queueType];
        for (var i in options) {
            if (options[i].type == type) {
                currentQueue = options[i];
                break;
            }
        }
    });
    
    ghost.client().on("ok", function(obj) {
         if (obj.isOk) {
            $("#playBtn").hide();
            $("#homeBtn").text("Cancel");
        
            $("#queues").hide();
            $("#queueText").hide();
        
            $("#loader").show();
        
            inQueue = true;
        
            ghost.client().on('allyFound', function(name) {
                $("#name_a_2").fadeIn(1100);
                $("#a2_name").text(name);
                $("#homeBtn").prop("disable", true);
                canCancel = false;
            });
        
            ghost.client().on('enemyFound', function(name) {
                if (foundEnemyCount == 0) {
                    $("#name_o_1").fadeIn(1100);
                    $("#o1_name").text(name);
                    foundEnemyCount++;
                } else {
                    $("#name_o_2").fadeIn(1100);
                    $("#o2_name").text(name);
                    foundEnemyCount++;
                }

                $("#homeBtn").prop("disable", true);
                canCancel = false;

                if (currentQueue.opponentCount == foundEnemyCount) {
                    setTimeout(function() {
                        var count = 5;
                        $("#gameCountdown").foundation('reveal', 'open');
                        var id = setInterval(function() {
                            count--;
                            if (count < 0) {
                                console.log("Launching game");
                                clearInterval(id);
                                ghost.launch();
                            } else {
                                $("#countdownText").text("" + count);
                            }
                        }, 1000);
                    }, 4800);
                }
            });
         } else {
             alert("Failed to join queue!");
         }
    });

    
});