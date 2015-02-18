var map;
var heatmap;
var view;
var tweetMarkers = [];
var heatpoints = [];
var first = true;
var tweetsData = [];

function main() {
	view = $("#selector").val();
	map = initialize();
	getTweets();
	setInterval(getTweets, 10000);

	$("#selector" ).change(function () {
		view = $("#selector").val();
		showData(view);
	});
	
}

var getTweets = function () {
	if(heatpoints.length!=0) {
		heatpoints = [];
	}
	var serverUrl = "gettweets.jsp";
	
	$.ajax({
	    url : serverUrl,
	    data : '',
	    dataType: 'json',
	    success : function(jsonarray) {
	    	tweetsData = [];
	        
	        $.each(jsonarray, function(i, tweet) {
	        	var lon = tweet.lon;
	        	var lat = tweet.lat;
	        	var sent = tweet.sent;
	        	
	        	var point = new google.maps.LatLng(lat, lon);
	        	
	        	var value;
	        	if(sent=="positive") {
	        		value = 4;
	        	}
	        	else if(sent=="neutral") {
	        		value = 3;
	        	}
	        	else if(sent="negative") {
	        		value = 2;
	        	}
	        	else {
	        		value = 1;
	        	}
	        	
	        	var heatpoint = {
	        			location: point,
	        			weight: value
	        	};
	        	tweetsData[tweetsData.length] = point;
	        	heatpoints[heatpoints.length] = heatpoint;
	        });
	        showData(view);
	    }
	});
};

var showData = function(view) {
	if(heatmap) {
		heatmap.setMap(null);
	}
	if(tweetMarkers.length!=0) {
		$.each(tweetMarkers, function(i, marker) {
			marker.setMap(null);
		});
	}

	if(view=="heatmap") {
		heatmap = new google.maps.visualization.HeatmapLayer({
	  		data: heatpoints,
	  		radius: 25
		});
		heatmap.setMap(map);		
	}
	else if(view=="tweets") {
		$.each(tweetsData, function(i, tweet) {
			//Create a new marker
		    var marker = new google.maps.Marker({
	    		position: tweet
			});
			tweetMarkers[tweetMarkers.length] = marker;
		    marker.setMap(map);
		});
	}
	else {
		console.log("Invalid view value");
	}
};




function initialize() {
	var sanFrancisco = new google.maps.LatLng(19.5900847, -17.147339);

    var mapOptions = {
      center: sanFrancisco,
      zoom: 2
    };

    var map = new google.maps.Map($('#map-canvas')[0], mapOptions);
    return map;
}

function createHeatmap(map) {
	
	if(heatmap) {
		heatmap.setMap(null);
	}

}

$(document).ready(main);