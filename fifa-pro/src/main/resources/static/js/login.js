$(document).ready(function() {
    loginForm = $("#login-form");
    enter = $("#enter");
    registrationForm = $("#registration-form");
    loginMessage = $("#login-message");
    login = $("#login");
    $("#league-games").hide();
    var user;

    hideAllLoginForms();

    // check login
    $.ajax({
            url : "user/check",
            type : "GET",
            data: {"key" : localStorage.getItem("sessionKey")},
            success : function(data) {
                    //console.log(" -> ", data.length);
                if (data.length != 0) {
                user = data.user;
                localStorage.setItem("teamadm", user.teamId == null ? 0 : user.teamId);
                    loginMessage.text("Hi " + user.name).show();
                    login.hide();
                    $("#league-games").show();
                }
            }
    });

    loginMessage.click(function() {
        $.ajax({
            url : "user/logout",
            type : "GET",
            data : {key : localStorage.getItem("sessionKey")},
            success : function() {
                localStorage.setItem("sessionKey", "");
                window.location.reload();
            }
        })
    })

    $("#login-show").click(function() {
        hideAllLoginForms();
        loginForm.show();
    });

    enter.click(function() {
        userName = $("#user-name").val();
        pass = CryptoJS.MD5($("#password").val()) + "";
            $.ajax({
                    url : "user/login",
                    type : "POST",
                    data: {"username" : userName, "password" : pass, "sessionKey" : localStorage.getItem("sessionKey")},
                    success : function(data) {
                        if (data.message != null) {
                            loginMessage.text(data.message).show();
                        } else {
                            localStorage.setItem("sessionKey", data.sessionKey);
                            window.location.reload();
                        }
                    }
            });
    });

    $("#registration-link").click(function(){
        loginForm.hide();
        registrationForm.show();
    })

    $("#confirm-r").click(function() {
        userName = $("#r-user-name").val();
        pass = CryptoJS.MD5($("#r-password").val()) + "";
        pass2 = CryptoJS.MD5($("#r-password2").val()) + "";

        if (userName.length < 5 || userName.length > 20) {
            loginMessage.text("Имя должно быть от 5 до 20 символов").show();
        } else if (!(pass === pass2) || $("#r-password").val().length == 0){
            loginMessage.text("Пароли не совподают").show() + " " + userName.length;
        } else {
            $.ajax({
                url : "user/registration",
                type : "POST",
                data: {"userName" : userName, "email" : "---", "password" : pass2},
                success : function(data) {
                    if (data.message == null) {
                        loginMessage.text("success reg " + userName).show();
                        localStorage.setItem("sessionKey", data.sessionKey);
                        window.location.reload();
                    } else {
                        loginMessage.text(data.message).show();
                    }
                }
            });

        }

    })

    function hideAllLoginForms() {
        loginForm.hide();
        registrationForm.hide();
        loginMessage.hide();
    }

})