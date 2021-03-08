$(document).ready(function () {

var userName = "Guest";
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
var chat = $("#chat-body");
hideAll();
localStorage.setItem("p2p", "false");
localStorage.removeItem("team1");
localStorage.removeItem("team2");
localStorage.removeItem("matchType");
leagueGames = "";
playersGoals = "";
isLeagueGamesShow = true;
leagueGamesId = 0;
showChat = false;
stompClient = "";
chatContent ='';

    $("#play").click(function() {
    allMenuShow();
    $("#play").hide();
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
                // if ($("#is2p")[0].checked) {
                //    localStorage.setItem("p2p", "first");
                // }

                 localStorage.setItem("team1", game.firstTeam.team.id);
                 localStorage.setItem("team2", game.secondTeam.team.id);
                 localStorage.setItem("matchType", 2);
                 if (game.playSide == 'FiRST_TEAM') {
                    localStorage.setItem("p2p", "second");
                 }
                 window.location.href = "match.html";
            });
    }

    $("#play-match").click(function() {
        localStorage.setItem("team1", $('#firstTeamSelect').val());
        localStorage.setItem("team2", $('#secondTeamSelect').val());
        localStorage.setItem("matchType", 1);
        window.location.href = "match.html";
    });

    $("#habits").click(function() {
        window.location.href = "habit.html";
    });

    $("#team-create").click(function() {
    allMenuShow();
    $("#team-create").hide();
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
            allMenuShow();
            $("#leagues").hide();
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
                $("#league-games-by-id").hide();
            } else {
                var tableContent = table[0].leagueName + '<table>  <tr> <th>Место</th> <th>Команда</th><th>Сыграно</th> <th>Выиграшей</th> <th>Ничьи</th> <th>Проиграши</th> <th>Забито</th> <th>Пропущено</th><th>Очки</th> </tr>';
                for(unit in table) {
                    tableContent += '<tr> <th>' + table[unit].position + '</th> <th>'
                    + table[unit].team.name + '</th> <th>' + table[unit].playGames + '</th>  <th>' + table[unit].wins + '</th> <th>' + table[unit].draw + '</th> <th>' + table[unit].loses + '</th> <th>'
                    + table[unit].goals + '</th> <th>' + table[unit].goalsLose + '</th> <th>' + table[unit].points + '</th> </tr>';
                }
                tableContent +='</table>';
                leagueTable.html(tableContent);
                showLeagueGames(table[0].team.leagueId);
            }
        }
    });
    });

    function showLeagueGames(leagueId) {
    if (leagueGamesId != leagueId)
    $.ajax({
        url : "match/league-games/" + leagueId,
        type : "GET",
        success : function(data) {
            leagueGamesId = leagueId;
            updatePlayerGoals(leagueId);
            $("#league-games-by-id").show();
            leagueGames = "<br>GAMES<br>"
            for (i in data) {
                leagueGames += data[i].date + ": " + data[i].firstTeamName + " " + (data[i].percentageHoldBall.x == 0 ? "-" : data[i].goals.x + ":" + data[i].goals.y) + " " + data[i].secondTeamName + "<br>"
            }
        $("#league-games-by-id").html(leagueGames);
        }
    });
    $("#league-games-by-id").html(leagueGames);
    }

    function updatePlayerGoals(leagueId) {
        $.ajax({
            url : "player/get-goals/" + leagueId,
            type : "GET",
            success : function(data) {
                playersGoals = "<br>GOALS<br>"
                n = 1
                for(i in data) {
                    d = data[i]
                    if (n <= 10)
                    playersGoals += n++ + ' ' + d.player.name + '(' + d.teamName + ') ' + d.goalsInLeague + '<br>'
                }
            }
        })
    }

    $("#league-games-by-id").click(f => {
        isLeagueGamesShow = !isLeagueGamesShow;
        if (isLeagueGamesShow) {
            showLeagueGames(leagueGamesId);
        } else {
            $("#league-games-by-id").html(playersGoals);
        }
    })

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
        allMenuShow();
        teamAdmin.hide();
        hideAll();
        countryCheck.show();
        if (localStorage.getItem("teamadm") == 0) {
        $.ajax({
            url: 'countries',
            type: 'GET',
            success: function (countries) {
                $("#team-check-message").text("Выберете команду для управления");
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
        } else {
            window.location.href="team-page.html";
        }
    });

    countryCheck.change(function() {
        teamCheck.show();
        var countryId = $('#country-check option:selected').val();
        $.ajax({
            url: 'team/get-free-by-country/' + countryId,
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
        $.ajax({
            url : "team/assign",
            type : "POST",
            data : {"teamId" : teamId},
            success : function (data) {
                    localStorage.setItem("teamadm", teamId);
                    window.location.href="team-page.html";
            }
        })
    });

    function allMenuShow() {
        $("#play").show();
        $("#team-create").show();
        $("#leagues").show();
        teamAdmin.show();
    }

    function hideAll() {
        teamForm.hide();
        message.hide();
        playSelect.hide();
        countryLeague.hide();
        leagueTable.hide();
        countryCheck.hide();
        teamCheck.hide();
        $("#league-games-by-id").hide();
        $("#chat-body").hide();
    }

    $.ajax({
        url : "counter/online",
        type : "GET",
        success : function (d) {
            if (d.usersOnline.length == 0) {
                $("#user-count").text("Юзеров онлайн нет");
            } else {
                uss = 0;
                for (u in d.usersOnline) {
                    if (uss == 0) {
                        uss = "Онлайн: " + d.usersOnline[u];
                    } else {
                        uss = ", " + d.usersOnline[u];
                    }
                }
                $("#user-count").text(uss);
            }
            $("#guest-count").text("Гостей:" + d.guestsOnline);
            $("#today-count").text("Сегодня: " + d.onlineToday);
            $("#all-count").text("Всего: " + d.onlineAllTime);
        }
    })

$("#wiki").click(f => window.location.href = "wiki.html")
$("#chat-button").click(f => {
    if (showChat) {
        $("#chat-body").hide();
    } else {
        $("#chat-body").show();
        openChat();
        //$("#chat-text").html(user.name + '' + userName)
    }
    showChat = !showChat;
})

$("#chat-send").click(f => {
    messageBody = $("#chat-message").val()
    stompClient.send("/app/chat/general/" + localStorage.getItem('sessionKey'), {}, messageBody);
})

function openChat() {
if (stompClient == "")
    initChatMessages();
    socket = new SockJS('/fifa-stomp');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
    stompClient.subscribe('/topic/chat/general/*', function (chatMessages) {
        cm = JSON.parse(chatMessages.body)
        chatContent = ' ' + convertDateToTimeBetween(cm.created) + " " + cm.fromName + " " + cm.messageBody + "&#10" + chatContent;
        $("#chat-text").html(chatContent)
    });
});
}

function initChatMessages() {
    chatContent
    $.ajax({
        url : 'chat/get-all',
        type : 'GET',
        success : cm => {
            chatContent = ' ';
            for(i in cm) {
                chatContent += convertDateToTimeBetween(cm[i].created) + ' ' + cm[i].fromName + " " + cm[i].messageBody + "&#10 "
                $("#chat-text").html(chatContent)
            }
        }
    })
}

function convertDateToTimeBetween(date) {
    s = Math.floor((new Date().getTime() - new Date(date).getTime())/1000)
    if (s > 24 * 3600) {
        return Math.floor(s / 24 * 3600) + ' дней'
    }
    m = Math.floor(s / 60);
    s -= m * 60;
    var result = s > 9 | m == 0 ? s + ' c' : '0' + s + ' c'
    if (m > 0) {
        h = Math.floor(m / 60)
        m -= h * 60;
        result = m > 9 | h == 0 ? m + ' м ' + result : '0' + m + ' м ' + result;
        if (h > 0) {
            result = h + " ч " + result
        }
    }

    return result;
}

});