parent.postMessage({'action': 'getConfig'}, '*');

// http://stackoverflow.com/questions/1184624/convert-form-data-to-js-object-with-jquery
$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

window.onmessage = function (event) {
	if (event.data.action === 'gotConfig') {
		$.getJSON('app://mercy/public/themes/palette.json', function (palettes) {
			console.log(palettes);
			$('#theme').val(event.data.config.theme);
			for (var i = 0; i < palettes.length; i++) {
				var palette = palettes[i];
				var option = document.createElement('option');
				option.setAttribute('value', palette.colors.primary);
				option.innerHTML = palette.title;
				console.log(option);
				$('#palette').append(option);
			}
			$('#palette').val(event.data.config.primaryColor);
			$('#theme').val(event.data.config.theme);

		});
	}
}

$('form').submit(function (event) {
	event.preventDefault();
	var config = $(this).serializeObject();
	config.primaryColor = $('#palette').val();
	config.theme = $('#theme').val();
	window.parent.postMessage({'action': 'setConfig', 'config': config}, '*');
});