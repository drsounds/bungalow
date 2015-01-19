setTimeout(function () {
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
link.setAttribute('rel', 'stylesheet/less');
link.setAttribute('type', 'text/css');
link.setAttribute('href', 'app://mercy/public/themes/' + BUNGALOW_THEME + '/css/style.less');
document.head.appendChild(link);
less = {
    env: 'development',
    async: true,
    fileAsync: true, 
    poll: 1000, 
    functions: {}, 
    dumpLineNumbers: 'comments', 
    relativeUrls: false, 
    rootpath: ''
};
setTimeout(function () {	
	var scriptLess = document.createElement('script');
	scriptLess.setAttribute('src', 'app://mercy/public/scripts/vendor/less.js');
	scriptLess.setAttribute('type', 'text/javascript');
	document.head.appendChild(scriptLess);
	setTimeout(function () {
		less.watch();
	}, 10);
}, 100);
}, 10);