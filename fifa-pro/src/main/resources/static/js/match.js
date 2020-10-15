$(document).ready(function () {
action1 = $("#action1");
action2 = $("#action2");
action3 = $("#action3");
statusMatch = $("#match-status");
mpr = $("#main-page-ref");
ballPosition = 500;

    $.ajax({
        url: '/match/start/' + localStorage.getItem("team1") + "/" + localStorage.getItem("team2"),
        type: "POST",
        success: function (data) {
            $("#title-change").text(data.firstTeam.team.name + " 0:0 " + data.secondTeam.team.name);
            localStorage.setItem("matchId", data.matchId);
            statusMatch.text("Матч начался");
                            $("#show-area").text(". . . 0 -> . . .");
                            action1.text('Атаковать');
                            action2.text('Укрепиться в центре');
                            action3.text('Пас назад');
                            mpr.hide()
        }
    });

    action1.click(function() {
                            $.ajax({
                                url: '/match/step/' + localStorage.getItem("matchId") + "/1",
                                type: "POST",
                                success: function (data) {
                                    $("#match-status").html(getLog(data));
                                    $("#title-change").text(data.matchDto.firstTeam.team.name + " " + data.goalFirstTeam + ":" + data.goalSecondTeam +
                                        " " + data.matchDto.secondTeam.team.name);
                                        if (data.step > 90 && data.additionTime == -2) {
                                            mpr.show();
                                        }
                                }
                            });
    });

    action2.click(function() {
                            $.ajax({
                                url: '/match/step/' + localStorage.getItem("matchId") + "/2",
                                type: "POST",
                                success: function (data) {
                                    $("#match-status").html(getLog(data));
                                    $("#title-change").text(data.matchDto.firstTeam.team.name + " " + data.goalFirstTeam + ":" + data.goalSecondTeam +
                                         " " + data.matchDto.secondTeam.team.name);
                                    if (data.step > 90 && data.additionTime == -2) {
                                         mpr.show();
                                    }
                                }
                            });
    });

    action3.click(function() {
                            $.ajax({
                                url: '/match/step/' + localStorage.getItem("matchId") + "/3",
                                type: "POST",
                                success: function (data) {
                                    $("#match-status").html(getLog(data));
                                    $("#title-change").text(data.matchDto.firstTeam.team.name + " " + data.goalFirstTeam + ":" + data.goalSecondTeam +
                                        " " + data.matchDto.secondTeam.team.name);
                                    if (data.step > 90 && data.additionTime == -2) {
                                         mpr.show();
                                    }
                                }
                            });
    });

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
        result = "";

        if (data.firstTeamBall) {
        $("#playerName").text(data.firstPlayer.name);
            if (data.position == 2) {
            //ballPosition = 500;
                result = ". . . 0 -> . . .";
                action1.text('Атаковать');
                action2.text('Укрепиться в центре');
                action3.text('Пас назад');
            } else if (data.position == 1) {
                //ballPosition = 200;
                result = "0 -> . . . . . .";
                action1.text('Выбить в центр');
                action2.text('Пас');
                action3.text('Пас');
            } else if (data.position == 3) {
            //ballPosition = 950;
                result = ". . . . . . 0 ->";
                action1.text('Удар по воротам');
                action2.text('Подойти поближе');
                action3.text('Пас назад');
            }
        } else {
        $("#playerName").text(data.secondPlayer.name);
            if (data.position == 2) {
            //ballPosition = 550;
                   result = ". . . <- 0 . . .";
                action1.text('Не пропускать');
                action2.text('Пытаться забрать мяч');
                action3.text('Блокировать передачи');
            } else if (data.position == 1) {
            //ballPosition = 100;
                   result = "<- 0 . . . . . .";
                action1.text('Усиление вратаря');
                action2.text('Отбор');
                action3.text('Блок передач');
            } else if (data.position == 3) {
            //ballPosition = 850;
                   result = ". . . . . . <- 0";
                action1.text('Не давать вынос');
                action2.text('Отбор');
                action3.text('Отбор');
            }
        }
    writeStatistic(data.statisticDto, data.matchDto.firstTeam.team.name, data.matchDto.secondTeam.team.name);

        $(".ball-go").animate({left: data.ballPosition.x, top: data.ballPosition.y});
        $("#show-area").text(result);
    }

    function writeStatistic(stat, ft, st) {
        var tableContent = "<table> <tr> <th>" + ft + "</th><th>" + stat.goals.x + ":" + stat.goals.y + "</th><th>" + st + "</th></tr>"
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