/*global fetch*/
/*global resolve*/
/*global localStorage*/
String.prototype.capitalizeFirstLetter = function () {
    return this.charAt(0).toUpperCase() + this.slice(1);
}
var apps = localStorage.getItem('apps');
if (!apps) {
    apps = {
        'cravity': {
            'id': '5jlDMGaGRF3ji5IURkaj3jDY493ywO8KqelpGK7s',
            'key': 'rfyCrZxke8nomd2d60xbmQDH3y0dWbMWjwjyMD9F',
            'url': 'https://parseapi.back4app.com/'
        },
        'fungalify': {
            'id': '4VlyWUz5mP9J5n3IrKQGS0XImJDbwrzq6FMXjqOD',
            'key': 'OgaOxO9uNuYv8UFLqF1uCbCCUz8SdRRQCMnRJNWv',
            'url': 'https://parseapi.back4app.com/'
        }
    }
}
class Ocean {
    static getCurrentService() {
        return localStorage.getItem('service') || window.location.hostname;
    }
    static request(method, url, data, headers) {
        if (url.indexOf('parse:') == 0) {
            return new Promise((resolve, reject) => {
                let uri = new Uri(url);
                var app = apps[uri.service];
                Parse.initialize(app.id, app.key);
                Parse.serverURL = app.url;
                Parse.User.logIn('drsounds', '123');
                var entity = uri.domain.capitalizeFirstLetter();
                if (method === 'GET') {
                    if (uri.id) {
                        new Parse.Query(entity).get(uri.id, {
                            success: function (obj) {
                                let ob = obj.toJSON();
                                if (!obj.uri) {
                                    ob.uri = uri.protocol + ':' + entity.toLowerCase() + ':' + ob.objectId + '?service=' + uri.service;
                                }
                                
                                resolve(ob);
                            }
                        });
                    
                    } else {
                        new Parse.Query(entity).descending('time').find({
                            success: function (list) {
                                var objects = list.map((obj) => {
                                        let ob = obj.toJSON();
                                        if (!obj.uri) {
                                            ob.uri = uri.protocol + ':' + entity.toLowerCase() + ':' + ob.objectId + '?service=' + uri.service;
                                        }
                                        return ob;
                                    });
                                resolve({
                                    objects: objects
                                });
                            }
                        });
                    }
                }
                if (method === 'PUT') {
                            
                    new Parse.Query(entity).get(
                        uri.id,
                        {
                        success: function (obj) {
                            for (let k in data) {
                                let val = data[k];
                                if (val instanceof String && moment('YYYY-mm-dd', val).isValid()) {
                                    val = moment(val).toDate();
                                }
                                obj.set(k, data[k]);
                            }
                            obj.save({
                                success: function (obj) {
                                    
                                    resolve(obj);
                                },
                                error: function (obj) {
                                    reject(obj);
                                }
                            });
                        }
                    });
                }
                if (method === 'POST') {
                   
                    let obj = new Parse.Object(entity);
                    for (let k in data) {
                        obj.set(k, data[k]);
                    }
                    if(!('time' in data)) {
                        obj.set('time', new Date());
                    }   
                    obj.save(null, {
                        success: function (obj) {
                            resolve(obj);
                        },
                        error: function (err) {
                            reject(err);
                        }
                    });
                }
            });
        
        }
        if (url.indexOf('bungalow:') == 0) {
            //uri = uri.substr(uri.split(/\:/g)[0].length);
            //uri 
            let uri = new Uri(url);
            url = uri.toApiUrl();
             fetch(
                uri
            ).then((t) => t.json()).then((result) => {
                resolve(result);
            });
        }
        if (url.indexOf('https://') == 0) {
            fetch(
                url
            ).then((t) => t.json()).then((result) => {
                resolve(result);
            });
        } else if(url.indexOf('/') === 0) {
            return new Promise((resolve, reject) => {
                fetch(
                   'https://' + this.getCurrentService() + url
                ).then((t) => t.json()).then((result) => {
                    resolve(result);
                });
            })
        }
    }
    
    static get(url) {
        return this.request('GET', url);
    }
    
    static put(url, data) {
        return this.request('PUT', url, data);
    }
    
    static post(url, data) {
        return this.request('POST', url, data);
    }
}