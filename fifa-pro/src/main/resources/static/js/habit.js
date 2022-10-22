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
                    setInterval( f =>
                    {
                    var result = '';
                    dailyTime = 0;
                    for(i in habits) {
                        result += '<a><input class="hid" type="hidden" value="'+habits[i].id+'">'+habits[i].name+'<button class="up">Update</button>'+
                        '<button class="rev">Reverse</button> [' + timeShow(habits[i].hiSeconds + dailyTime++) + '] <button class="del">delete</button> </a><br><br>';
                    }
                    $("#habits-control").html(result);}, 1000
                    )
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
    return " - "
}
s = addZero(seconds % 60) + "s";
    if (seconds / 60 < 1) {
        return s;
    }
seconds -= seconds % 60;
seconds /= 60;
m = addZero(seconds % 60) + "m";
if (seconds / 60 < 1) {
    return m + ' : ' + s;
}
seconds -= seconds % 60;
seconds /= 60;
h = addZero(seconds % 24) + "h";
if (seconds / 24 < 1) {
    return h + " : " + m;
}
seconds -= seconds % 24;
seconds /= 24;
return seconds + "d : " + h;
}

function addZero(data) {
data += '';
if (data.length == 1) {
    return '0' + data;
}
return data;
}

})