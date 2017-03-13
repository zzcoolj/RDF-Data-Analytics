// http://www.d3noob.org/2014/02/making-bar-chart-in-d3js.html

var margin = {top: 20, right: 20, bottom: 70, left: 60},
    width = 1.5*(600 - margin.left - margin.right),
    height = 1.5*(300 - margin.top - margin.bottom);

// Parse the date / time
//var parseDate = d3.time.format("%Y-%m").parse;

var x = d3.scale.ordinal().rangeRoundBands([0, width], .15);

var y = d3.scale.linear().range([height, 0]);

//var xAxis = d3.svg.axis()
//    .scale(x)
//    .orient("bottom")
//    .tickFormat(d3.time.format("%Y-%m"));
var xAxis = d3.svg.axis()
    .scale(x)
    .orient("bottom")
    .ticks(10);

var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left")
    .ticks(10);

var svg = d3.select("#bar_chart").append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform",
    "translate(" + margin.left + "," + margin.top + ")");


var typeName = getQueryVariable("type");
if(typeName != "AllSubjects") {
    var csvFileName = "data/bar-data_type_" + typeName + ".csv";
} else {
    var csvFileName = "data/bar-data.csv";
}

//d3.csv("bar-data.csv", function (error, data) {
d3.csv(csvFileName, function (error, data) {

    data.forEach(function (d) {
        //d.date = parseDate(d.date);
        d.date = d.number_of_subjects;
        d.value = +d.number_of_properties;
    });

    x.domain(data.map(function (d) {
        return d.date;
    }));
    y.domain([0, d3.max(data, function (d) {
        return d.value;
    })]);

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis)
        .selectAll("text")
        .style("text-anchor", "end")
        .attr("dx", "0.3em")
        .attr("dy", "0.6em");

    svg.append("text")
        .attr("class", "x label")
        .attr("text-anchor", "end")
        .attr("x", width + 20)
        .attr("y", height - 6)
        .attr("font-size", "11px")
        .text("#properties");

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("transform", "rotate(-90)")
        .attr("y", 6)
        .attr("dy", ".31em")
        .style("text-anchor", "end")
        .text("Number of subjects");

    svg.selectAll("bar")
        .data(data)
        .enter()
        .append("rect")
        .style("fill", "steelblue")
        .attr("x", function (d) {
            return x(d.date);
        })
        .attr("width", x.rangeBand())
        .attr("y", function (d) {
            return y(d.value);
        })
        .attr("height", function (d) {
            return height - y(d.value);
        });

    svg.selectAll("bar")
        .data(data)
        .enter()
        .append("text")
        .attr("class", "bartext")
        .attr("text-anchor", "middle")
        .attr("fill", "steelblue")
        .attr("font-size", "11px")
        .attr("x", function (d) {
            return x(d.date) + 26;
        })
        .attr("y", function (d) {
            return y(d.value) - 5;
        })
        .text(function(d){
            return d.value;
        });

});