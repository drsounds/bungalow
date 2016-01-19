require([], function () {
	var top_tweeted_times = [
		420,
		480,
		650,
		600,
		550,
		430,
		230,
		120,
		80,
		10,
		20,
		30,
		390
	];
	
	var countries = [
		{
			id: 'US'
		},
		{
			id: 'SE'
		},
		{
			id: 'DE'
		},
		{
			id: 'DK'			
		},
		{
			id: 'US'
		}
	];
	
	var timezones = {
		'US': -8,
		'DE': -1,
		'UK': -1,
		'CN': 8,
		'SE': 2, // SUMMER TIME
		'IN': 6
	};
	
	var top_countries = {
		'DK': 0.2,
		'FI': 0.1,
		'DE': 0.9,
		'FR': 0.7,
		'IN': 0.6,
		'US': 0.9	
	};
	
	
	var Promote = function () {
		for (var i = 0; i < countries.length; i++) {
			var country = countries[i];
			
			var timezone = timezones[]timezone;
			
		}
	};
});