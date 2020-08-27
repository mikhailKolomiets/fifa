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
                            message.text(data[0].name + " us " + data[1].name)
                        }
                    }
         });
    });

    $("#team-create").click(function() {
        teamForm.show();
    });

    $("#sub-team").click(function() {
        var teamName = $("#team-name").val();
        $.ajax({
            url: '/team/create?teamName=' + teamName,
            type: 'POST',
            success: function (data) {
                if (data.length < 1) {
                    message.text("Команда с именем " + teamName + " существует")
                } else {
                    teamForm.hide();
                    var resultTeam = "Создана команда " + teamName;
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