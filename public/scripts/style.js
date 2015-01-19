
var BUNGALOW_THEME = 'bungalow';




// Configuration
function bungalow_load_settings () {
	var path = require('path');
	var fs = require('fs');
	var path = process.env['HOME'] + path.sep + 'bungalow.json';
	if (fs.existsSync(path)) {
		var settings = JSON.parse(fs.readFileSync(path));
		return settings;
	} else {
		return {
			'bungalows': {},
			'apps': [],
			'theme': 'bungalow'
		};
	}
}

function bungalow_save_settings (settings) {
	var path = require('path');
	var fs = require('fs');
	var path = process.env['HOME'] + path.sep + 'bungalow.json';
	fs.writeFileSync(path, JSON.stringify(settings));
}

var settings = bungalow_load_settings();

BUNGALOW_THEME = settings.theme;

var link = document.createElement('link');
link.setAttribute('rel', 'stylesheet');
link.setAttribute('type', 'text/css');
link.setAttribute('href', 'app://mercy/public/themes/' + BUNGALOW_THEME + '/css/style.css');
document.head.appendChild(link);

