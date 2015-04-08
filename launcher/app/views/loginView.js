
import { login } from '../controllers/login'

$(document).ready(function() {
    $("#version").html(login.version());
})

