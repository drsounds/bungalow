
var BUNGALOW_THEME = 'bungalow';
var Fungalow = function () {
	this.resolver = null;




	// Configuration

	this.settings = this.loadSettings();
	// load resolver
	var Resolver = require('resolvers/' + this.settings.resolver + '/main.js');
	this.resolver = new Resolver();
	BUNGALOW_THEME = this.settings.theme;
}

Fungalow.prototype.loadSettings = function () {
	var path = require('path');
	var fs = require('fs');
	var path = process.env['HOME'] + path.sep + 'bungalow.json';
	
	if (fs.existsSync(path)) {
		var settings = JSON.parse(fs.readFileSync(path));
		return settings;
	} else {
		var settings = {
			'bungalows': {},
			'apps': [],
			'theme': 'spotify09',
			'light': true,
			'primaryColor': '#FB8521'
		};
		this.saveSettings(settings);
		return settings;
	}

	
}

Fungalow.prototype.saveSettings = function (settings) {
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
	var lightTheme = fs.readFileSync(process.env.PWD + '/public/themes/' + settings.theme + '/css/light.less', {'encoding': 'utf-8'});
	console.log(process.env.PWD + '/public/themes/' + settings.theme + '/css/style.less');
	lightTheme = lightTheme.replace(/\@primary-color/, settings.primaryColor);
	lightTheme = lightTheme.replace(/\@secondary-color/, settings.secondaryColor);
	lightTheme = lightTheme.replace("@islight", true ? '@light' : '@dark');
	lightTheme = lightTheme.replace("@isdark", !true ? '@light' : '@dark');

	var darkTheme = fs.readFileSync(process.env.PWD + '/public/themes/' + settings.theme + '/css/dark.less', {'encoding': 'utf-8'});
	console.log(process.env.PWD + '/public/themes/' + settings.theme + '/css/style.less');
	darkTheme = darkTheme.replace(/\@primary-color/, settings.primaryColor);
	darkTheme = darkTheme.replace(/\@secondary-color/, settings.secondaryColor);
	darkTheme = darkTheme.replace("@islight", true ? '@light' : '@dark');
	darkTheme = darkTheme.replace("@isdark", !true ? '@light' : '@dark');
	
	//alert(theme);
	less.render(lightTheme, {}, function (error, output) {
		console.log(error, output);
		//alert(output);
		fs.writeFileSync(process.env.PWD + '/public/themes/' + settings.theme + '/css/light.css', output.css);
	});

	less.render(darkTheme, {}, function (error, output) {
		console.log(error, output);
		//alert(output);
		fs.writeFileSync(process.env.PWD + '/public/themes/' + settings.theme + '/css/dark.css', output.css);
	});
	fs.writeFileSync(process.env.PWD + '/public/themes/' + settings.theme + '/css/main.css', '@import url("' + (settings.light ? 'light' : 'dark') + '.css")');
	

	var mainCSS = '@import url("' + settings.theme + '/css/style.css")';
	fs.writeFileSync(process.env.PWD + '/public/themes/main.css', mainCSS);

	fs.writeFileSync(process.env.PWD + '/public/themes/main.css', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/main.css")');
	fs.writeFileSync(process.env.PWD + '/public/themes/light.css', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/light.css")');
	fs.writeFileSync(process.env.PWD + '/public/themes/dark.css', '@import url("http://127.0.0.1:9261/themes/' + settings.theme + '/css/dark.css")');

}

exports.Fungalow = Fungalow;
