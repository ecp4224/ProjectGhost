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
        var type = $("#queues").val();
        ghost.client().leaveQueue(type);
        
        ghost.client().on("ok", function(obj) {
            if (obj.isOk) {
                $("#playBtn").show();
                $("#homeBtn").text("Home");
        
                $("#queues").show();
                $("#queueText").show();
                $("#loader").hide();
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
                canCancel = false;
            });
        
            ghost.client().on('enemyFound', function(name) {
                if (!foundEnemy1) {
                    $("#name_o_1").fadeIn(1100);
                    $("#o1_name").text(name);
                } else {
                    $("#name_o_2").fadeIn(1100);
                    $("#o2_name").text(name);
                }
        
                canCancel = false;
            });
         } else {
             alert("Failed to join queue!");
         }
    });

    
});