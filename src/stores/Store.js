const {AppDispatcher} = require('../dispatchers/AppDispatcher');
const {PlayerConstants} = require('../constants/PlayerConstants');
const {Store} = require('./Store');


class _PlayerStore extends Store {
    constructor() {
        super();
        this.state = {
            resources: {}
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


export let MusicStore = new _PlayerStore();


AppDispatcher.register((payload) => {
    if (payload.actionType === PlayerConstants.GET_RESOURCE) {
        MusicStore.getLibrariesForBook(payload.uri);
    }
});
