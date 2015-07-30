var imports = {}
var __context = this;
var depth = 0;
var requirejs = function (exports) {
    exports.require = function () {
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
                module = 'http://localhost:9261/app/' + module.substr(1);


            }
            console.log(module);
            var _class = module.indexOf('#') > 0 ? module.split('#')[1] : null;
            if (_class != null) {
                module = module.split('#')[0];
            }

            if (module in imports) {
                items.push(imports[module]);
                return;
            }
            module = module + '.js';
            xmlHttp.open('GET', module, false);
            xmlHttp.send(null);
            if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
                console.log(xmlHttp.responseText);
                var code = ("(function () { var exports = {}; " + xmlHttp.responseText + "; console.log('exports" + depth + "', exports); return exports;  })();");
                console.log(code);
                var mod = eval(code);
                console.log("Mod", mod);
                if (_class != null) {
                    console.log(mod);
                    mod = mod[_class];
                }
                items.push(mod);
                imports[module] = mod;

            }
        }
        callback.apply(__context, items);
    }

};

requirejs(window);