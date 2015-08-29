window.addEventListener('message', function (event) {
    var data = event.data;
    if (data.action == 'navigate') {
        if ('arguments' in data) {
            var event = new CustomEvent('argumentchanged');
            event.data = data.arguments;
            if (window.onargumentschanged instanceof Function)
                window.onargumentschanged(event);
        } 
        if ('uri' in data) {
            var uri = data.uri;
            if (uri.match(/#(.*)/g)) {
                self.location.hash = uri.substr(1);
            } else {
                var arguments = uri.split(/\:/g).slice(1);
                var event = new CustomEvent('argumentchanged');
                event.data = arguments;
                if (window.onargumentschanged instanceof Function)
                    window.onargumentschanged(event);
            }
        }
    }
});
$(document).on('click', '*[data-uri]', function (event) {
    var target = event.target;
    window.parent.postMessage({'action': 'navigate', 'uri': target.dataset['uri']}, '*');
});



(function ($) {
    $.fn.popover = function (options) {
        var $elm = this;
        var $anchor = $(options.anchor);
        console.log($anchor[0]);
        console.log($elm[0].getBoundingClientRect());
        $(this).css({'position': 'absolute'});
        $(this).addClass('popover');
        $(this).css({'width': '320px', 'height': '320px'});
        $(this).css({'left': $anchor[0].getBoundingClientRect().left + 'px', 'top': ($anchor[0].getBoundingClientRect().top + $anchor[0].getBoundingClientRect().height) + 'px'});
        $(this).html($elm.html());
        $(this).fadeIn();
    };
})(jQuery);