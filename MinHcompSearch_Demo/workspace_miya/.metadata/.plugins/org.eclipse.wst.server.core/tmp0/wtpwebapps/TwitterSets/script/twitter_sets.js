var tweetNum = 5;
var icons = new Array('blue_icon.png', 'dblue_icon.png', 'red_icon.png', 'dred_icon.png', 'green_icon.png', 'yellow_icon.png');
var unames = new Array('MIYANISHIE', 'SEKI', 'NAKASUGA', 'MATHIEU', 'KUMANAMI', 'FUJIKAWA', 'LIU', 'HIGASHIYAMA');
var queryNum = 5;

$(document).ready(function() {
	for (var i = 1; i <= queryNum; i++) {
		clearTextBox(i);
	}
});

function clearTextBox(i) {
	//var self = $("#query"+i);
	var self = $("#query" + i);
	//var btn = $("span.deleteicon span.bt"+i);
	var btn = $("span.deleteicon span#bt" + i);
	//var btn = $("#bt"+i);
	//var btn = $("#bt");
	//btn.show();
	if (self.val().length < 1) {
		btn.hide();
	}
	self.keyup(function() {
		if (self.val().length > 0) {
			btn.show();
		} else {
			btn.hide();
		}
	});
	btn.click(function() {
		self.val("");
		btn.hide();
		self.focus();
	});
}

/*
 $(document).ready(function() {
 $('input.deletable').wrap('<span class="deleteicon" />').after($('<span/>').click(function() {
 $(this).prev('input').val('').focus();
 }));
 });
 */

function queryAlert() {
	alert($("#query1").val() + " " + $("#query2").val() + " " + $("#query3").val() + " " + $("#query4").val() + " " + $("#query5").val());
}

function js2tagcloud(map) {
	var ws = new Array();
	for (k in map) {
		var v = map[k];
		ws.push({
			text : k,
			weight : v,
			html : {
				//onmouseover : "f"
				"class": "tag",
				onclick : "drawTemporalDynamics(\"" + k + "\")"
			}
		});
	}
	return ws;
}

function js2chart(head, chart) {
	var tp = new Array();
	tp.push(head);

	for (key in chart[i]) {

	}
	for (var i = 0; i < chart.length; i++) {
		map = chart[i];
		var t = new Array();

		for (var j = 0; j < head.length; j++) {
			var v = map[head[j]];
			t.push(v);
		}
		tp.push(t);
	}
	return tp;
}

function gsearch() {
	var query = $("#gsearch").val();
	var keyday = $("#keyday").val();
	var offsetdays = eval($("#offsetdays").val());
	var days = keyday.split("/");
	var jd1 = computeJD(days[0], days[1], eval(days[2]) - offsetdays);
	var jd2 = computeJD(days[0], days[1], eval(days[2]) + offsetdays);
	var search_url = "https://www.google.com/search?q=" + encodeURI(query) + "+daterange%3A" + jd1 + "-" + jd2;
	window.open(search_url, 'view');
}

function addTweets(tweets) {
	var text = '<ul class="tweet-results">';
	for (var i = 0; i < tweetNum && i < tweets.length; i++) {
		var uname = unames[Math.floor(Math.random() * unames.length)];
		var icon = icons[Math.floor(Math.random() * icons.length)];
		var date = tweets[i][2];
		//date = date.getFullYear()+"/"+date.getMonth()+"/"+date.getDate();
		var tweet_str = tweets[i][3].replace(/(http:\/\/[\x21-\x7e]+)/gi, "<a href='$1'>$1</a>");
		text += '<li class="tweet-result">';
		text += ' <div class="tweet-content">';
		text += '  <div class="image-holder">' + '<img src="img/' + icon + '" class="user-icon" />' + '</div>';
		text += '  <div class="tweet-holder">';
		//text += '   <span class="uname">' + uname + '</span>';
		//text += '   <span class="date">' + date + '</span>';
		text += '<div id="container"><div id="left">'+'<span class="uname">' + uname + '</span>'+'</div>';     
	    text += '<div align="right">'+'<span class="date">' + date + '</span>'+'</div></div>';
	     
		text += '   <p class="tweet">' + tweet_str + '</p>';
		text += '  </div>';
		text += ' </div>';
		text += '</li>';
	}
	text += '</ul>';
	$("#tweets").empty().append(text);
}

function searchPost() {
	//alert("searchPost");
	init();
	$("#searchForm").submit(function(event) {
		$("#tagcloud").empty();
		/* stop form from submitting normally */
		event.preventDefault();
		/* get some values from elements on the page: */
		var $form = $(this);
		var term1 = $("#query1").val();
		var term2 = $("#query2").val();
		var term3 = $("#query3").val();
		var term4 = $("#query4").val();
		var term5 = $("#query5").val();
		
		var url = $form.attr('action');
		/* Send the data using post and put the results in a div */
		$.post(url, {
			q1 : term1,
			q2 : term2,
			q3 : term3,
			q4 : term4,
			q5 : term5
		}, function(js) {
			if(js.tagcloud == undefined){
				init();
				alert("No results!");
			}else{
				ws = [];
				ws = js2tagcloud(js.tagcloud);
				$("#tagcloud").jQCloud(ws);
				addTweets(js.tweets);
			}
		}, "json");
		$("#searchForm").unbind("submit");
	});
}

function init(){
	$("#tagcloud").empty();
	$("#tweets").empty();
	$("#chart_div").empty();
	$("#websearch" ).empty();
}

function minustxt(txtid) {
	$(txtid).val("");
}

function drawTemporalDynamics_dummy(cand_word) {
	var chartData = {};
	chartData["name"] = "FIFA 2022 soccer World Cup"
	chartData["data"] = [['Time', "FIFA 2022 soccer World Cup", 'Qatar'], ['2011/1/27', 1000, 400], ['2011/1/28', 1170, 460], ['2011/2/2', 660, 1120], ['2007/2/9', 1030, 540], ['2007/2/9', 1030, 540], ['2007/2/9', 1030, 540], ['2007/2/9', 1030, 540], ['2007/2/9', 1030, 540], ['2007/2/9', 1030, 540], ['2007/2/9', 1030, 540]];
	var query = "FIFA 2022 soccer World Cup" + " " + cand_word;
	addTemporalDynamics(query, chartData);
}

function drawTemporalDynamics(cand_word) {
	// Get query terms
	var terms = [];
	for (var i = 1; i <= queryNum; i++) {
		var term = $("#query" + i).val();
		terms[i - 1] = term;
	}

	for (var i = 1; i <= queryNum; i++) {
		var term = $("#query" + i).val();
		if (term == "" || i == queryNum) {
			$("#query" + i).val(cand_word);
			var btn = $("span.deleteicon span#bt" + i);
			btn.show();
			break;
		}
	}

	//var query = $.trim([term1, term2, term3, term4, term5].join(' '));
	var query = $.trim(terms.join(' '));
	var url = $("#searchForm").attr('action');

	$.post(url, {
		q1 : terms[0],
		q2 : terms[1],
		q3 : terms[2],
		q4 : terms[3],
		q5 : terms[4],
		cw : cand_word
	}, function(js) {
		var chartData = {};
		// Draw chart
		chartData["name"] = query;
		chartData["data"] = js2chart(['time', query, query + " " + cand_word], js.chart);
		//alert(query);
		addTemporalDynamics(query + " " + cand_word, chartData);
		addTweets(js.tweets);
	}, "json");
}

function addTemporalDynamics(query, chartData) {
	//alert(query);
	var title_sets = chartData["name"];
	//var options = {title : title_sets};
	var options = {};
	var data = google.visualization.arrayToDataTable(chartData["data"]);
	addWebSearchBox(query);
	// add google search box

	function selectHandler() {
		var selectedItem = chart.getSelection()[0];
		if (selectedItem) {
			var selected_day = data.getValue(selectedItem.row, 0);
			$("#keyday").val(selected_day);
		}
	}

	var chart = new google.visualization.LineChart(document.getElementById('chart_div'));
	google.visualization.events.addListener(chart, 'select', selectHandler);
	chart.draw(data, options);
}

function addWebSearchBox(query) {
	var wstext = "";
	wstext += '<div align="center">';
	wstext += '<input type="text" id="gsearch" name="gsearch" size="35" />';
	wstext += '<input type="text" id="keyday" name="keyday" size="12" />';
	wstext += '<select id="offsetdays">';
	wstext += '<option value="0">0</option>';
	wstext += '<option value="1">1</option>';
	wstext += '<option value="2">2</option>';
	wstext += '<option value="3">3</option>';
	wstext += '<option value="4">4</option>';
	wstext += '<option value="5">5</option>';
	wstext += '</select>';
	wstext += '<input type="submit" value="Google Search" onclick="gsearch()" />';
	wstext += '</div>'
	$("#websearch").empty().append(wstext);
	$("#gsearch").val(query);
}

function computeJD(Y, M, D) {
	Y = eval(Y);
	M = eval(M);
	D = eval(D);
	with (Math) {
		GGG = 1;
		if (Y <= 1585)
			GGG = 0;
		JD = -1 * floor(7 * (floor((M + 9) / 12) + Y) / 4);
		S = 1;
		if ((M - 9) < 0)
			S = -1;
		A = abs(M - 9);
		J1 = floor(Y + S * floor(A / 7));
		J1 = -1 * floor((floor(J1 / 100) + 1) * 3 / 4);
		JD = JD + floor(275 * M / 9) + D + (GGG * J1);
		JD = JD + 1721027 + 2 * GGG + 367 * Y;
	}
	if (D == 0 && M == 0 && Y == 0) {
		alert("Please enter a meaningful date!");
	}
	return JD;
}

