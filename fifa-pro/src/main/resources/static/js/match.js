$(document).ready(function () {
action1 = $("#action1");
action2 = $("#action2");
action3 = $("#action3");

    $.ajax({
        url: '/match/start/' + localStorage.getItem("team1") + "/" + localStorage.getItem("team2"),
        type: "POST",
        success: function (data) {
            $("#title-change").text(data.firstTeam.team.name + " 0:0 " + data.secondTeam.team.name);
            localStorage.setItem("matchId", data.matchId);
            $("#match-status").text("Матч начался");
        }
    });

    action1.click(function() {
                            $.ajax({
                                url: '/match/step/' + localStorage.getItem("matchId") + "/1",
                                type: "POST",
                                success: function (data) {
                                    //$("#match-status").text("Матч начался");
                                    $("#match-status").text(getLog(data));
                                }
                            });
    });

    action2.click(function() {
                            $.ajax({
                                url: '/match/step/' + localStorage.getItem("matchId") + "/2",
                                type: "POST",
                                success: function (data) {
                                    //$("#match-status").text("Матч начался");
                                    $("#match-status").text(getLog(data));
                                }
                            });
    });

    action3.click(function() {
                            $.ajax({
                                url: '/match/step/' + localStorage.getItem("matchId") + "/3",
                                type: "POST",
                                success: function (data) {
                                    //$("#match-status").text("Матч начался");
                                    $("#match-status").text(getLog(data));
                                }
                            });
    });

    function getLog(data) {
        return data.lastStepLog;
    }
})