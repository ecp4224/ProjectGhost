var ghost = require('./vendor/ghost/ghost.js');
import { login } from '../controllers/login'

$(document).ready(function() {
    $("#version").html(login.version());
});

$('#loginBtn').click(function() {
    var user = $('#username').val();
    var pass = $('#password').val();

    var handler = ghost.login(user, pass);
    handler.on('connect', function() {
        alert('You have been logged in!');
    });
    handler.on('loginFailed', function() {
        console.log("Login failed!");
    });
});

