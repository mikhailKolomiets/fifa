$(document).ready(function () {

var message = $("#message");
var teamForm = $("#team-form");
var playSelect = $("#play-select");
var countryLeague = $("#country-league");
var leagueTable = $("#league-table");
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
                            $('#firstTeamSelect').empty();
                            $('#secondTeamSelect').empty();
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
                                    var ft;
                                    var st;
                                        if (games.length > 0) {
                                        ft = games[0].firstTeam;
                                        st = games[0].secondTeam;
                                        $("#league-game-1").text('(' + ft.team.country.countryName + ') ' + ft.team.name + ' (' + ft.leaguePosition + ") - " + st.team.name + ' (' + st.leaguePosition + ")").click(function() {
                                                    localStorage.setItem("team1", games[0].firstTeam.team.id);
                                                    localStorage.setItem("team2", games[0].secondTeam.team.id);
                                                    window.location.href = "match.html";
                                        })
                                        }
                                        if (games.length > 1) {
                                            ft = games[1].firstTeam;
                                            st = games[1].secondTeam;
                                            $("#league-game-2").text('(' + ft.team.country.countryName + ') ' + ft.team.name + ' (' + ft.leaguePosition + ") - " + st.team.name + ' (' + st.leaguePosition + ")").click(function() {
                                                   localStorage.setItem("team1", games[1].firstTeam.team.id);
                                                   localStorage.setItem("team2", games[1].secondTeam.team.id);
                                                   window.location.href = "match.html";
                                            })
                                        }
                                       if (games.length > 2) {
                                       ft = games[2].firstTeam;
                                       st = games[2].secondTeam;
                                       $("#league-game-3").text('(' + ft.team.country.countryName + ') ' + ft.team.name + ' (' + ft.leaguePosition + ") - " + st.team.name + ' (' + st.leaguePosition + ")").click(function() {
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

        $("#leagues").click(function() {
            hideAll();
            countryLeague.show();
            leagueTable.show();
            $.ajax({
                url : "/countries",
                type : "get",
                success : function(response) {
                    countryLeague.empty();
                    countryLeague.append($('<option>', {
                        value: 0,
                        text:  'Выбрать страну...'
                    }));
                    for (item in response) {
                        message.text(item);
                        countryLeague.append($('<option>', {
                            value: response[item].countryId,
                            text:  response[item].countryName
                        }));
                    }
                }
            });
        });

    countryLeague.change(function(){
    $.ajax({
        url : "league/table-first/" + countryLeague.val(),
        type : "GET",
        success : function(table) {
            if (table.length == 0) {
                leagueTable.text($('#country-league option:selected').text() + " пока не проводит лиг");
            } else {
                var tableContent = table[0].leagueName + '<table>  <tr> <th>Место</th> <th>Команда</th><th>Сыграно</th> <th>Выиграшей</th> <th>Ничьи</th> <th>Проиграши</th> <th>Забито</th> <th>Пропущено</th><th>Очки</th> </tr>';
                for(unit in table) {
                    tableContent += '<tr> <th>' + table[unit].position + '</th> <th>'
                    + table[unit].team.name + '</th> <th>' + table[unit].playGames + '</th>  <th>' + table[unit].wins + '</th> <th>' + table[unit].draw + '</th> <th>' + table[unit].loses + '</th> <th>'
                    + table[unit].goals + '</th> <th>' + table[unit].goalsLose + '</th> <th>' + table[unit].points + '</th> </tr>';
                }
                tableContent +='</table>';
                leagueTable.html(tableContent);
            }
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
        countryLeague.hide();
        leagueTable.hide();
    }

});