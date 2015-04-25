
import { login } from '../controllers/login'

$(document).ready(function() {
    $("#version").html(login.version());
});

$('#loginBtn').click(function() {
    $("#loginLoading").foundation('reveal', 'open');

    var user = $('#username').val();
    var pass = $('#password').val();


    login.submit(user, pass, function() {
        $("#loginLoading").foundation('reveal', 'close');
        window.location.replace('menu.html');
    }, function() {
        $("#loginLoading").foundation('reveal', 'close');
    });
});

