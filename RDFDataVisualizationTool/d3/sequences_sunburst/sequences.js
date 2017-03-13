// Dimensions of sunburst.
var width = 950;
var height = 600;
var radius = Math.min(width, height) / 2;

// Breadcrumb dimensions: width, height, spacing, width of tip/tail.
var b = {
    w: 75, h: 30, s: 3, t: 10
};

var typeName = getQueryVariable("type");
if(typeName != "AllSubjects") {
    var orderedPropertiesCsvFileName = "data/ordered_properties_type_" + typeName + ".csv";
    var propertyFrequencyCsvFileName = "data/property_frequency_type_" + typeName + ".csv";
    var d3SequencesSunburstCsvFileName = "data/d3_sequences_sunburst_type_" + typeName + ".csv";
} else {
    var orderedPropertiesCsvFileName = "data/ordered_properties.csv";
    var propertyFrequencyCsvFileName = "data/property_frequency.csv";
    var d3SequencesSunburstCsvFileName = "data/d3_sequences_sunburst.csv";
}


// When showing sequence sunburst graph of a specific type, there are some properties whose frequency are 0, cause properties here are properties of all kinds of subjects.
// Number of properties whose frequency is not 0.
var numPropertiesFrequencyNotZero = 0;
// Mapping of step names(e.g. p1) to corresponding frequency.
var pn_frequency = {
    "end": ""
};

//d3.text("../../exploratory-rdf-analytics/data/property_frequency.csv", function(text) {
d3.text(propertyFrequencyCsvFileName, function(text) {
    var csv = d3.csv.parseRows(text);
    var numProperties = csv[0];
    for(var i=0; i<numProperties; i++) {
        var pn_frequency_pair = csv[1+i].toString().split(":\t");
        var pn = pn_frequency_pair[0];
        var frequency = pn_frequency_pair[1];
        pn_frequency[pn] = frequency;
        if(frequency != 0) {
            numPropertiesFrequencyNotZero++;
        }
    }
    //alert(numPropertiesFrequencyNotZero);
});


// Mapping of step names to colors.
var colors = {
    //e.g. "p1": "#5687d1",
    "end": "#bbbbbb",
    "noProperty": "#ffffff"
};

// Mapping of step names(e.g. p1) to corresponding property names.
var pn_propertyName = {
    "end": "end"
};

// When SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY is 1, read group_by_properties_result.txt file to generate a key-value array for keys like "p1, p2,..., pn".
// When SQL_COUNT_DISTINCT_PROPERTIES_STRATEGY is not 1, read ordered_properties.txt file to generate a key-value array for keys like "p1, p2,..., pn".
//d3.text("../../exploratory-rdf-analytics/data/ordered_properties.csv", function(text) {
d3.text(orderedPropertiesCsvFileName, function(text) {
    var csv = d3.csv.parseRows(text);
    var numProperties = csv[0];
    // Show all properties even its frequency is 0
    //for(var i=0; i<numProperties; i++) {
    // Show properties whose frequency is not 0
    for(var i=0; i<numPropertiesFrequencyNotZero; i++) {
        var pn_propertyName_pair = csv[1+i].toString().split(":\t");
        var pn = pn_propertyName_pair[0];
        //var propertyName = pn_propertyName_pair[1];
        propertyName = getShorterName(pn_propertyName_pair[1]);
        if((typeName != "AllSubjects") && (propertyName == "type")) {
            continue;
        }
        pn_propertyName[pn] = propertyName;
        // Add key-value(e.g. p1: #5687d1) pairs in colors
        colors[pn] = getRandomColor();
    }
});


// Total size of all segments; we set this later, after loading the data.
var totalSize = 0;

var vis = d3.select("#chart").append("svg:svg")
    .attr("width", width)
    .attr("height", height)
    .append("svg:g")
    .attr("id", "container")
    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

var partition = d3.layout.partition()
    .size([2 * Math.PI, radius * radius])
    .value(function(d) { 
        return d.size; 
    });

var arc = d3.svg.arc()
    .startAngle(function(d) { return d.x; })
    .endAngle(function(d) { return d.x + d.dx; })
    .innerRadius(function(d) { return Math.sqrt(d.y); })
    .outerRadius(function(d) { return Math.sqrt(d.y + d.dy); });

// Use d3.text and d3.csv.parseRows so that we do not need to have a header
// row, and can receive the csv as an array of arrays.
//d3.text("d3_sequences_sunburst.csv", function(text) {
d3.text(d3SequencesSunburstCsvFileName, function(text) {
    var csv = d3.csv.parseRows(text);
    var json = buildHierarchy(csv);
    createVisualization(json);
});

// Main function to draw and set up the visualization, once we have the data.
function createVisualization(json) {

    // Basic setup of page elements.
    initializeBreadcrumbTrail();
    drawLegend();
    d3.select("#togglelegend").on("click", toggleLegend);

    // Bounding circle underneath the sunburst, to make it easier to detect
    // when the mouse leaves the parent g.
    vis.append("svg:circle")
        .attr("r", radius)
        .style("opacity", 0);

    // For efficiency, filter nodes to keep only those large enough to see.
    var nodes = partition.nodes(json)
        .filter(function(d) {
            return (d.dx > 0.005); // 0.005 radians = 0.29 degrees
        });

    var path = vis.data([json]).selectAll("path")
        .data(nodes)
        .enter().append("svg:path")
        .attr("display", function(d) { return d.depth ? null : "none"; })
        .attr("d", arc)
        .attr("fill-rule", "evenodd")
        .style("fill", function(d) { return colors[d.name]; })
        .style("opacity", 1)
        .on("mouseover", mouseover);

    // Add the mouseleave handler to the bounding circle.
    d3.select("#container").on("mouseleave", mouseleave);

    // Get total size of the tree = value of root node from partition.
    totalSize = path.node().__data__.value;
};

// Fade all but the current sequence, and show it in the breadcrumb trail.
function mouseover(d) {

    var percentage = (100 * d.value / totalSize).toPrecision(3);
    var percentageString = percentage + "%";
    if (percentage < 0.1) {
        percentageString = "< 0.1%";
    }

    var countString = "\n(" + d.value + ")";
    d3.select("#count")
        .text(countString);

    d3.select("#percentage")
        .text(percentageString);

    d3.select("#explanation")
        .style("visibility", "");

    var sequenceArray = getAncestors(d);
    // Remove the "noProperty" sequence in the breadcrumb trail.
    sequenceArray = sequenceArray.filter(removeNoPropertyFromArray);

    updateBreadcrumbs(sequenceArray, percentageString);

    // Fade all the segments.
    d3.selectAll("path")
        .style("opacity", 0.3);

    // Then highlight only those that are an ancestor of the current segment.
    vis.selectAll("path")
        .filter(function(node) {
            return (sequenceArray.indexOf(node) >= 0);
        })
        .style("opacity", 1);
}

// Restore everything to full opacity when moving off the visualization.
function mouseleave(d) {

    // Hide the breadcrumb trail
    d3.select("#trail")
        .style("visibility", "hidden");

    // Deactivate all segments during transition.
    d3.selectAll("path").on("mouseover", null);

    // Transition each segment to full opacity and then reactivate it.
    d3.selectAll("path")
        .transition()
        .duration(1000)
        .style("opacity", 1)
        .each("end", function() {
            d3.select(this).on("mouseover", mouseover);
        });

    d3.select("#explanation")
        .style("visibility", "hidden");
}

// Given a node in a partition layout, return an array of all of its ancestor
// nodes, highest first, but excluding the root.
function getAncestors(node) {
    var path = [];
    var current = node;
    while (current.parent) {
        path.unshift(current);
        current = current.parent;
    }
    return path;
}

function initializeBreadcrumbTrail() {
    // Add the svg area.
    var trail = d3.select("#sequence").append("svg:svg")
        .attr("width", width)
        .attr("height", 50)
        .attr("id", "trail");
    // Add the label at the end, for the percentage.
    trail.append("svg:text")
        .attr("id", "endlabel")
        .style("fill", "#000");
}

// Generate a string that describes the points of a breadcrumb polygon.
function breadcrumbPoints(d, i) {
    var points = [];
    points.push("0,0");
    points.push(b.w + ",0");
    points.push(b.w + b.t + "," + (b.h / 2));
    points.push(b.w + "," + b.h);
    points.push("0," + b.h);
    if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
        points.push(b.t + "," + (b.h / 2));
    }
    return points.join(" ");
}

// Update the breadcrumb trail to show the current sequence and percentage.
function updateBreadcrumbs(nodeArray, percentageString) {

    // Data join; key function combines name and depth (= position in sequence).
    var g = d3.select("#trail")
        .selectAll("g")
        .data(nodeArray, function(d) { return d.name + d.depth; });

    // Add breadcrumb and label for entering nodes.
    var entering = g.enter().append("svg:g");

    entering.append("svg:polygon")
        .attr("points", breadcrumbPoints)
        .style("fill", function(d) { return colors[d.name]; });

    entering.append("svg:text")
        .attr("x", (b.w + b.t) / 2)
        .attr("y", b.h / 2)
        .attr("dy", "0.35em")
        .attr("text-anchor", "middle")
        .text(function(d) { return pn_propertyName[d.name]; });
        //.text(function(d) { return d.name; });

    // Set position for entering and updating nodes.
    g.attr("transform", function(d, i) {
        return "translate(" + i * (b.w + b.s) + ", 0)";
    });

    // Remove exiting nodes.
    g.exit().remove();

    // Now move and update the percentage at the end.
    d3.select("#trail").select("#endlabel")
        .attr("x", (nodeArray.length + 0.5) * (b.w + b.s))
        .attr("y", b.h / 2)
        .attr("dy", "0.35em")
        .attr("text-anchor", "middle")
        .text(percentageString);

    // Make the breadcrumb trail visible, if it's hidden.
    d3.select("#trail")
        .style("visibility", "");

}

function drawLegend() {

    // Dimensions of legend item: width, height, spacing, radius of rounded rect.
    var li = {
        w: 110, h: 30, s: 3, r: 3
    };

    // Remove "end" and "noProperty" in the legend
    var colorsFiltered = removeEndAndNoPropertyFromColors(colors);

    var legend = d3.select("#legend").append("svg:svg")
        .attr("width", li.w)
        .attr("height", d3.keys(colorsFiltered).length * (li.h + li.s));

    var g = legend.selectAll("g")
        .data(d3.entries(colorsFiltered))
        .enter().append("svg:g")
        .attr("transform", function(d, i) {
            return "translate(0," + i * (li.h + li.s) + ")";
        });

    g.append("svg:rect")
        .attr("rx", li.r)
        .attr("ry", li.r)
        .attr("width", li.w)
        .attr("height", li.h)
        .style("fill", function(d) { return d.value; });

    // TODO Legend part
    g.append("svg:text")
        .attr("x", li.w / 12)
        .attr("y", li.h / 2)
        .attr("dy", "0.35em")
        .attr("text-anchor", "left")
        .text(function(d) { return pn_propertyName[d.key] + " (" + pn_frequency[d.key] + ")"; });
        //.text(function(d) { return d.key + pn_propertyName[d.key]; });
}

function toggleLegend() {
    var legend = d3.select("#legend");
    if (legend.style("visibility") == "hidden") {
        legend.style("visibility", "");
    } else {
        legend.style("visibility", "hidden");
    }
}

// Take a 2-column CSV and transform it into a hierarchical structure suitable
// for a partition layout. The first column is a sequence of step names, from
// root to leaf, separated by hyphens. The second column is a count of how 
// often that sequence occurred.
function buildHierarchy(csv) {
    var root = {"name": "root", "children": []};
    for (var i = 0; i < csv.length; i++) {
        var sequence = csv[i][0];
        var size = +csv[i][1];
        if (isNaN(size)) { // e.g. if this is a header row
            continue;
        }
        var parts = sequence.split("-");
        var currentNode = root;
        for (var j = 0; j < parts.length; j++) {
            var children = currentNode["children"];
            var nodeName = parts[j];
            var childNode;
            if (j + 1 < parts.length) {
                // Not yet at the end of the sequence; move down the tree.
                var foundChild = false;
                for (var k = 0; k < children.length; k++) {
                    if (children[k]["name"] == nodeName) {
                        childNode = children[k];
                        foundChild = true;
                        break;
                    }
                }
                // If we don't already have a child node for this branch, create it.
                if (!foundChild) {
                    childNode = {"name": nodeName, "children": []};
                    children.push(childNode);
                }
                currentNode = childNode;
            } else {
                // Reached the end of the sequence; create a leaf node.
                childNode = {"name": nodeName, "size": size};
                children.push(childNode);
            }
        }
    }
    return root;
}

function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

function getShorterName(fullPropertyName) {
    //return fullPropertyName;
    return fullPropertyName.split(/[^A-Za-z]/).pop();
}

function removeNoPropertyFromArray(sequence) {
    return sequence.name != "noProperty";
}

function removeEndAndNoPropertyFromColors(colors) {
    var result = {
        "temp": "#bbbbbb"
    };
    for (var key in colors) {
        if (colors.hasOwnProperty(key)) {
            if((key != "end") && (key != "noProperty")) {
                result[key] = colors[key];
            }
        }
    }
    delete result.temp;
    return result;
}