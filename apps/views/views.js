(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define(['b'], factory);
    } else if (typeof module === 'object' && module.exports) {
        // Node. Does not work with strict CommonJS, but
        // only CommonJS-like environments that support module.exports,
        // like Node.
        module.exports = factory(require('b'));
    } else {
        // Browser globals (root is window)
        root.returnExports = factory(root.b);
    }
}(this, function (b) {
	var exports = {};
	var moment = require('moment');
	var models = require('models');
    //use b in some fashion.
    /**
     * A context
     * @class
     * @constructor
     */
    var Context = function () {
        this.currentApp = window.location.href.split(/\//g)[5];
        // // console.log("Current app", this.currentApp);
        this.currentIndex = 0;
        var self = this;
        window.onmessage = function (event) {
            if (event.data.action === 'trackended') {
                var index = event.data.newIndex;
                var tracks = document.querySelectorAll('.sp-track');
                $('#context_' + event.data.uri.replace(/\:/g, '__') + ' > .sp-track').removeClass('sp-now-playing');

            }
        }

    };

    var collection_contexts = {};

    var actives = {};


    // // console.log(window.location.href.split(/\//g)[4]);

    HTMLTableRowElement.prototype.toTrack = function () {
        /*var track = {
         'uri': this.getAttribute('data-uri'),
         'duration': this.getAttribute('data-duration'),
         'name': this.getElementsByTagName('td')[0].textContent,
         'artists': [{
         'name': this.getElementsByTagName('td')[1].getElementsByTagName('a')[0].innerText,
         'uri': this.getElementsByTagName('td')[1].getElementsByTagName('a')[0].getAttribute('data-uri')
         }]
         };*/
        var track = JSON.parse(this.getAttribute('data-object'));
        // // console.log("Converted tr to track", track);
        return track;
    }

    function context_to_tracks(elements) {
        var tracks = [];
        for (var i = 0; i < elements.length; i++) {
            var elm = elements[i];
            tracks.push(elm.toTrack());
        }
        return tracks;
    }

    /**
     * Plays a context at certain index
     * @this {Context}
     * @param {String} uri The context's URI
     * @param {Integer} index The index of entry to play
     * @return {void}
     */

    Context.prototype.play = function (uri, index) {
        // // console.log("Playing from context with uri" + uri);
        // // console.log("Start playing track");
        var sel = '.sp-table[data-uri="' + uri + '"]';
        $(sel + ' .sp-track').removeClass('sp-now-playing');
        // // console.log(sel + ' .sp-track');
        var tracks = document.querySelectorAll(sel + ' .sp-track');
        tracks = context_to_tracks(tracks);
        var track = tracks[index];
        // // console.log("tracks in context ", tracks);
        var tracklist = {
            'tracks': tracks,
            'track': track,
            'currentIndex': index,
            'uri': uri
        };

        // // console.log(this.currentApp, "Current app");
        window.parent.postMessage({'action': 'activateApp', 'app': this.currentApp}, '*');
        window.parent.postMessage({
            'action': 'play',
            'track': JSON.parse(JSON.stringify(track)),
            'data': JSON.stringify(tracklist)
        }, '*');
        this.currentIndex = 0;
        track.classList.add('sp-now-playing');

    }

    exports.Context = Context;

    var track_contexts = new Context();
    (function ($) {
        $.fn.spotifize = function (options) {
            // // console.log("Spotifize");
            var self = this;


        };
    }(jQuery));

    $(document).on('dblclick', '.sp-track', function (event) {
        var parent = $(event.target).parent().parent().parent();
        var uri = $(parent).attr('data-uri');
        // // console.log("Uri", uri);
        // // console.log("Parent", parent[0]);
        var index = $(this).attr('data-track-index');
        context.play(uri, index);

    });

    function deci(number) {
        return Math.round(number) < 10 ? '0' + Math.round(number) : Math.round(number);
    }

    /**
     * Converts second into MM:SS string
     * @param  {Integer} seconds Seconds
     * @return {String} A MM:SS
     */
    function toMS(seconds) {
        var minutes = Math.floor(seconds / 60);
        var seconds = (seconds % 60) / 60;
        return deci(minutes) + ':' + deci(seconds);
    }

    /**
     * Update track contexts
     * @function
     * @param {String} uri URI of context
     * @param {Array} tracks Array of Track instances of the tracks to update
     * @param {Integer} position The position where to update
     **/
    function bungalow_update_context(uri, tracks, position) {
        var context = track_contexts[uri];
        context.insertTracks(tracks, position);
    }


    /** From http://upshots.org/javascript/jquery-insert-element-at-index */
    $.fn.insertAt = function (elements, index) {
        var children = this.children().clone(true);
        var array = $.makeArray(children);
        array.splice(index, 0, elements);
        this.empty().append(array);
        return this;
    }

    /**
     * Represents a single track entry
     * @param {Track} track   A music track
     * @param {[type]} index   Index of the track in the context
     * @param {[type]} options Options
     * @constructor
     * @class
     */
    var Track = function (track, index, contextUri, options) {
        if ('track' in track) {
            track = Object.assign(track, track.track);
        }
        this.node = document.createElement('tr');
        this.node.classList.add('sp-track');
        this.node.setAttribute('data-uri', track.uri);
        this.node.setAttribute('data-context-uri', contextUri);
        this.node.setAttribute('data-object', JSON.stringify(track));
        this.node.setAttribute('draggable', true);
        this.node.setAttribute('data-track-index', index);


        for (var i = 0; i < options.fields.length; i++) {
            var field = options.fields[i];
            if (field === 'image') {
                var td3 = document.createElement('td');
                if ('album' in track) {
                td3.innerHTML = '<img src="' + track.album.images[0].url +'" height="100%">';
                this.node.appendChild(td3);
                }
                td3.style.width = '10px';
            }
            if (field === 'no') {
                var td3 = document.createElement('td');
                td3.innerHTML = '';
                td3.style.textAlign = 'right';
                this.node.appendChild(td3);
                td3.style.width = '10px';
            }
            if (field === 'title') {
                var td1 = document.createElement('td');
                td1.innerHTML = track.name;
                this.node.appendChild(td1);
            }
            if (field === 'artist' || field == 'author') {
                var td2 = document.createElement('td');

                td2.innerHTML = '<a data-uri="' + (track.authors[0].uri || track.authors[0].link) + '">' + track.authors[0].name + '</a>';
                this.node.appendChild(td2);
            }
            
            if (field === 'duration') {
                var td3 = document.createElement('td');
                td3.innerHTML = '<span class="fade" style="float: right">' + moment.utc(parseInt(track.duration) * 1000).format('mm:ss') + '</span>';
                this.node.appendChild(td3);
            }
            if (field === 'popularity') {
                var td2 = document.createElement('td');

                var popularityBar = new PopularityBar(track);
                td2.appendChild(popularityBar.node);
                popularityBar.render();
                this.node.appendChild(td2);
            }
            if (field === 'album') {
                var td4 = document.createElement('td');
                td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.album.uri + '">' + track.album.name + '</a>';
                this.node.appendChild(td4);
            }
            if (field === 'user') {
                var td4 = document.createElement('td');
                td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.added_by.uri + '">' + track.added_by.displayName + '</a>';
                this.node.appendChild(td4);
            }

        }

        var td5 = document.createElement('td');
        td5.innerHTML = '&nbsp;';
        this.node.appendChild(td5);
        if ('availability' in track && track.availability !== 1) {
           
        } else {
        	 $(this.node).addClass('sp-track-unavailable');
        }

    }

    exports.Track = Track;

    /**
     * Image view
     * @param {Track|Album|collection} object The resource
     * @param {Object} options [description]
     */
    var Image = function (object, options) {
        this.node = document.createElement('div');
    }

    /**
     * Player view
     * @param {Track|Album|collection} object The resource
     * @param {Object} options [description]
     */
    var Player = function (object, options) {
        this.node = document.createElement('div');
    }

    exports.Player = Player;

    exports.Image = Image;

    var fieldTypes = {
        'no': 'No.',
        'title': 'Title',
        'album': 'Album',
        'artist': 'Artist',
        'duration': 'fa-clock',
        'popularity': 'fa-thumbs-o-up',
        'user': 'User',
        'image': '',
        'creted': 'fa-clock'
    };

    var Collection = function (collection, options, Class) {
        this.collection = collection;
        this.options = options;
        this.node = document.createElement('table');
        this.node.style.cellSpacing = '10pt';
        this.loading = false;
        this.type = 'track';
        this.ViewClass = Class;
        this.end = false;
        this.extend = (options ? options.extend : true) || true;
    }

    Collection.prototype.next = function () {
        var fields = this.fields;
        console.log("next");
        if (this.end) {
            return;
        }
        /*
        if (!$(this.node).is(':visible')) {
            return;
        }*/
        console.log("A");
        var collection = this.collection;
        var self = this;
        console.log("Next");
        if (!collection)
            return;
        if (!this.loading && !this.end) {
            this.loading = true;
            collection.next().then(function (collection) {
                if (collection.objects.length < 1) {
                    self.end = true;
                    return;
                }
                console.log("New page fetched at collection");
                console.log(collection.objects);
                for (var i = 0; i < collection.objects.length; i++) {
                    console.log('self.node ' + self.node);

                    var track = new self.ViewClass(collection.objects[i], i, collection.uri, {'fields': fields});
                    console.log(track);
                    $(self.node).append(track.node);
                    console.log('self.node ' + self.node);
                }
                self.loading = false;
                $('div.sp-collection-next[data-uri="' + collection.uri + '"]').removeAttr('data-using');
                sync_contexts();
                if (!self.extend) {
                    self.end = true;
                }
            });
        }
    }


    var PopularityBar = function (resource) {
        this.node = document.createElement('canvas');
        this.BAR_WIDTH = 2
        this.BAR_HEIGHT = 7;
        this.SPACE = 1;
        this.popularity = resource.popularity || 0.1;
        this.colorDark = 'rgba(255, 255, 255, 0.215)'; // 'rgba(89, 89, 89)';
        this.colorLight = 'rgba(255, 255, 255, .882)'; //'rgba(225, 255, 255)';
        this.node.height = 7;
        this.node.width = 35;

    }

    PopularityBar.prototype.render = function () {
        this.backColor = 'transparent';
        var ctx = this.node.getContext('2d');
        // draw dark bars
        ctx.fillStyle = this.backColor;
        ctx.fillRect(0, 0, this.node.width, this.node.height);
        ctx.fillStyle = this.colorDark;

        var totalPigs = 0
        for (var i = 0; i < this.node.width; i+= this.BAR_WIDTH + this.SPACE) {
            ctx.fillRect(i, 0, this.BAR_WIDTH, this.BAR_HEIGHT);
            totalPigs++;
        }
        ctx.fillStyle = this.colorLight;

        var lightPigs = this.popularity * totalPigs;
        var left = 0;
        for (var i = 0; i < lightPigs; i++) {
            ctx.fillRect(left, 0, this.BAR_WIDTH, this.BAR_HEIGHT);
            left += this.BAR_WIDTH + this.SPACE;
        }
    }

    exports.PopularityBar = PopularityBar;

    exports.Collection = Collection;
    var header_types = {
        'number': '#',
        'track': 'Track',
        'duration': 'fa-clock',
        'popularity': 'fa-like'
    }
    /**
     * A tracklist view
     * @param {collection|Album|List} collection A collection
     * @param {Object} options Options
     * @constructor
     * @class
     */
    var TrackContext = function (resource, options) {
        Collection.call(this, resource, options, Track);
        this.stickyHeader = (options || options.sticky) || false;
        var table = document.createElement('table');
        this.node = table;
        this.type = 'track';
        this.loading = false;
        var headers = false;
        var fields = ['title', 'artist', 'duration', 'album'];
        this.reorder = false;
        this.resource = resource;
        this.collection = resource.tracks;
        if (options && options.headers) {
            headers = options.headers;

        }
        if (options && options.reorder) {
            this.reorder = options.reorder;
        }
        this.headers = headers;
        this.uri = resource.uri;
        if (options && options.fields) {
            fields = options.fields;
        }
        this.fields = fields;
        this.node = document.createElement('table');
        var tbody = document.createElement('tbody');
        this.node.classList.add('sp-collection');
        console.log(resource);  
        this.node.setAttribute('id', 'context_' + resource.uri.replace(/\:/g, '__'));
        this.tbody = tbody;
        this.node.appendChild(tbody);
        this.node.setAttribute('width', '100%');
        this.node.classList.add('sp-table');
        this.node.setAttribute('data-reorder', this.reorder);
        this.node.setAttribute('data-drag-index', -1);
        var thead = document.createElement('thead');
        this.thead = thead;
        var c = "";
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var title = fieldTypes[field];
            if (title.indexOf('fa-') == 0) {
                title = '<i class="fa ' + title + '"></i>';
            }
            c += "<th>" + title + '</th>';
        }
        thead.innerHTML = '<tr>' + c + '<th style="width:10%; text-align: left"></th></tr>';
        this.node.setAttribute('data-uri', resource.uri);

        var tableY = 0;
        var background = document.createElement('div');
        this.background = background;
        var listTop = this.node.offsetTop + (headers ? (thead.offsetHheight) : 0);
        $(background).css({
            'position': 'absolute',
            'z-index': -1,
            'left': this.node.style.left,
            'top': listTop + 'px',
            'height': (window.innerHeight - listTop) + 'px'
        });
        $(this.background).addClass('sp-table-background');
        var self = this;
        window.addEventListener('resize', function (event) {

            $(background).css({
                'position': 'absolute',
                'left': self.node.style.left,
                'top': listTop + 'px',
                'height': (window.innerHeight - listTop) + 'px'
            });

        });

        // To make the header hovering
        if (headers) {
            this.node.appendChild(thead);
            if (this.stickyHeader) {


                window.addEventListener('scroll', function (event) {
                    var tabbar = $('.sp-tabbar');

                    var absolutePos = $(thead).offset();
                    if (tableY == 0) {
                        tableY = absolutePos.top;
                    }
                    if ($(window).scrollTop() >= tableY - tabbar.height()) {
                        var scrollOffset = $(window).scrollTop() - tableY + tabbar.height();
                        $(thead).css({'transform': 'translate(0px, ' + (scrollOffset) + 'px)'});
                    } else {
                        $(thead).css({'transform': 'none'});

                    }
                });
            }

        }
        $(this.node).spotifize();
        collection_contexts[resource.uri] = this; // Register context here
        this.node.appendChild(createCollectionNext(resource.uri));
        setTimeout(function () {
            sync_contexts();
        }, 100);
    }

    TrackContext.prototype = Object.create(Collection.prototype);
    TrackContext.prototype.constructor = Collection;


    TrackContext.prototype.show = function () {
        $(this.node).show();

        /*  try {
         this.parentNode.appendChild(this.background);
         } catch (e) {

         }
         $(this.background).show();
         var listTop =  this.node.offsetTop + (this.headers ? (this.thead.offsetHheight) : 0);

         $(this.background).css({'position': 'absolute', 'left': this.node.style.left, 'top': listTop + 'px', 'height': (window.innerHeight - listTop) + 'px'});
         */
    }
    TrackContext.prototype.hide = function () {
        $(this).hide();
        $(this.background).hide();
    }

    exports.TrackContext = TrackContext;

    var context = new Context();
    /**
     * Inserts a set of track into the given context and notify the parent
     * @param  {Array} tracks  array of track objects
     * @param  {Integer} position Position of tracks
     * @return {void}
     */
    TrackContext.prototype.insertTracks = function (tracks, position) {
        for (var i = 0; i < tracks.length; i++) {
            var track = new Track(tracks, i, this.fields);
            $(this.tbody).eq(position + i).after(track.node);


        }

        // Announce context update
        window.parent.postMessage({
            'action': 'contextupdated',
            'uri': this.collection.uri,
            'position': position,
            'tracks': tracks
        }, '*');


    }


    function showThrobber() {
        $('#throbber').show();
    }


    function hideThrobber() {
        $('#throbber').hide();
    }

    exports.showThrobber = showThrobber;
    exports.hideThrobber = hideThrobber;


    var CollectionItem = function (object, options) {
        this.node = document.createElement('div');
        $(this.node).classList.add('col-md-4');
        var box = document.createElement('div');
        $(box).addClass('box');
        $(box).html('<div class="box-header"></div><div class="box-content"><h3><a data-uri="' + object.uri + '">' + object.name + '</a></h3><p>' +  + '</p><a class="btn btn-primary">Add</a></div>');
        $(this.node).append(box);
    }

    /**
     *
     * @param resource
     * @param options
     * @constructor
     */
    var Card = function (resource, options, type) {
        this.node = document.createElement('div');
        this.node.classList.add('sp-card-holder');
        var card = document.createElement('div');
        card.classList.add('sp-card');
        var imageDiv = document.createElement('div');
        imageDiv.classList.add('sp-card-image');

        var content = document.createElement('div');
        content.classList.add('sp-card-content');

        card.appendChild(imageDiv);
        card.appendChild(content);
        card.setAttribute('data-uri', resource.uri);
        card.setAttribute('data-id', resource.id);
        card.setAttribute('data-type', type);

        imageDiv.style.backgroundImage = 'url("' + resource.images[0].url + '")';
        content.innerHTML = '<h4><a href="/' + resource.uri.split(/\:/).slice(1).join('/') + '" data-uri="' + resource.uri + '">' + resource.name + '</a></h4>';
        content.innerHTML += '<p>' + resource.description + '</p>';
        this.node.appendChild(card);
        if ('followers' in resource) {
            content.innerHTML += '<small>' + resource.followers.count + ' followers</small>';
        }

    }

    var AlbumCardCollection = function (resource, options, coverSize) {
        Collection.call(this, resource, options, Card);
        this.coverSize = coverSize ? coverSize : 128;
        this.Class = Album;
        this.resource = resource;
        this.node = document.createElement('div');
        this.type = 'album';
        this.node.setAttribute('data-uri', resource.uri + ':albums');
        this.tbody = this.node;
        this.node.classList.add('sp-collection');
        collection_contexts[resource.uri + ':albums'] = this; // Register context here
        setTimeout(function () {
            sync_contexts();
        }, 100);
        $(this.node).append('<div class="sp-collection-next" data-uri="' + this.resource.uri + ':albuns">');

    };

    var CardCollection = function (collection, options, type) {
        Collection.call(this, collection, options, Card);
        this.Class = Card;
        this.resource = {};
        this.collection = collection;
        this.node = document.createElement('div');
        this.type = type;
        console.log(collection.uri);
        this.node.setAttribute('data-uri', collection.uri + ':' + type + 's');
        this.tbody = this.node;
        this.node.classList.add('sp-collection');
        collection_contexts[collection.uri + ':' + type + 's'] = this; // Register context here
        setTimeout(function () {
            sync_contexts();
        }, 100);
        $(this.node).append('<div class="sp-collection-next">');
    };

    CardCollection.prototype = Object.create(Collection.prototype);
    CardCollection.prototype.constructor = Collection;

    exports.Card = Card;
    exports.AlbumCardCollection = AlbumCardCollection;
    exports.CardCollection = CardCollection;


    var AlbumCollection = function (resource, options, coverSize, type) {
        Collection.call(this, resource, options, Album);

        this.coverSize = coverSize ? coverSize : 128;
        this.Class = Album;
        this.resource = resource;
        this.node = document.createElement('table');
        this.type = type || 'album';
        this.node.style.borderSpacing = '10pt';
        this.node.style.borderSpacing = '10pt';
        this.collection = resource[this.type + 's'];
        this.node.setAttribute('data-uri', resource.uri + ':' + this.type + 's');
        this.tbody = this.node;
        this.node.classList.add('sp-collection');
        collection_contexts[resource.uri + ':' + this.type + 's'] = this; // Register context here
        this.node.appendChild(createCollectionNext(resource.uri + ':' + this.type + 's'));
        console.log(this.node);
        this.next();
        setTimeout(function () {
            sync_contexts();
        }, 100);

    };

    var Feed = function (resource, options, coverSize) {
        Collection.call(this, resource, options, Post);
        this.node = document.createElement('table');
        this.coverSize = coverSize ? coverSize : 128;
        this.Class = Post;
        this.resource = resource;

        this.type = 'post';
        this.collection = resource[this.type + 's'];
        console.log("Hashtag", this.collection);

        this.node.setAttribute('data-uri', resource.uri + ':' + this.type + 's');
       
        this.node.classList.add('sp-collection');
        console.log("Hashtag uri", resource.uri);   
        collection_contexts[this.resource.uri + ':posts'] = this; // Register context here
        console.log("context", collection_contexts);
        this.node.appendChild(createCollectionNext(this.resource.uri + ':posts'));
        this.node.style.cellSpacing = '10pt';
        console.log("CF");
        setTimeout(function () {
            sync_contexts();
        }, 100);

        this.node.style.cellSpacing = '10pt';
    };

    Feed.prototype = new Collection();
    Feed.prototype.constructor = Collection;

    exports.Feed = Feed;
    var views = exports;
    var Post = function (post, options) {
        this.node = document.createElement('tbody');
        this.node.style.paddingTop = 20;
        var tr1 = document.createElement('tr');
        var tr2 = document.createElement('tr');
        this.node.classList.add('sp-card');

        var td1 = document.createElement('td');
        post.user.images = [{ url: 'http://simpleicon.com/wp-content/uploads/user-3.padding'}]
        var image = new CoverImage(post.user, 64);
        image.node.style.cssFloat = 'left';
        image.node.style.marginRight = '13pt';
        td1.appendChild(image.node);
        td1.innerHTML += '<div><h3><a href="" data-uri="bungalow:user:' + post.user.id + '">' + post.user.name + '</a></h3>' + '<p>' + post.message.bungalowize() + '</p></div>';
        console.log("Fag");
        var resourceType = post.resource.type;
        console.log(resourceType.capitalizeFirstLetter());
        var ViewResource = views[resourceType.capitalizeFirstLetter()];
        console.log(views);

        var Resource = models[resourceType.capitalizeFirstLetter()];

        this.resource = Resource.fromURI(post.resource.uri);
        console.log(ViewResource);
        var resource = new ViewResource(this.resource);
        tr1.appendChild(td1);
        this.node.appendChild(tr1);
        this.node.appendChild(resource.node);
        td1.style.paddingBottom = '10pt';
		
		var tr2 = document.createElement('tr');
		var td2 = document.createElement('td');
		tr2.appendChild(td2);
		td2.appendChild(resource.node);
		resource.node.nodeName = 'table';
		this.node.appendChild(td2);

    }

    exports.Post = Post;

    function createCollectionNext(uri, type) {
        type = type ? type : 'tbody';
        var collectionNext = document.createElement(type);
        collectionNext.setAttribute('data-uri', uri);
        collectionNext.classList.add('sp-collection-next');
        return collectionNext;
    }

    AlbumCollection.prototype.next = function () {
        Collection.prototype.next.call(this, arguments);
    }


    /**
     * Represents an album view
     * @param {Album} album Album instance
     * @param {Object} options options
     * @class
     * @constructor
     */
    var Album = function (album, options) {
        var self = this;
        this.node = document.createElement('tbody');
        self.node.setAttribute('width', '100%');
        self.node.classList.add('sp-album');
        self.node.classList.add('sp-context');
        var tr = document.createElement('tr');
        if (album instanceof models.Album) {

        } else {
            album = new models[album.type.capitalizeFirstLetter()](album);
        }
        album.load('name', 'artists', 'images', 'albums').then(function (album) {
            
            var tbody = document.createElement('tbody');
            var tr1 = document.createElement('tr');
       		tr1.style.marginBottom = '10pt';
            self.loading = false;
            // // console.log("ALBUM", album);
            self.node.setAttribute('data-uri', album.uri);
            var td1 = document.createElement('td');

        	td1.style.paddingLeft = '30pt';
            console.log(album);
            var image = '';
            if ('images' in album && album.images.length > 0) {
                image = album.images[0].url;
            }
            td1.innerHTML = '<a data-uri="' + album.uri + '"><img class="shadow" data-uri="' + (album.uri) + '" src="' + image + '" width="192px"></a>';
			if ('description' in album) {
				td1.innerHTML += '<p>' + album.description.bungalowize() + '</p>';
			}
            td1.setAttribute('valign', 'top');
            td1.setAttribute('width', '170px');
            td1.style.paddingRight = '13pt';
            var tr2 = document.createElement('tr');
            var td2 = document.createElement('td');
            td2.setAttribute('valign', 'top');
            album.release_date = '1970-01-01';
            td2.innerHTML = '<h2 style="margin-bottom: 10px"><a data-uri="' + album.uri + '">' + album.name + '</a> %s </h2>';
            
            if ('owner' in album) {
                td2.innerHTML = td2.innerHTML.replace('%s', '   by <a data-uri="bungalow:user:' + album.owner.id + '">' + album.owner.id + '</a>');

            } else {
                td2.innerHTML = td2.innerHTML.replace('%s', '');
            }

            // // console.log(td2.innerHTML);
            //alert(album.tracks);
           

            var context = new TrackContext(album, {headers: false, 'fields': ['image', 'title', 'duration', 'popularity', 'artist']});
            var tr2 = document.createElement('tr');
            var tdtracks = document.createElement('td');
            tdtracks.setAttribute('colspan', 2);
            tr.appendChild(td1);
            tr.appendChild(td2);
            self.node.appendChild(tr);
            // // console.log(table);
            self.node.style.marginBottom = '26pt';
            self.node.style.marginTop = '26pt';
            self.node.style.paddingLeft = '26pt';
            td2.appendChild(context.node);
        });


    }

    /**
     * Represents an album view
     * @param {Album} album Album instance
     * @param {Object} options options
     * @class
     * @constructor
     */
    var Playlist = function (playlist, options) {
        var self = this;
        this.node = document.createElement('tr');
        
        if (playlist instanceof models.Playlist) {

        } else {
            playlist = new models[playlist.type.capitalizeFirstLetter()](playlist);
        }
        playlist.load('name', 'artists', 'images', 'tracks').then(function (playlist) {
            self.node.setAttribute('width', '100%');
            self.node.classList.add('sp-playlist');
            self.node.classList.add('sp-context');
            var tbody = document.createElement('tbody');
            var tr1 = document.createElement('tr');
            self.loading = false;
            // // console.log("ALBUM", playlist);
            self.node.setAttribute('data-uri', playlist.uri);
            var td1 = document.createElement('td');
            console.log(playlist);
            var image = '';
            if ('images' in playlist && playlist.images.length > 0) {
                image = playlist.images[0].url;
            }
            td1.innerHTML = '<a data-uri="' + playlist.uri + '"><img class="shadow" data-uri="' + (playlist.uri) + '" src="' + image + '" width="192px"></a>';
            td1.setAttribute('valign', 'top');
            td1.setAttribute('width', '170px');
            td1.style.paddingRight = '13pt';
            var tr2 = document.createElement('tr');
            var td2 = document.createElement('td');
            td2.setAttribute('valign', 'top');
            playlist.release_date = '1970-01-01';
            td2.innerHTML = '<small>' + playlist.release_date + '</small><h3 style="margin-bottom: 10px"><a data-uri="' + playlist.uri + '">' + playlist.name + '</a> - by <a data-uri="bungalow:user:' + playlist.owner.id + '">' + playlist.owner.name + '</a> </h3>';
            
            if ('owner' in playlist) {
                td2.innerHTML = td2.innerHTML.replace('%2', '   by <a data-uri="bungalow:user:' + playlist.owner.id + '">' + playlist.owner.id + '</a>');

            } else {
                td2.innerHTML = td2.innerHTML.replace('%2', '');
            }

            // // console.log(td2.innerHTML);
            //alert(playlist.tracks);
           

            var context = new TrackContext(playlist, {headers: false, 'fields': ['image', 'title', 'duration', 'popularity', 'artist']});
            var tr2 = document.createElement('tr');
            var tdtracks = document.createElement('td');
            tdtracks.setAttribute('colspan', 2);
            self.node.appendChild(td1);
            self.node.appendChild(td2);
            // // console.log(table);
            self.node.style.marginBottom = '26pt';
            self.node.style.marginTop = '26pt';
            self.node.style.paddingLeft = '26pt';
            td2.appendChild(context.node);
        });


    }

    exports.Playlist = Playlist;

    exports.Album = Album;

    exports.AlbumCollection = AlbumCollection;

    AlbumCollection.prototype = Object.create(Collection.prototype);
    AlbumCollection.prototype.constructor = Collection;

    $(document).on('dragstart', '.sp-track', function (event) {
        $.event.props.push('dataTransfer');
        var $collection = $('.sp-table[data-uri="' + $(this).attr('data-context-uri') + '"]');

        var contextUri = $collection.attr('data-uri');
        var startIndex = $('.sp-table[data-uri="' + contextUri + '"] tbody tr').index(this);
        $collection.attr('data-drag-start-index', startIndex);
        $collection.attr('data-drag-new-index', startIndex);
        event.originalEvent.dataTransfer.effectAllowed = 'copyMove';
        event.originalEvent.dataTransfer.dropEffect = 'copyMove';
        var uris = "";
        var $tracks = $('.sp-track-selected').each(function (i) {
            uris += $(this).attr('data-uri') + "\n";
        });
        event.originalEvent.dataTransfer.setData('text/uri-list', uris);
        event.originalEvent.dataTransfer.setData('text', uris);
        // // console.log("Drag started");
    });
    $(document).on('dragleave', '.sp-track', function (event) {
        $(this).removeClass('sp-track-dragover');
    });
    $(document).on('dragenter', '.sp-track', function (event) {
        $.event.props.push('dataTransfer');
        var $collection = $('.sp-table[data-uri="' + $(this).attr('data-context-uri') + '"]');
        if ($collection.attr('data-reorder') === 'true') {

            event.originalEvent.dataTransfer.effectAllowed = 'copyMove';
            event.originalEvent.dataTransfer.dropEffect = 'copyMove';
            var index = $collection.find('tr').index($(this));
            // // console.log("Element index below ", index);
            $collection.attr('data-drag-new-index', index);
            $(this).addClass('sp-track-dragover');
        }
    });
    $(document).on('dragover', '.sp-track', function (event) {
        event.originalEvent.preventDefault();
    });
    $(document).on('dragend', '.sp-track', function (event) {
        $.event.props.push('dataTransfer');
        event.originalEvent.preventDefault();

    });
    // from http://stackoverflow.com/questions/487073/check-if-element-is-visible-after-scrolling
    function isScrolledInto(elem)
    {
        var $elem = $(elem);
        var $window = $(window);

        var docTop = $window.scrollTop();
        var docBottom = docTop + $window.height();

        var elemTop = $elem.offset().top;
        var elemBottom = elemTop + $elem.height();

        return ((elemBottom <= docBottom) && (elemTop >= docTop));
    }

    $(document).on('drop', '.sp-track', function (event) {
        $.event.props.push('dataTransfer');
        var contextUri = $(this).attr('data-context-uri');
        // // console.log(contextUri);
        var $collection = $('.sp-table[data-uri="' + contextUri + '"]');
        var $tracks = $('.sp-table[data-uri="' + contextUri + '"] tbody tr');
        var newIndex = $tracks.index($(this));
        // // console.log(this.innerHTML);

        // // console.log(newIndex, $collection.attr('data-drag-start-index'));

        if ($collection.attr('data-reorder') === 'true') {
            var startIndex = $collection.attr('data-drag-start-index');

            // If we are at the same collection
            if (startIndex > -1) {
                // Get all selected tracks uris
                var uris = [];
                var $selectedTracks = $('.sp-table[data-uri="' + contextUri + '"] tbody tr.sp-track-selected');
                $selectedTracks.each(function (i) {
                    uris += $(this).attr('data-uri');
                });

                var indicies = [];
                $selectedTracks.each(function (i) {
                    indicies.push($tracks.index(this));
                });
                // Remove all selected songs
                $selectedTracks.remove();
                for (var i = 0; i < $selectedTracks.length; i++) {

                    $collection.find('tbody').insertAt($selectedTracks[i], newIndex + i);
                }

                // notify parent
                window.parent.postMessage({
                    'action': 'reorderedtracks',
                    'context': contextUri,
                    'oldIndex': startIndex,
                    'newIndex': newIndex,
                    'indicies': indicies,
                    'uris': uris
                }, '*');
            }
        }
        $collection.attr('data-drag-start-index', -1);
        $collection.attr('data-drag-new-index', -1);
    });
    $(document).on('click', 'a[data-uri]', function (event) {
        // // console.log("clicked link", event.target);
        parent.postMessage({'action': 'navigate', 'uri': event.target.getAttribute('data-uri')}, '*');
    });

    $(document).on('mousedown', '.sp-track', function (event) {
        $('.sp-track').removeClass('sp-track-selected');
        $(this).addClass('sp-track-selected');
    });


    /**
     * Bungalow specification
     **/
    var Bungalow = function (section, options) {
        this.node = document.createElement('iframe');
    }

    function sync_contexts() {

        $('.sp-collection-next').each(function (i) {
            var endofpage = this.getBoundingClientRect().bottom;
            var bottom = ($(window).height() );

            if (endofpage <= bottom && !this.hasAttribute('data-using')) {
                $(this).remove();
                console.log("New " + this.getAttribute('data-uri') + " reached");
                var collection = collection_contexts[this.getAttribute('data-uri')];
                console.log(collection);
                $(this).attr('data-using', 'true');
                collection.next();


            }

        })
    }

    String.prototype.bungalowize = function () {
        var str = this.replace(/href\=/, 'data-uri=');
        str = str.replace(/\#([a-zA-Z0-9]+)/g, '<a data-uri="bungalow:hashtag:$1">#$1</a>');
        str = str.replace(/\@([a-zA-Z0-9]+)/g, '<a data-uri="bungalow:user:$1">@$1</a>');
        return str;
    }

    var SimpleHeader = function (resource, options) {
        this.node = document.createElement('table');
        this.node.cellPadding = '10px';
        this.node.style.width = '100%';
        var tr1 = document.createElement('tr');
        var tr2 = document.createElement('tr');

        this.node.appendChild(tr1);
        var td1 = document.createElement('td');
        td1.setAttribute('rowspan', 3);
        var image = new CoverImage(resource, 128);
        var td2 = document.createElement('td');
        td2.appendChild(image.node);
        console.log(resource);
        td2.innerHTML = 

        '<small class="sp-type">' + resource.type.toUpperCase() + '</small>' +
        '<h2><a data-uri="' + resource.uri + '">' + resource.name + '</a> by <a data-uri="' + resource.owner.uri + '">' + resource.owner.id + '</a></h2>' +
        //'<p>Created by <a data-uri="bungalow:user:' + resource.owner.id + '">' + resource.owner.display_name + '</a></p>' +
        '<p>' + resource.description.bungalowize(); + '</p>';
        
        td2.style.verticalAlign = 'top';
        td1.style.verticalAlign = 'top';
        td1.width = '128px';
        tr1.appendChild(td1);
        td1.appendChild(image.node);
        tr1.appendChild(td2);
        this.node.appendChild(td2);
        this.node.appendChild(tr2);


        var td3 = document.createElement('td');
        //tr2.appendChild(td3);
        var toolbar = document.createElement('div');
        toolbar.classList.add('sp-toolbar');
        var playButton = document.createElement('button');
        playButton.innerHTML = 'Play';
        playButton.classList.add('btn');
        playButton.classList.add('btn-primary');

        var followButton = document.createElement('button');
        followButton.innerHTML = 'Follow';
        followButton.classList.add('btn');

        toolbar.appendChild(playButton);
        toolbar.appendChild(followButton);
        td3.appendChild(toolbar);
    }

    exports.SimpleHeader = SimpleHeader;

    var CoverImage = function (resource, size) {
        this.node = document.createElement('div');
        this.node.classList.add('sp-cover-image');
        this.node.style.width = size + 'px';
        this.node.style.height = size + 'px';
        console.log("Width", this.node.style.width);
        if ('icon' in resource) {
            var coverdefault = document.createElement('sp-cover-default');
            var iconDiv = document.createElement('span');
            iconDiv.classList.add('sp-cover-icon');
            if (resource.icon.indexOf('fa-') == 0) {
                iconDiv.classList.add('fa');
                iconDiv.classList.add('fa-' + resource.icon);
            } else {
                iconDiv.innerHTML = resource.icon;
            }
            coverdefault.appendChild(iconDiv);
            this.node.appendChild(coverdefault);
        }


        var image = document.createElement('div');
        var size = size ? size : 128;
        if ('images' in resource && resource.images.length > 0)
            image.style.backgroundImage = 'url("' + resource.images[0].url + '")';
        image.style.width = size + 'px';
        image.style.height = size + 'px';
        this.node.appendChild(image);

    }

    var Header = function (resource, options, coverSize) {
        Object.assign(this, options);
        var type = options.type;
        coverSize = coverSize ? coverSize : 128;
        this.node = document.createElement('div');
        this.node.classList.add('sp-header');
        var bgdiv = document.createElement('div');
        if ('images' in resource && resource.images > 0) {
            bgdiv.style.backgroundImage = 'url("' + resource.images[0].url + '")';
        } else {
            bgdiv.style.backgroundImage = 'url("")';
        }
        bgdiv.classList.add('sp-header-background-image');
        this.node.appendChild(bgdiv);

        var content = document.createElement('div');
        this.node.appendChild(content);
        content.classList.add('content');

        var table = document.createElement('table');
        content.appendChild(table);
        table.classList.add('sp-header-content');

        var tbody = document.createElement('tbody');

        table.appendChild(tbody);

        var tr = document.createElement('tr');

        tbody.appendChild(tr);

        var td1 = document.createElement('td');

        var coverImage = new CoverImage(resource, coverSize);
        td1.width = "10px";
        td1.appendChild(coverImage.node);

        var td2 = document.createElement('td');
        td2.style.verticalAlign = 'top';
        td2.innerHTML = '<small class="sp-type">' + type.toUpperCase() + '</small><h2>' + resource.name + '</h2>';

        var toolbar = document.createElement('div');
        toolbar.classList.add('sp-toolbar');

        td2.appendChild(toolbar);

        var td3 = document.createElement('td');
        td3.setAttribute('width', '100pt');


        toolbar.innerHTML = '<button class="btn btn-primary">Play</button><button class="btn btn-default">Follow</button>';


        tr.appendChild(td1);
        tr.appendChild(td2);

        tr.appendChild(td3);

        if ('tabs' in this) {
            this.tabbar = new TabBar(this.tabs);
            this.node.style.overflow = 'visible';
            this.node.appendChild(this.tabbar.node);
            this.tabbar.node.style.top = 'calc(100% - 60pt)';

        }

        $('.sp-tabbar').css({'background-opacity': '0'});
        var tabbar = $('.sp-tabbar')[0];
        if (tabbar) {
            this.tabbar = tabbar;
            var self = this;
            window.addEventListener('scroll', function () {
                var headerClientRect = self.node.getBoundingClientRect();
                var tabbarClientRect = $('.sp-tabbar')[0].getBoundingClientRect();
                var tabbarHeight = tabbarClientRect.bottom - tabbarClientRect.top;
                var headerEnd = $(window).scrollTop() - headerClientRect.bottom;
                var headerTabbarProportion = headerEnd / tabbarClientRect.bottom;
                console.log(headerTabbarProportion);
                if (headerTabbarProportion < 1) {
                    $('.sp-tabbar').css({'background-opacity':  headerTabbarProportion});
                } else {
                    $('.sp-tabbar').css({'background-opacity': 1});
                }

            });
        }

        content.style.top =  '-150px';
        content.style.left =  '30px';
    }

    var TabBar = function (data) {
        this.node = document.createElement('div');
        this.node.classList.add('sp-tabbar');
        var self = this;
        Object.assign(this, data);
        for (var i = 0; i < this.views.length; i++) {
            var tabItem = new TabBarTab(this.views[i]);
            this.node.appendChild(tabItem.node);
            if (i == 0) {
                tabItem.node.classList.add('sp-tabbar-tab-active');
            }
        }
        var tabbarY = 0;
        this.slicky = false;
        window.addEventListener('scroll', function (event) {
            var tabbar = $(self.node)   ;

            var absolutePos = $(tabbar).offset();
            if (tabbarY == 0) {
                tabbarY = absolutePos.top;
            }
            console.log($(window).scrollTop() > tabbarY)
            if ($(window).scrollTop() >= tabbarY - (self.slicky ? $(tabbar).height(): 0)) {
                var scrollOffset = $(window).scrollTop() - tabbarY  ;
                console.log("Scrolloffset: " + scrollOffset);
                var translate = 'translate(0px, ' + (scrollOffset) + 'px)';
                $(tabbar).css({'transform': translate});
            } else {
                $(tabbar).css({'transform': 'translate(0px, -' + (self.slicky ? 100 : 0) + '%)' });

            }
        });
        var tabbar = $(this.node);
        if ($(window).scrollTop() >= tabbarY - (self.slicky ? $(tabbar).height() * 2: 0)) {
            var scrollOffset = $(window).scrollTop() - tabbarY - (self.slicky ? $(tabbar).height() :0)  ;
            $(tabbar).css({'transform': 'translate(0px, -' + (scrollOffset) + 'px)'});
        } else {
            $(tabbar).css({'transform': 'translate(0px, -' + (this.slicky ? 100 : 0) + '%)' });

        }
    }

    var TabBarTab = function (data) {
        Object.assign(this, data);
        this.node = document.createElement('span');
        this.node.setAttribute('class', 'sp-tabbar-tab');
        this.node.setAttribute('data-id', data.id);
        $(this.node).html(this.title);
        this.node.addEventListener('click', function (event) {
            window.parent.postMessage({'action': 'hashchange', hash: event.target.dataset['id']}, '*');
        });
    }

    exports.Header = Header;
    exports.TabBar = TabBar;
    exports.TabBarTab = TabBarTab;
    $(window).scroll(function () {
        sync_contexts();
    });

    window.addEventListener('message', function (event) {
        var data = event.data;
        if (data.action == 'hashchange') {
            set(data.hash);
        }
    });

    function set (viewId) {
        $('.sp-tabbar-tab').removeClass('sp-tabbar-tab-active');
        $('.sp-tabbar-tab[data-id="' + viewId + '"]').addClass('sp-tabbar-tab-active');
        $('.sp-section').hide();
        $('.sp-section#' + viewId).show();
    }
    return exports;
});