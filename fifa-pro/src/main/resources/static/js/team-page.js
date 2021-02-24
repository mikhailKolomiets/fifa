$(document).ready(function () {
//todo get from somewhere
var teamId = localStorage.getItem("teamadm");
var stuff = $("#team-stuff");
var transfer = $("#transfer");
var infoShow = $("#info");
var players;
var allPlayers;
var teamDataMain;
var typeIndex = 0;
var typeValues = ["GK", "CD", "MD", "ST"];

    $.ajax({
        url : "team/get/" + teamId,
        type : "GET",
        success : function(teamData) {
            teamDataMain = teamData;
            if (teamData.length == 0) {
                localStorage.setItem("teamadm",0);
                window.location.href = "index.html";
            } else {
                updateStadiumInfo(teamData.team.stadium, teamData.team.funs);
                localStorage.setItem("teamadm",teamData.team.id);
                var money = teamData.team.money;
                $("#title").text(teamData.team.name);
                var staff = teamData.players;
                var tableContent = tableGenerate(staff, teamData.reserve, "MAIN STUFF");

                staff = teamData.reserve;
                if (staff.length > 0) {
                    tableContent += tableGenerate(staff, teamData.players, "RESERVE STUFF");
                }

                stuff.html(tableContent);

                    $.ajax({
                        url : "player/offers",
                        type : "GET",
                        success : function(data) {
                            players = data;
                            allPlayers = data;
                            transferTableGenerate();
                        }
                    });
        $.ajax({
            url : "message/team-new-amount/" + teamDataMain.team.id,
            type : "GET",
            success : function(message) {
                if (message != 0) {
                    $("#team-messages").css({"color":"white",'background-color':'green'}).text('Новых сообщений ' + message)
                }
            }
        })
            }
        }
    });

    function transferTableGenerate() {
        var tableContent = teamDataMain.team.name + ' have ' + teamDataMain.team.money + '<table>  <tr><th>NAME</th> <th id="skill" class="sort">SKILL</th> <th id="speed" class="sort">SPEED</th> <th id="age" class="sort">AGE</th> <th id="type" class="sort">POSITION</th> <th id="price" class="sort">PRICE</th> <th></th> </tr>';
                for(i = 0; i < 20; i++) {
                tableContent += '<tr><td class="playername">' + players[i].name + '</td>' +
                '<td> <input type = "hidden" class="playerid" value="'+players[i].id+'"/>' + players[i].skill + '</td> <td>' + players[i].speed + '</td>  <td>' +
                players[i].age + '</td> <td>' + players[i].type + '</td>' +
                '<td class="playerprice">' + players[i].price + '</td>';
                if (teamDataMain.team.money >= players[i].price) {
                    tableContent += ' <td> <input type="button" value="buy" class="buy"/> </td> </tr>';
                } else {
                tableContent += ' <td></td> </tr>'
            }
        }
        tableContent += '</table>';
        transfer.html(tableContent);
    }

    function offersSort(sortType) {
        switch (sortType) {
            case "skill": players.sort((pl1, pl2) => pl1.skill < pl2.skill ? 1 : -1);
                break;
            case "speed": players.sort((pl1, pl2) => pl1.speed < pl2.speed ? 1 : -1);
                break;
            case "age": players.sort((pl1, pl2) => pl1.age > pl2.age ? 1 : -1);
                break;
            case "price": players.sort((pl1, pl2) => pl1.price > pl2.price ? 1 : -1);
                break;
            case "type":
                players = allPlayers;
                players = players.filter(pl => pl.type === typeValues[typeIndex]);
//                players.sort((pl1, pl2) => {
//                return pl2.type === typeValues[typeIndex] ? 1 : -1;
//                });
                typeIndex++;
                typeIndex = typeIndex < typeValues.length ? typeIndex : 0;
                break;
        }
    }

    $(document).on('click', "[class^=sort]", function() {
        var sortType = $(this).attr("id");
        offersSort(sortType);
        transferTableGenerate();
    });

    function tableGenerate(players, change, preName) {
                var tableContent = preName + '<table>  <tr> <th>NAME</th> <th>SKILL</th> <th>SPEED</th> <th>EXP</th> <th>AGE</th> <th>POSITION</th> <th>PRICE</th> <th>CHANGE</th> <th></th> </tr>';
                for(i in players) {
                    deleteButton = players[0].reserve == true ? '<td> <input type="button" value="Продать за ' + Math.floor((players[i].skill + players[i].speed) / 10) +'" class="delsel"/> </td>' : '';
                    tableContent += '<tr><td> <input type = "text" class="playername" value="'+players[i].name+'"/></td>' +
                    '<td> <input type = "hidden" class="playerid" value="'+players[i].id+'"/>' + players[i].skill + '</td> <td>' + players[i].speed + '</td> <td>' + players[i].exp + '</td> <td>' +
                    players[i].age + '</td> <td>' + players[i].type + '</td>' +
                    '<td> <input type = "text" class="playerprice" value="' + players[i].price + '"/></td>'+
                    ' <td>' + generateSelectForReserve(change, players[i].type) + '</td>' +
                    ' <td> <input type="button" value="edit" class="butsel"/> </td> ' + deleteButton + ' </tr>'
                }
                return tableContent + '</table>';
    }

    function generateSelectForReserve(reserve, position) {
        selectContent = '-';
            for (i in reserve) {
                player = reserve[i];
                if (player.type == position) {
                    if (selectContent == "-") {
                    selectContent = '<select class="splayer"> <option value="0">замена</option>';
                    }
                    selectContent += '<option value="' + player.id + '">' + player.name + '</option>';
                }
            }
            return selectContent + '</select>';
    }

    $(document).on('click', "[class^=butsel]", function() {
        var row = $(this).parents("tr");    // Find the row
        var playerName = row.find(".playername").val(); // Find the text
        var id = row.find('.playerid').val();
        var price = row.find('.playerprice').val();

        $.ajax({
            url : "player/update",
            type : "POST",
            data: {"playerName" : playerName, "playerId" : id, "price" : price},
            success : function(teamData) {
                infoShow.text("Changed to " + playerName + " and price " + price);
            }
        });
    });

    $(document).on('click', "[class^=delsel]", function() {
        var row = $(this).parents("tr");    // Find the row
        var id = row.find('.playerid').val();
        var price = row.find('.playerprice').val();

        $.ajax({
            url : "player/delete/" + id,
            type : "DELETE",
            success : function(teamData) {
                window.location.reload();
            }
        });
    });

        $(document).on('click', "[class^=buy]", function() {
            var row = $(this).parents("tr");    // Find the row
            var playerName = row.find(".playername").text(); // Find the text
            var id = row.find('.playerid').val();
            var price = row.find('.playerprice').val();

        $.ajax({
            url : "player/buy",
            type : "POST",
            data: {"teamId" : teamId, "playerId" : id},
            success : function(teamData) {
                window.location.reload();
            }
        });
        });

    $(document).on('change', '[class^=splayer]', function() {
        var row = $(this).parents("tr");
        var id = row.find('.playerid').val();
        var resId = row.find('.splayer').val();

        $.ajax({
            url : "player/update-stuff/" + id + "/" + resId,
            type : "PUT",
            success : function(teamData) {
                window.location.reload();
            }
        });
    });

    function updateStadiumInfo(stadium, funs) {
        info = '';
        if (stadium.name == null) {
            info = 'Обычный';
        } else {
            info = stadium.name;
        }
        info += ". Вместимость:" + stadium.population + ', фанатов: ' + funs + ', билеты по: ' + stadium.ticketPrice;
        $("#stadium-info").text(info);
        $.ajax({
            url : 'match/last-home/' + teamDataMain.team.id,
            method : 'GET',
            success : function(last) {
                if (last.length != 0) {
                    $("#last-game-info").text('На последнем матче было ' + last.funs + ' болельщиков')
                }
            }
        })
    }

    $("#price-confirm").click(function(){
        $.ajax({
            url : "stadium/change-price",
            type : "POST",
            data : {price : $("#price-form").val(), stadiumId : teamDataMain.team.stadium.id},
            success : function() {
                window.location.reload()
            }
        })
    })
$("#team-messages")
    $("#team-messages").click(function(){
        $.ajax({
            url : "message/team/" + teamDataMain.team.id,
            type : "GET",
            success : function(message) {
                if (message.length == 0) {
                    $("#team-messages-show").text('Сообщений пока нет')
                } else {
                    haveNew = false;
                    messages = '';
                    for (i in message) {
                        if (message[i].readed == false) {
                            haveNew = true;
                            messages += '<b class="green">1' + message[i].body + '</b><br>'
                        } else {
                            messages += message[i].body + '<br>'
                        }
                    }
                    $("#team-messages-show").html(messages);
                    if (haveNew) {
                        $.ajax({url : "message/team/make-all-read", type : 'PUT', data : {'teamId': teamDataMain.team.id}})
                    }
                }
            }
        })
    })

});