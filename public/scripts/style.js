
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
			'theme': 'bungalow',
			'primaryColor': '#FB8521'
		};
	}
}

function bungalow_save_settings (settings) {
	console.log("Settings", settings);
	if (!settings) {
		return;
	}
	var path = require('path');
	var fs = require('fs');
	var path = process.env['HOME'] + path.sep + 'bungalow.json';
	fs.writeFileSync(path, JSON.stringify(settings));

	// Set theme variables
	var less = require('less');
	var theme = fs.readFileSync(process.env.PWD + '/public/themes/' + settings.theme + '/css/style.less', {'encoding': 'utf-8'});
	console.log(process.env.PWD + '/public/themes/' + settings.theme + '/css/style.less');
	theme = theme.replace(/\@primary-color/, settings.primaryColor);
	less.render(theme, {}, function (error, output) {
		console.log(error, output);
		alert(output);
		fs.writeFileSync(process.env.PWD + '/public/themes/' + settings.theme + '/css/style.css', output.css);
	});
}

var settings = bungalow_load_settings();

BUNGALOW_THEME = settings.theme;

var link = document.createElement('link');
link.setAttribute('rel', 'stylesheet');
link.setAttribute('type', 'text/css');
link.setAttribute('href', 'app://mercy/public/themes/' + BUNGALOW_THEME + '/css/style.css');
document.head.appendChild(link);

