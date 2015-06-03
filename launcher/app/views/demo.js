var gui   = require('nw.gui');
var ghost = require('./vendor/ghost/ghost.js');

var USERNAME = "player1";
var PASSWORD = "123454321";

var handler = ghost.login(USERNAME, PASSWORD);
handler.on('connect', function() {
    ghost.saveHandler(handler);
});

var win = gui.Window.get();

$('[data-queue]').click(function(e) {
    var queue = $(this).attr('data-queue');

    ghost.client().joinQueue(queue);

    ghost.client().on('ok', function(e) {
        $('#loginLoading').foundation('reveal', 'open');
        if (e.isOk) {
            ghost.client().on('enemyFound', function(name) {
                setTimeout(function() {
                    var count = 5;
                    $('#loginLoading').foundation('reveal', 'close');
                    $("#gameCountdown").foundation('reveal', 'open');
                    var id = setInterval(function() {
                        count--;
                        if (count < 0) {
                            console.log("Launching game");
                            clearInterval(id);
                            ghost.launch(function(e) {
                                win.show();
                            });
                            win.hide();
                            $("#gameCountdown").foundation('reveal', 'close');
                        } else {
                            $("#countdownText").text("" + count);
                        }
                    }, 1000);
                }, 1800);
            });

        } else {
            alert("Failed to join queue!");
            $('#loginLoading').foundation('reveal', 'close');
        }
    });
});

$('#registerClose').click(function(e) {
    $('#register').foundation('reveal', 'close');
});