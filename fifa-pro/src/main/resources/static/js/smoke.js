$(document).ready(function(){

$.ajax({
    url : 'smoke/get/' + localStorage.getItem('sessionKey'),
    type : 'GET',
    success : function(account) {
        updateAccountData(account)
    }
})

$("#account-create").click(f => {
    sig = $('#s-pre').val();
    sig = sig > 0 ? 0 - sig : sig;
    $.ajax({
        url : 'smoke/create/' + localStorage.getItem('sessionKey'),
        type : 'POST',
        data : JSON.stringify({
            priceForOne : $('#s-cost').val(),
            commonTime : $('#s-time').val() * 60,
            type : $('#type').val(),
            cigarettes : sig
        }),
        contentType: "application/json; charset=utf-8",
        success : function(data) {
            updateAccountData(data)
        }
    })
})

$('#update-time').click(f => {
        $.ajax({
            url : 'smoke/update-time/' + localStorage.getItem('sessionKey'),
            type : 'PUT',
            success : function(data) {
                updateAccountData(data)
            }
        })
})

$('#reverse-time').click(f => {
    $.ajax({
        url : 'smoke/reverse-time/' + localStorage.getItem('sessionKey'),
        type : 'PUT',
        success : function(data) {
            updateAccountData(data)
        }
    })
})

function updateAccountData(account) {
        if (account.length != 0) {
            $("#sleep").hide()
            $('#enter').hide();
            $('#smoke-account').show()
            cigarettes = account.cigarettes;
            if (account.cigarettes < 0) {
                $('#cigarettes').text('To start leave ' + (Math.abs(account.cigarettes)) + ' cigarettes')
            } else {
                $('#cigarettes').text('You smoked ' + account.cigarettes + ' cigarettes')
                $('#money-lose').text('Money spend: ' + showMoney(account.moneyLose))
                $('#money-save').text('Money save: ' + showMoney(account.moneySaves))
            }
            $("#money-got").text('If smoke you get: ' + showMoney(account.moneyByLast))
            $("#time-to-next").text('Time to next: ' + showTime(account.secondsNext))
            $("#time-to-last").text("Last time: " + showTime(account.secondsLast))
            if (account.secondsNext < 0 && account.secondsLast / (account.secondsLast + account.secondsNext) > 2) {
                $("#sleep").show()
            }
        } else {
            $('#smoke-account').hide()
            $('#enter').show()
        }
}

$("#sleep-yes").click(f => {
    $.ajax({
        url : 'smoke/after-sleep/' + localStorage.getItem('sessionKey'),
        type : 'PUT',
        success : function(data) {
            updateAccountData(data)
        }
    })
})

    function showTime(s) {
        var result;
        d = Math.floor(s / 24 / 3600)
        h = Math.floor(s / 3600)
        m = Math.floor(s / 60)
        if (s < 0) {
            result = 'OK';
        } else if (d > 0) {
            h = h - d * 24;
            h = h > 9 ? h : "0" + h
            result = d + " D " + h + " H"
        } else if (h > 0) {
            m = m - h * 60;
            m = m > 9 ? m : "0" + m
            result = h + " H " + m + " M"
        } else {
            s = s - m * 60;
            s = s > 9 ? s : "0" + s
            result = m + " M " + s + " S"
        }

        return result;
    }

    function showMoney(m) {
        x = m < 0 ? '-' : ''
        m = Math.abs(m)
        c = Math.floor(m / 100)
        a = Math.floor(c / 1000000)
        b = Math.floor(c / 1000)
        d = m - c * 100;
        d = d > 9 ? d : '0' + d

        if (c > 1000) {
            c = c - b * 1000;
            c = c > 9 ? '0' + c : '00' + c
        }
        if (a > 0) {
            b = b - a * 1000;
            if (b < 100) {
                b = b > 9 ? "0" + b : "00" + b
            }
        } else {
            a = '';
            b = b > 0 ? b : ''
        }
        return x + a + " " + b + " " + c + "." + d
    }
})