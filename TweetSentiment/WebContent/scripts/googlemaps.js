var heatmap; 

function main() {
	var keyword = $("#selector").val();
	
	var map = initialize();
	getTweets(map, keyword);
	
	$("#selector" ).change(function () {
		keyword = $("#selector").val();
		
		getTweets(map, keyword);
	});
	
}

function getTweets(map, keyword) {
	
	var serverUrl = "http://localhost:8080/TweetSentiment/gettweets.jsp?keyword="+keyword;
	
	$.ajax({
	    url : serverUrl,
	    data : '',
	    dataType: 'json',
	    success : function(jsonarray) {
	        
	        var heatmapData = [];
	        
	        $.each(jsonarray, function(i, tweet) {
	        	var lon = tweet.lon;
	        	var lat = tweet.lat;
	        	var point = new google.maps.LatLng(lat, lon);
	        	heatmapData[heatmapData.length] = point;
	        });
	        
			createHeatmap(map, heatmapData);
	    }
	});
	
}

function initialize() {
	var sanFrancisco = new google.maps.LatLng(19.5900847, -17.147339);

    var mapOptions = {
      center: sanFrancisco,
      zoom: 2
    };

    var map = new google.maps.Map($('#map-canvas')[0], mapOptions);
    return map;
}

function createHeatmap(map, heatmapData) {
	
	if(heatmap) {
		heatmap.setMap(null);
	}
	
	heatmap = new google.maps.visualization.HeatmapLayer({
	  	data: heatmapData,
	  	radius: 25
	});
	
	heatmap.setMap(map);
}

$(document).ready(main);