
import { login } from '../controllers/login'

$(document).ready(function() {
    $("#version").html(login.version());
});

/*$('#loginBtn').click(function() {
    $("#loginLoading").foundation('reveal', 'open');

    var user = $('#username').val();
    var pass = $('#password').val();


    setTimeout(function() {
        login.submit(user, pass, function() {
            $("#loginLoading").foundation('reveal', 'close');
            window.location.replace('menu.html');
        }, function() {
            $("#loginLoading").foundation('reveal', 'close');
        });
    }, 800);
});*/

$(document).ready(function() {
    $('#loginForm').on('submit', function(e) {
        e.preventDefault();

        $("#loginLoading").foundation('reveal', 'open');

        var user = $('#username').val();
        var pass = $('#password').val();

        setTimeout(function () {
            login.submit(user, pass, function () {
                $("#loginLoading").foundation('reveal', 'close');
                window.location.replace('menu.html');
            }, function() {
                $("#loginLoading").foundation('reveal', 'close');
                alert('No');
            });
        }, 800);
    });

    $('#signupForm').on('submit', function(e) {
        e.preventDefault();

        $('#modalTitle').text('Creating Account..');
        $('#loginLoading').foundation('reveal', 'open');

        var user = $('#signup-username').val();
        var pass = $('#signup-password').val();
        var email = $('#signup-email').val();

        setTimeout(function () {
            login.register(user, pass, email, function() {
                $('#modalTitle').text('Signing In..');
            }, function () {
                $("#loginLoading").foundation('reveal', 'close');
                window.location.replace('menu.html');
            }, function () {
                $("#loginLoading").foundation('reveal', 'close');
                alert('No');
            });
        }, 800);
    })
});

