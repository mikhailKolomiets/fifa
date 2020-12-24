$(document).ready(function () {

var user;

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
                        result += '<a><input class="hid" type="hidden" value="'+habits[i].id+'">'+habits[i].name+'<button class="up">Update</button></a><br>';
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

})