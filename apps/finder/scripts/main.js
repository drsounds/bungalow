var playlists = {};

window.addEventListener('message', function (event) {
	console.log("Event data", event.data);
	if (event.data.action === 'navigate') {
		$('#apps').html("");
		showThrobber();
		

		App.find(function (apps) {
			var html = "<tr>";
			for (var i = 0; i < apps.length; i++) {
				var app = apps[i];
				
				html += '<td><table><tr><td><div class="app-icon" style="background-image: url(\'' + app.icon_url + '\')"></div></td><td valign="top"><h3 style="height: 20pt">' + app.name + '</h3><a class="btn" data-uri="spotify:' + app.id + '">View</a>	</td></tr></table></td>';
				if (i % 2 == 1) {
					html += '</tr><tr>';
				}
				hideThrobber();	
			}
			html += '<tr>';
			$('#apps').html(html);
		});
	}

})