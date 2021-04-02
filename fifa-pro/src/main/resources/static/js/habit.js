$(document).ready(function () {

var user;
var habitId;

setInterval(f => window.location.reload(), 300000)

function whenTimeout() {
window.location.reload();
}

$("#delete-habit-confirm").hide();

$.ajax({
    url : "user/check",
            type : "GET",
            data: {"key" : localStorage.getItem("sessionKey")},
            success : function(data) {
                if (data.length == 0) {
                    window.location.href = "index.html";
                }
                user = data;
                $.ajax({
                    url : "habit/info/" + data.user.id,
                    type : "GET",
                    success : function(habits) {
                    $("#habits-info").text(habits);
                    }
                })
                $.ajax({
                    url : "habit/get-all/" + data.user.id,
                    type : "GET",
                    success : function(habits) {
                    var result = '';
                    for(i in habits) {
                        result += '<a><input class="hid" type="hidden" value="'+habits[i].id+'">'+habits[i].name+'<button class="up">Update</button>'+
                        '<button class="rev">Reverse</button> HI:' + timeShow(habits[i].hiSeconds) + ' <button class="del">delete</button> </a><br>';
                    }
                    $("#habits-control").html(result);
                    }
                })
            }
});

$(document).on('click', "[class^=up]", function() {
    var div = $(this).parents("a");
    var id = div.find('.hid').val();

    $.ajax({
            url : "habit/reset-time",
            type : "POST",
            data: {"habitId" : id},
            success : function(data) {
                window.location.reload();
            }
    });
});

$(document).on('click', "[class^=rev]", function() {
    var div = $(this).parents("a");
    var id = div.find('.hid').val();

    $.ajax({
            url : "habit/back-time",
            type : "POST",
            data: {"habitId" : id},
            success : function(data) {
                window.location.reload();
            }
    });
});

$(document).on('click', "[class^=del]", function() {
    var div = $(this).parents("a");
    habitId = div.find('.hid').val();
    $("#delete-habit-confirm").show();
});

$("#delete-habit-button").click(function() {
    pass = CryptoJS.MD5($("#user-password").val()) + "";
        $.ajax({
                url : "habit/delete",
                type : "DELETE",
                data: {"habitId" : habitId, "password" : pass},
                success : function(data) {
                    if (data.id == habitId) {
                        window.location.reload();
                    } else {
                        $("#habits-info").text("something wrong. maybe password? ")
                    }
                }
        });
});

$("#create-habit").click(function() {
    $.ajax({
        url : "habit/create",
        type : "POST",
        data : {"userId" : user.user.id, "name" : $("#new-habit").val()},
        success : function(data) {
            window.location.reload();
        }
    })
})

function timeShow(seconds) {
if (seconds == 0) {
    return "0s"
}
result = seconds % 60 + "s";
    if (seconds / 60 < 1) {
        return result;
    }
seconds -= seconds % 60;
seconds /= 60;
result = seconds % 60 + "m:" + result;
if (seconds / 60 < 1) {
    return result;
}
seconds -= seconds % 60;
seconds /= 60;
result = seconds % 24 + "h:" + result;
if (seconds / 24 < 1) {
    return result;
}
seconds -= seconds % 24;
seconds /= 24;
return seconds + "d:" + result;
}

})