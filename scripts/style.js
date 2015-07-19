
var BUNGALOW_THEME = 'bungalow';




// Configuration
function bungalow_load_settings () {
	var path = require('path');
	var fs = require('fs');
	var path = process.env['HOME'] + path.sep + 'bungalow.json';
	

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

}

var settings = bungalow_load_settings();

BUNGALOW_THEME = settings.theme;

