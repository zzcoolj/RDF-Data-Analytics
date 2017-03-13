/**
 * Created by zzcoolj on 16/6/7.
 */
var types = new Array();

d3.csv("../bar_chart/pie-data.csv", function (error, data) {
    data.forEach(function (d) {
        types.push(getShorterName(d.typeName));
    });
    types.forEach(generateHyperlink);

    var typeName = getQueryVariable("type");
    if(typeName == "Null") {
        typeName = "None";
    }
    document.getElementById("navigator_bar_" + typeName).style.color = "black";
    document.getElementById("navigator_bar_" + typeName).style.fontWeight = "bold";
});

function generateHyperlink(type) {
    var a = document.createElement('a');
    var linkText = document.createTextNode(" - " + type);
    a.appendChild(linkText);
    a.id = "navigator_bar_" + type;
    a.style.textDecoration = "none";
    a.style.color = "#ADADAD";
    if(type != "None") {
        a.href = "../sequences_sunburst/index.html?type=" + type;
    } else {
        a.href = "../sequences_sunburst/index.html?type=Null";
    }
    document.getElementById('navigator_bar').appendChild(a);
}

function getShorterName(fullPropertyName) {
    //return fullPropertyName;
    return fullPropertyName.split(/[^A-Za-z]/).pop();
}

/*
 Usage:
 http://www.example.com/index.php?type=Article
 getQueryVariable("type");
 */
function getQueryVariable(variable)
{
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if(pair[0] == variable){return pair[1];}
    }
    return(false);
}