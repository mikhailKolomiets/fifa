$(document).ready(function () {

var message = $("#message");
var teamForm = $("#team-form");
teamForm.hide();

    $("#play").click(function() {
        $.ajax({
                    url: '/team/get-team',
                    success: function (data) {
                        $("#message").text(data.length);
                        if (data.length < 2) {
                            message.text("Для игры надо 2 команды а не " + data.length)
                        } else {
                            localStorage.setItem("team1",data[1].id);
                            localStorage.setItem("team2", data[0].id);
                            message.html("<a href=\"match.html\">" + data[0].name + " us " + data[1].name + "</a>")
                        }
                    }
         });
    });

    $("#team-create").click(function() {
        teamForm.show();
        $.ajax({
            url : "/countries",
            type : "get",
            success : function(response) {
                $('#countryName').empty();
                $('#countryName').append($('<option>', {
                    value: 0,
                    text:  'Выбрать страну...'
                }));
                for (item in response) {
                    message.text(item);
                    $('#countryName').append($('<option>', {
                        value: response[item].countryName,
                        text:  response[item].countryName
                    }));
                }
            },
            error : function(e) {
            }
        });
    });

    $("#sub-team").click(function() {

        $.ajax({
            url: '/team/create',
            type: 'POST',
            data: JSON.stringify({teamName : $("#team-name").val(), countryName : $("#countryName").val()}),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function (data) {
                if (data.length < 1) {
                    message.text("Команда с именем " + teamName + " существует")
                } else {
                    teamForm.hide();
                    var resultTeam = "Создана команда " + data.team.name;
                    for (i = 0; i < data.players.length; i++) {
                        resultTeam = resultTeam + "<br>" + data.players[i].name + " позиция " + data.players[i].type + ", возраст " + data.players[i].age +
                        ", скорость " + data.players[i].speed + ", умения " + data.players[i].skill
                    }
                    $("#new-team").html(resultTeam);
                }
            }
        });
    });

});