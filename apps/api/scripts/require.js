var __level = 0;
var requirejs = function (exports) {
    exports.require = function () {
        var __imports = {};
        var __context = {};
        var modules = arguments[0];
        var callback = arguments[1];
        var items = [];
        for (var i = 0; i < modules.length; i++) {
            var xmlHttp = new XMLHttpRequest();
            var module = modules[i];
            if (module.indexOf('$') == 0) {
                var module = module.substr(0);
                var app = module.split('/')[0];
                module = app + '/scripts/' + module.split('/').slice(1).join('/');
                module = 'http://play.bungalow.qi/apps/' + module.substr(1);


            }
            var _class = module.indexOf('#') > 0 ? module.split('#')[1] : null;
            if (_class != null) {
                module = module.split('#')[0];
            }

            if (module in __imports) {
                items.push(__imports[module]);
                return;
            }
            module = module + '.js';
            console.log(module);
            xmlHttp.open('GET', module, false);
            xmlHttp.send(null);
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                __level--;
                var code = ("(function () { var exports = {}; " + xmlHttp.responseText + "; return exports;  })();");
                mod = null;
                try {
                    var mod = eval(code);
                } catch (e) {
                    mod = null;
                }
                __level++;
                if (_class != null) {
                    mod = mod[_class];
                }
                items.push(mod);
                __imports[module] = mod;

            }
        }
        console.log(items);
        console.log(callback);
        callback.apply(__context, items);

    }

};

requirejs(window);