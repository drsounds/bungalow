require(['$api/models'], function (models) {
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

    var activeViews = {};


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
    var TrackView = function (track, index, contextUri, options) {
        this.node = document.createElement('tr');
        this.node.classList.add('sp-track');
        this.node.setAttribute('data-uri', track.uri);
        this.node.setAttribute('data-context-uri', contextUri);
        this.node.setAttribute('data-object', JSON.stringify(track));
        this.node.setAttribute('draggable', true);
        this.node.setAttribute('data-track-index', index);


        for (var i = 0; i < options.fields.length; i++) {
            var field = options.fields[i];
            if (field === 'title') {
                var td1 = document.createElement('td');
                td1.innerHTML = track.name;
                this.node.appendChild(td1);
            }
            if (field === 'artist') {
                var td2 = document.createElement('td');

                td2.innerHTML = '<a data-uri="' + (track.artists[0].uri || track.artists[0].link) + '">' + track.artists[0].name + '</a>';
                this.node.appendChild(td2);
            }
            if (field === 'duration') {
                var td3 = document.createElement('td');
                td3.innerHTML = '<span class="fade" style="float: right">' + toMS(track.duration) + '</span>';
                this.node.appendChild(td3);
            }
            if (field === 'album') {
                var td4 = document.createElement('td');
                td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.album.link + '">' + track.album.name + '</a>';
                this.node.appendChild(td4);
            }
            if (field === 'popularity') {
                var td4 = document.createElement('td');
                td4.innerHTML = '<meter min="0" style="max-width:35px; vertical-align: 0px;" max="100" value="' + track.popularity + '">';
                this.node.appendChild(td4);
            }
            if (field === 'user') {
                var td4 = document.createElement('td');
                td4.innerHTML = '<a href="javascript:void()" data-uri="' + track.user.uri + '">' + track.user.name + '</a>';
                this.node.appendChild(td4);
            }

        }

        var td5 = document.createElement('td');
        td5.innerHTML = '&nbsp;';
        this.node.appendChild(td5);
        if ('availability' in track && track.availability !== 1) {
            $(this.node).addClass('sp-track-unavailable');
        }

    }

    exports.TrackView = TrackView;

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
        'title': 'Title',
        'album': 'Album',
        'artist': 'Artist',
        'duration': 'Duration',
        'popularity': 'Popularity',
        'user': 'User',
        'creted': 'Added'
    };

    var CollectionView = function (resource, options, ViewClass) {
        this.resource = resource;
        this.options = options;
        this.node = document.createElement('div');
        this.tbody = this.node;
        this.loading = false;
        this.type = 'track';
        this.ViewClass = ViewClass;
        this.end = false;
    }

    CollectionView.prototype.next = function () {
        var tbody = this.tbody;
        var fields = this.fields;
        var collection = this.resource[this.type + 's'];
        var self = this;
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

                for (var i = 0; i < collection.objects.length; i++) {
                    var trackView = new self.ViewClass(collection.objects[i], i, collection.uri, {'fields': fields});
                    $(tbody).append(trackView.node);
                    self.loading = false;
                }
            });
        }
    }

    exports.CollectionView = CollectionView;

    /**
     * A tracklist view
     * @param {collection|Album|List} collection A collection
     * @param {Object} options Options
     * @constructor
     * @class
     */
    var TrackContextView = function (resource, options) {
        CollectionView.call(this, resource, options, TrackView);
        this.stickyHeader = false;
        var table = document.createElement('table');
        this.node = table;
        this.type = 'track';
        this.loading = false;
        var headers = false;
        var fields = ['title', 'artist', 'duration', 'album'];
        this.reorder = false;
        this.resource = resource;
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
            c += "<th>" + fieldTypes[field] + '</th>';
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

        })
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
                    if ($(window).scrollTop() >= tableY - tabbar.height() - 2) {
                        var scrollOffset = $(window).scrollTop() - tableY + tabbar.height() + 2;
                        $(thead).css({'transform': 'translate(0px, ' + (scrollOffset) + 'px)'});
                    } else {
                        $(thead).css({'transform': 'none'});

                    }
                });
            }

        }
        $(this.node).spotifize();
        collection_contexts[resource.uri] = this; // Register context here
        setTimeout(function () {
            sync_contexts();
        }, 100);
    }

    TrackContextView.prototype = Object.create(CollectionView.prototype);
    TrackContextView.prototype.constructor = CollectionView;


    TrackContextView.prototype.show = function () {
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
    TrackContextView.prototype.hide = function () {
        $(this).hide();
        $(this.background).hide();
    }

    exports.TrackContextView = TrackContextView;

    var context = new Context();
    /**
     * Inserts a set of track into the given context and notify the parent
     * @param  {Array} tracks  array of track objects
     * @param  {Integer} position Position of tracks
     * @return {void}
     */
    TrackContextView.prototype.insertTracks = function (tracks, position) {
        for (var i = 0; i < tracks.length; i++) {
            var trackView = new TrackView(tracks, i, this.fields);
            $(this.tbody).eq(position + i).after(trackView.node);


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
        $(box).html('<div class="box-header"></div><div class="box-content"><h3><a data-uri="' + object.uri + '">' + object.name + '</a></h3><p>' + object.description + '</p><a class="btn btn-primary">Add</a></div>');
        $(this.node).append(box);
    }

    var AlbumCollectionView = function (resource, options) {
        CollectionView.call(this, resource, options, AlbumView);
        this.ViewClass = AlbumView;
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

    }

    /**
     * Represents an album view
     * @param {Album} album Album instance
     * @param {Object} options options
     * @class
     * @constructor
     */
    var AlbumView = function (album, options) {
        var table = document.createElement('table');
        var tbody = document.createElement('tbody');
        table.setAttribute('width', '100%');
        table.classList.add('sp-album');
        table.classList.add('sp-context');
        var tr1 = document.createElement('tr');
        this.loading = false;
        // // console.log("ALBUM", album);
        table.setAttribute('data-uri', album.uri);
        var td1 = document.createElement('td');
        td1.innerHTML = '<img class="shadow" data-uri="' + (album.uri) + '" src="' + album.images[0].url + '" width="256px">';
        td1.setAttribute('valign', 'top');
        td1.setAttribute('width', '170px');
        td1.style.paddingRight = '13pt';
        var tr2 = document.createElement('tr');
        var td2 = document.createElement('td');
        td2.setAttribute('valign', 'top');
        td2.innerHTML = '<h3 style="margin-bottom: 10px"><a data-uri="' + album.uri + '">' + album.name + '</a></h3>';
        // // console.log(td2.innerHTML);
        //alert(album.tracks);
        var self = this;

        var contextView = new TrackContextView(album, {headers:true, 'fields': ['title', 'duration', 'popularity']});
        var tr2 = document.createElement('tr');
        var tdtracks = document.createElement('td');
        tr2.appendChild(tdtracks);
        tdtracks.setAttribute('colspan', 2);
        tr1.appendChild(td1);
        tr1.appendChild(td2);
        tbody.appendChild(tr1);
        tbody.appendChild(tr2);
        table.appendChild(tbody);
        // // console.log(table);
        this.node = table;
        this.node.style.marginBottom = '26pt';
        this.node.style.marginTop = '26pt';
        this.node.style.paddingLeft = '26pt';
        tdtracks.appendChild(contextView.node);


    }

    exports.AlbumView = AlbumView;

    exports.AlbumCollectionView = AlbumCollectionView;

    AlbumCollectionView.prototype = Object.create(CollectionView.prototype);
    AlbumCollectionView.prototype.constructor = CollectionView;

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
    function isScrolledIntoView(elem)
    {
        var $elem = $(elem);
        var $window = $(window);

        var docViewTop = $window.scrollTop();
        var docViewBottom = docViewTop + $window.height();

        var elemTop = $elem.offset().top;
        var elemBottom = elemTop + $elem.height();

        return ((elemBottom <= docViewBottom) && (elemTop >= docViewTop));
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
        $('.sp-collection').each(function (i) {
            var endofpage = this.getBoundingClientRect().bottom;
            var bottom = ($(window).height() );


            if (endofpage <= bottom) {
                console.log("New " + this.getAttribute('data-uri') + " reached");
                var collectionView = collection_contexts[this.getAttribute('data-uri')];
                console.log(collectionView);
                collectionView.next();


            }
        })
    }

    var Header = function (resource) {
        this.node = document.createElement('div');
        this.node.classList.add('sp-header');
        this.node.style.backgroundImage.src = resource.images[0].src;
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
    }

    exports.Header = Header;

    $(window).scroll(function () {
        sync_contexts();
    });
});