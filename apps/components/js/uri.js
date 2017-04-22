
function objectToQueryString(obj, prefix) {
    return Object.keys(obj).map(objKey => {
        if (obj.hasOwnProperty(objKey)) {
            const key = prefix ? `prefix[${objKey}]` : objKey;
            const value = obj[objKey];

            return typeof value === "object" ? this.objectToQueryString(value, key) : `${encodeURIComponent(key)}=${encodeURIComponent(value)}`;
        }

        return null;
    }).join("&");
}

function parseQuery(search) {

    var args = search.split('&');

    var argsParsed = {};

    var i, arg, kvp, key, value;

    for (i=0; i < args.length; i++) {

        arg = args[i];

        if (-1 === arg.indexOf('=')) {

            argsParsed[decodeURIComponent(arg).trim()] = true;
        }
        else {

            kvp = arg.split('=');

            key = decodeURIComponent(kvp[0]).trim();

            value = decodeURIComponent(kvp[1]).trim();

            argsParsed[key] = value;
        }
    }

    return argsParsed;
}
class Uri {
  hasQuery() {
    return this.query instanceof Object && Object.keys(this.query).length > 0;
  }
  constructor(uri) {

      this.fgs = uri.split(/\?/g);
      this.uri = this.fgs[0];
      this.parts = this.uri.split('@');
      this.fragments = this.parts[0].split(':');
      this.id = this.fragments[2];
      this.protocol = this.fragments[0];
      if (this.fgs.length > 1) {
        this.query = parseQuery(this.fgs[1]);
      } else {
        this.query = {
          service: localStorage.getItem('service')
        };
      }

      if (this.parts.length > 1) {
        this.query.service = this.parts[1];
      }
      
      if(!this.query.service) {
        this.query.service = localStorage.getItem('service');
      }
      this.service = this.query.service;
      this.domain = this.fragments[1];
      this.path = this.fragments.slice(2);

      this.subService = this.service;
      this.apiUrl = this.toApiUrl();
  }
  toUri() {
    var strUri = this.protocol + ':' + this.domain + ':' + this.path.join(':');
    if (this.hasQuery()) {
        strUri + '?' + objectToQueryString(this.query);
    }
    return strUri;
  }

  toUrn() {
    var url = 'urn:' + this.domain + ':' + this.path.join(':');
    if (this.hasQuery()) {
        url += '?' + objectToQueryString(this.query);
    }
    return url;
  }
  
  toApiUrl() {
    var url = 'https://' + this.service + '/' + this.domain + '/' + this.path.join('/');
    if (this.hasQuery()) {
        url += '?' + objectToQueryString(this.query);
    }
    return url;
  }
  toBrowserPath() {
    var strUri = '/' + this.domain + '/' + this.path.join('/') ;

    if (this.hasQuery()) {
        strUri += '?' + objectToQueryString(this.query);
    }
    return strUri;
  }
  toUrl() {
      var strUri = 'https://' + window.location.hostname + '/' + this.domain + '/' + this.path.join('/');

      if (this.hasQuery()) {
          strUri += '?' + objectToQueryString(this.query);
      }
      return strUri;
  }
  static parse(uri) {
      return new Uri(uri);
  }
}
