const {AppDispatcher} = require('../dispatchers/AppDispatcher');
const {MusicConstants} = require('../constants/MusicConstants');
const {Store} = require('./Store');


class _MusicStore extends Store {
    constructor() {
        super();
        this.state = {
            resources: {},
            player: {
                is_playing: false,
                item: null,
                context: null
            }
        }    
    }
    /**
     * Get libraries for book with ISBN
     * @param String isbn The ISBN number for the book to look up
     * @async
     * @return An book object
     **/
    async getResourceByUri(uri) {
        let result = await fetch('/api/music/' + uri.substr(uri.split(':')[0].length).replace(/\:/, '/')).then((result) => result.json());
        
        this.state.resources[uri] = result;
        this.emitChange();    
    }
    
}


export let MusicStore = new _MusicStore();


AppDispatcher.register((payload) => {
    if (payload.actionType === MusicConstants.GET_RESOURCE) {
        MusicStore.getLibrariesForBook(payload.uri);
    }
});
