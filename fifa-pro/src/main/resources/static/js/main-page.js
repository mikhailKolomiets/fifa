$(document).ready(function () {

var message = $("#message");
var teamForm = $("#team-form");
var playSelect = $("#play-select");
hideAll();

    $("#play").click(function() {
        $.ajax({
                    url: '/team/get-team',
                    success: function (data) {
                        $("#message").text(data.length);
                        if (data.length < 2) {
                            message.text("Для игры надо 2 команды а не " + data.length)
                        } else {
                            hideAll();
                            playSelect.show();
                            for (team in data) {
                                $('#firstTeamSelect').append($('<option>', {
                                    value: data[team].id,
                                    text:  data[team].name
                                }));
                                $('#secondTeamSelect').append($('<option>', {
                                    value: data[team].id,
                                    text:  data[team].name
                                }));
                                //$("#league-games").text("play league")
                            }
                                $.ajax({
                                    url: 'match/get-for-league-games',
                                    success: function (games) {
                                        if (games.length > 0) {
                                        $("#league-game-1").text(games[0].firstTeam.team.name + " - " + games[0].secondTeam.team.name).click(function() {
                                                    localStorage.setItem("team1", games[0].firstTeam.team.id);
                                                    localStorage.setItem("team2", games[0].secondTeam.team.id);
                                                    window.location.href = "match.html";
                                        })
                                        }
                                        if (games.length > 1) {
                                            $("#league-game-2").text(games[1].firstTeam.team.name + " - " + games[1].secondTeam.team.name).click(function() {
                                                   localStorage.setItem("team1", games[1].firstTeam.team.id);
                                                    localStorage.setItem("team2", games[1].secondTeam.team.id);
                                                    window.location.href = "match.html";
                                        })
                                        }
                                       if (games.length > 2) {
                                         $("#league-game-3").text(games[2].firstTeam.team.name + " - " + games[2].secondTeam.team.name).click(function() {
                                                     localStorage.setItem("team1", games[2].firstTeam.team.id);
                                                     localStorage.setItem("team2", games[2].secondTeam.team.id);
                                                     window.location.href = "match.html";
                                         })
                                         }
                                    }
                                });
                        }
                    }
         });
    });

    $("#play-match").click(function() {
        localStorage.setItem("team1", $('#firstTeamSelect').val());
        localStorage.setItem("team2", $('#secondTeamSelect').val());
        window.location.href = "match.html";
    });

    $("#team-create").click(function() {
        hideAll();
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

    function hideAll() {
        teamForm.hide();
        message.hide();
        playSelect.hide();
    }

});