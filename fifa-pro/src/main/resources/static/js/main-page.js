$(document).ready(function () {

var message = $("#message");
var teamForm = $("#team-form");
var playSelect = $("#play-select");
var countryLeague = $("#country-league");
var leagueTable = $("#league-table");
var lastMatchesData = "";
var matchIntervalShow;
var matchShowIndex = 0;
var leagueGame1 = $("#league-game-1");
var teamAdmin = $("#team-admin");
var countryCheck = $("#country-check");
var teamCheck = $("#team-check");
hideAll();
localStorage.setItem("p2p", "false");

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
                                        for(i=0; i < games.length; i++) {
                                            let div = $('<div></div>').attr('id', `league-game-${i+1}`);
                                            $(`#league-games`).append(div);
                                            statGame(games[i], $(`#league-game-${i+1}`));
                                        }
                                    }
                                });
                        }
                    }
         });
    });

    function statGame(game, opt) {
        ft = game.firstTeam;
        st = game.secondTeam;
        opt.text('(' + ft.team.country.countryName + ') ' + ft.team.name + ' (' + ft.leaguePosition + ") - " + st.team.name + ' (' + st.leaguePosition + ")")
            .css('color' , game.playSide == 'FiRST_TEAM' ? "red" : "")
            .click(function() {
                 if ($("#is2p")[0].checked) {
                    localStorage.setItem("p2p", "first");
                 }

                 localStorage.setItem("team1", game.firstTeam.team.id);
                 localStorage.setItem("team2", game.secondTeam.team.id);
                 if (game.playSide == 'FiRST_TEAM') {
                    localStorage.setItem("p2p", "second");
                 }
                 window.location.href = "match.html";
            });
    }

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

    $.ajax({
        url : "match/last-league-matches",
        type : "GET",
        success : function(games) {
            lastMatchesData = games;
            if (games.length > 0) {
                textLength = 50;
                result = "";
                for (i in games) {
                    result = result + games[i].firstTeamName + " " + games[i].goals.x + ":" + games[i].goals.y + " " + games[i].secondTeamName + " / "
                }
                if (result.length < textLength) {
                    $("#last-matches").text(result);
                } else {
                    i = 0;
                        matchIntervalShow = setInterval(function(){
                            if (i < result.length - textLength) {
                                txt = result.substr(i, textLength)
                            } else {
                                txt = result.substr(i, result.length) + result.substr(0, i - result.length + textLength);
                            }
                            $("#last-matches").text(txt);
                            i = i < result.length ? i + 1 : 0;
                        }, 700);
                }
            }
        }

    });

    $("#last-matches").click(function() {
        clearInterval(matchIntervalShow);
        showStatTableByIndex(matchShowIndex);
        matchShowIndex++;
        matchShowIndex = matchShowIndex > lastMatchesData.length - 1 ? 0 : matchShowIndex;
    });

    function showStatTableByIndex(index) {
                stat = lastMatchesData[index];
                        var tableContent = "<table> <tr> <th>" + stat.firstTeamName + "</th><th>" + stat.goals.x + ":" + stat.goals.y + "</th><th>" + stat.secondTeamName + "</th></tr>"
                        + "<tr><th>" + stat.goalKick.x + "</th><th>Ударов</th><th>" + stat.goalKick.y + "</th></tr>"
                        + "<tr><th>" + stat.percentageHoldBall.x + "</th><th>%BM</th><th>" + stat.percentageHoldBall.y + "</th></tr>";

                        for (i in stat.goalsList) {
                            goal = stat.goalsList[i];
                            if (goal.team.name == stat.firstTeamName) {
                                tableContent = tableContent + "<tr><th>" + goal.player.name + "</th><th>" + goal.gameTime + "</th><th></th></tr>";
                            } else {
                                tableContent = tableContent + "<tr><th></th><th>" + goal.gameTime + "</th><th>" + goal.player.name + "</th></tr>";
                            }
                        }

                        tableContent = tableContent + "</table>";
                $("#last-matches").html(tableContent);
    }

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

    teamAdmin.click(function() {
        hideAll();
        countryCheck.show();
        $.ajax({
            url: 'countries',
            type: 'GET',
            success: function (countries) {
                    countryCheck.empty();
                    countryCheck.append($('<option>', {
                        value: 0,
                        text:  'Выбрать страну...'
                    }));
                    for (item in countries) {
                        countryCheck.append($('<option>', {
                            value: countries[item].countryId,
                            text:  countries[item].countryName
                        }));
                    }
            }
        });
    });

    countryCheck.change(function() {
        teamCheck.show();
        var countryId = $('#country-check option:selected').val();
        $.ajax({
            url: 'team/get-by-country/' + countryId,
            type: 'GET',
            success: function (teams) {
                    teamCheck.empty();
                    teamCheck.append($('<option>', {
                        value: 0,
                        text:  'Выбрать команду...'
                    }));
                    for (item in teams) {
                        teamCheck.append($('<option>', {
                            value: teams[item].id,
                            text:  teams[item].name
                        }));
                    }
            }
        });
    });

    teamCheck.change(function() {
        var teamId = $('#team-check option:selected').val();
        localStorage.setItem("teamadm", teamId);
        window.location.href="team-page.html";
    });

    function hideAll() {
        teamForm.hide();
        message.hide();
        playSelect.hide();
        countryLeague.hide();
        leagueTable.hide();
        countryCheck.hide();
        teamCheck.hide();
    }

});