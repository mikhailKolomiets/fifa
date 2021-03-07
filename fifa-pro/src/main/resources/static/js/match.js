$(document).ready(function () {
statusMatch = $("#match-status");
mpr = $("#main-page-ref");
ballPosition = 500;
var matchType = localStorage.getItem("p2p") == "first" ? 0 : 1;
var stompClient;
var playSide = 1;

    $.ajax({
        url: '/match/start/' + localStorage.getItem("team1") + "/" + localStorage.getItem("team2") + "/" + localStorage.getItem("matchType"),
        type: "POST",
        success: function (data) {
            $("#title-change").text(data.firstTeam.team.name + " 0:0 " + data.secondTeam.team.name);
            localStorage.setItem("matchId", data.matchId);
            statusMatch.text("Матч начался");
            $("#player-arrows-2").hide();
            if (localStorage.getItem("matchType") == 2 && localStorage.getItem("team2") == localStorage.getItem("teamadm")) {
                playSide = 2;
                $("#player-arrows-1").hide();
                $("#player-arrows-2").show();
            }
            mpr.hide();

                                socket = new SockJS('/fifa-stomp');
                                        stompClient = Stomp.over(socket);
                                        stompClient.connect({}, function (frame) {
                                            console.log('Connected: ' + frame);
                                            stompClient.subscribe('/topic/p2p/' + localStorage.getItem("matchId") , function (dto) {
                                            dto = JSON.parse(dto.body);
                                                $("#match-status").html(getLog(dto));
                                                $("#title-change").text(dto.matchDto.firstTeam.team.name + " " + dto.goalFirstTeam + ":" + dto.goalSecondTeam +
                                                    " " + dto.matchDto.secondTeam.team.name);
                                                    if (dto.step > 90 && dto.additionTime == -2) {
                                                        mpr.show();
                                                    }
                                            });
                                        });

        }
    });

    $("#action-1").click(f => makeAction(1));
    $("#action-2").click(f => makeAction(2));
    $("#action-3").click(f => makeAction(2));
    $("#action-4").click(f => makeAction(3));
    $("#action-5").click(f => makeAction(3));
    $("#action-6").click(f => makeAction(2));
    $("#action-7").click(f => makeAction(2));
    $("#action-8").click(f => makeAction(1));

    function makeAction(i) {

            if (playSide == 2 && i != 2) {
                i = i == 1 ? 3 : 1;
            }
           // changeButton(i);

            p2pgame(i);

    }


        $.ajax({
            url: '/match/all-point',
            type: "GET",
            success: function (data) {
                for(j=0; j < data.length; j++) {
                let i = j;
                let div = $("<div class='player'></div>").attr('id', `player-${i}`);
                    $("#players").append(div);

                    if (i < 11) {
                        $(`#player-${i}`).addClass('player-1');
                    } else {
                        $(`#player-${i}`).addClass('player-2');
                    }
                    $(`#player-${i}`).css({'left': data[i].x + 30, 'top': data[i].y + 30});
                }
//            $("#players").html(getLog(data));
//            $("#title-change").text(data.matchDto.firstTeam.team.name + " " + data.goalFirstTeam + ":" + data.goalSecondTeam +
//                " " + data.matchDto.secondTeam.team.name);
//                if (data.step > 90 && data.additionTime == -2) {
//                    mpr.show();
//                }

            }
        });

    function changeButton(action) {
        $.ajax({
            url: '/match/step/' + localStorage.getItem("matchId") + "/" + action,
            type: "POST",
            data : {teamSide : playSide},
            success: function (data) {
            $("#match-status").html(getLog(data));
            $("#title-change").text(data.matchDto.firstTeam.team.name + " " + data.goalFirstTeam + ":" + data.goalSecondTeam +
                " " + data.matchDto.secondTeam.team.name);
                if (data.step > 90 && data.additionTime == -2) {
                    mpr.show();
                }
            }
        });
    }

    function p2pgame(action) {
        room = localStorage.getItem("matchId");
        stompClient.send("/app/p2p/" + room, {}, action + '' + playSide);
    }

    function getLog(data) {
        showArea(data);
        result = "";


        for(i = 1; i < 6; i++) {
            if (data.log.length < i) {
                return result;
            } else {
                result = result + data.log[data.log.length - i] + "<br>"
            }
        }
        return result;
    }

    function showArea(data) {
        $("#first-team-chance-show").css('width', data.firstTeamChance * 3 + "px");
        $("#second-team-chance-show").css('width', 450 - data.secondTeamChance * 3 + "px");
        result = "";
        if (localStorage.getItem("p2p") == "second") {
            data.firstTeamBall = !data.firstTeamBall;
            data.position = data.position == 3 ? 1 : data.position == 1 ? 3 : 2;
        }

        if ((data.firstTeamBall && playSide == 1) || (!data.firstTeamBall && playSide == 2)) {
            $(".ball-go>img").addClass('green-ball').removeClass('red-ball');
            $("#player-arrows-1").show();
            $("#player-arrows-2").hide();
        } else {
            $(".ball-go>img").addClass('red-ball').removeClass('green-ball');
            $("#player-arrows-2").show();
            $("#player-arrows-1").hide();
        }
        $("#playerName").text(data.firstTeamBall ? data.firstPlayer.name : data.secondPlayer.name);
        writeStatistic(data.statisticDto, data.matchDto.firstTeam.team, data.matchDto.secondTeam.team);

        $(".ball-go").animate({left: data.ballPosition.x, top: data.ballPosition.y});
    }

    function writeStatistic(stat, firstTeam, secondTeam) {
        ft = firstTeam.name;
        st = secondTeam.name;
        fti = firstTeam.image;
        sti = secondTeam.image;
        var tableContent = "<table>"
        + '<tr> <th><img src="' + fti + '" width="165" height="137"></th><th>' + stat.goals.x + ":" + stat.goals.y + '<th><img src="' + sti + '" width="165" height="137"></th><th></tr>'
        + "<tr> <th>" + ft + "</th><th></th><th>" + st + "</th></tr>"
        + "<tr><th>" + stat.goalKick.x + "</th><th>Ударов</th><th>" + stat.goalKick.y + "</th></tr>"
        + "<tr><th>" + stat.percentageHoldBall.x + "</th><th>%BM</th><th>" + stat.percentageHoldBall.y + "</th></tr>";

        for (i in stat.goalsList) {
            goal = stat.goalsList[i];
            if (goal.team.name == ft) {
                tableContent = tableContent + "<tr><th>" + goal.player.name + "</th><th>" + goal.gameTime + "</th><th></th></tr>";
            } else {
                tableContent = tableContent + "<tr><th></th><th>" + goal.gameTime + "</th><th>" + goal.player.name + "</th></tr>";
            }
        }

        tableContent = tableContent + "</table>";
        $("#statistic").html(tableContent);
    }
})