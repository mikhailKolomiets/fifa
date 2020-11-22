$(document).ready(function () {
//todo get from somewhere
var teamId = localStorage.getItem("teamadm");
var stuff = $("#team-stuff");
var transfer = $("#transfer");
var infoShow = $("#info");

    $.ajax({
        url : "team/get/" + teamId,
        type : "GET",
        success : function(teamData) {
            if (teamData.length == 0) {
                stuff.text("no team");
            } else {
                var money = teamData.team.money;
                $("#title").text(teamData.team.name);
                var players = teamData.players;
                var tableContent = tableGenerate(players, teamData.reserve, "MAIN STUFF");

                players = teamData.reserve;
                if (players.length > 0) {
                    tableContent += tableGenerate(players, teamData.players, "RESERVE STUFF");
                }

                stuff.html(tableContent);

                    $.ajax({
                        url : "player/offers",
                        type : "GET",
                        success : function(players) {
                            var tableContent = teamData.team.name + ' have ' + money + '<table>  <tr> <th>NAME</th> <th>SKILL</th> <th>SPEED</th> <th>AGE</th> <th>POSITION</th> <th>PRICE</th> <th></th> </tr>';
                            for(i in players) {
                                tableContent += '<tr><td class="playername">'+players[i].name+'</td>' +
                                '<td> <input type = "hidden" class="playerid" value="'+players[i].id+'"/>' + players[i].skill + '</td> <td>' + players[i].speed + '</td>  <td>' +
                                players[i].age + '</td> <td>' + players[i].type + '</td>' +
                                '<td class="playerprice">' + players[i].price + '</td>';
                                if (money >= players[i].price) {
                                    tableContent += ' <td> <input type="button" value="buy" class="buy"/> </td> </tr>';
                                } else {
                                    tableContent += ' <td></td> </tr>'
                                }
                            }
                            tableContent += '</table>';
                            transfer.html(tableContent);
                        }
                    });
            }
        }
    });

    function tableGenerate(players, change, preName) {
                var tableContent = preName + '<table>  <tr> <th>NAME</th> <th>SKILL</th> <th>SPEED</th> <th>AGE</th> <th>POSITION</th> <th>PRICE</th> <th>CHANGE</th> <th></th> </tr>';
                for(i in players) {
                    tableContent += '<tr><td> <input type = "text" class="playername" value="'+players[i].name+'"/></td>' +
                    '<td> <input type = "hidden" class="playerid" value="'+players[i].id+'"/>' + players[i].skill + '</td> <td>' + players[i].speed + '</td>  <td>' +
                    players[i].age + '</td> <td>' + players[i].type + '</td>' +
                    '<td> <input type = "text" class="playerprice" value="' + players[i].price + '"/></td>'+
                    ' <td>' + generateSelectForReserve(change, players[i].type) + '</td>' +
                    ' <td> <input type="button" value="edit" class="butsel"/> </td> </tr>'
                }
                return tableContent + '</table>';
    }

    function generateSelectForReserve(reserve, position) {
        selectContent = '-';
            for (i in reserve) {
                player = reserve[i];
                if (player.type == position) {
                    if (selectContent == "-") {
                    selectContent = '<select class="splayer"> <option value="0">change to</option>';
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

});