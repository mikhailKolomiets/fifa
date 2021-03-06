$(document).ready(function () {

var wikidata;

$.ajax({
    url : "wiki/get-all",
    type : "GET",
    success : function(data) {
        wikidata = data;
        init();
    }
}
)

function init() {
    context = "";
    for(i in wikidata) {
        context += '<a><input class="hid" type="hidden" value="' + i + '"><div class="chose">' + wikidata[i].name + '</div></a>'
    }
    $("#menu").html(context)
}

$(document).on('click', "[class^=chose]", function() {
    var div = $(this).parents("a");
    var id = div.find('.hid').val();
    $("#description").text(wikidata[id].description)
})

})