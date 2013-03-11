
$(function() {
	$.getJSON('http://127.0.0.1:8080/log4j-servlet/api/appenders', function(json) {
		var table = $('#appenders');
		$.each(json, function(index, item) {
			table.append($('<tr id="' + item.name + '">'
					+ '<td>' + item.name + '</td>'
					+ '<td>' + item.threshold + '</td>'
					+ '</tr>'));
		});
	});
	
	$.getJSON('http://127.0.0.1:8080/log4j-servlet/api/loggers', function(json) {
		var table = $('#loggers');
		$.each(json, function(index, item) {
			table.append($('<tr id="' + item.name + '">'
					+ '<td>' + item.name + '</td>'
					+ '<td><div class="btn-group"><a class="btn dropdown-toggle" data-toggle="dropdown" href="#">'
					+ '<span class="currentLevel">' + item.level + '</span>'
					+ '<span class="caret"></span>'
					+ '</a><ul class="dropdown-menu">'
					+ '<li><a class="level-btn" href="#">ERROR</a></li>'
					+ '<li><a class="level-btn" href="#">WARN</a></li>'
					+ '<li><a class="level-btn" href="#">INFO</a></li>'
					+ '<li><a class="level-btn" href="#">DEBUG</a></li>'
					+ '<li><a class="level-btn" href="#">TRACE</a></li>'
					+ '</ul>'
					+ '</tr>'));
		});
	});
	
	$(document).on('click', '#reload', function(event) {
		$.getJSON('http://127.0.0.1:8080/log4j-servlet/api/reload', function(json) {
			location.reload();
		});
	});
	
	$(document).on('click', '.level-btn', function(event) {
		var name = $(this).closest('tr').attr('id');
		var level = $(this).text();
		var currentValue = $(this).closest('div').find('span[class="currentLevel"]');
		console.log('Setting level of ' + name + ' to ' + level);
		
		var loggerRequest = {
			'name': name,
			'level': level
		};
		$.ajax({
			url: 'http://127.0.0.1:8080/log4j-servlet/api/loggers',
			type: 'POST',
			data: JSON.stringify(loggerRequest),
			contentType: 'application/json; charset=utf-8',
			dataType: 'json',
			error: function(jqXHR, status, error) {
				console.log('status: ' + status);
				console.log('error: ' + error);
			},
			success: function(jqXHR, status, error) {
				if (jqXHR.status == 'root') {
					location.reload();
				} else {
					currentValue.text(level);
				}
			}
		});
	});
});
