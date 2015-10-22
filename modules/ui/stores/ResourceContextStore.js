var AppDispatcher = require('../dispatcher/AppDispatcher.js');
var EventEmitter = require('events').EventEmitter;
var ResourceContextConstants = require('../constants/ResourceContextConstants.js');
var assign = require('object-assign');

var CHANGE_EVENT = 'change';

var _resources = {}; // collection of resource items


/**
 * Delete a RESOURCE item.
 * @param {string} id
 */
function destroy(id) {
  delete _resources[id];
}

var ResourceContextStore = assign({}, EventEmitter.prototype, {

  /**
   * Get the entire collection of RESOURCEs.
   * @return {object}
   */
  getResourcesForContext: function(uri) {
    return _resources[uri];
  },

  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  selectRows: function (contextUri, indicies) {
    for (var i = 0; i < resources[contextUri].length; i++)
        resources[contextUri][i].selected = false;
    _resources[contextUri][indicies[0]].selected = true;
  },

  subscribe: function (contextUri) {
    if (contextUri == 'bungalow:context:debug') {
        _resources[contextUri] = [
            {
                uri: 'bungalow:track:test',
                authors: [
                    {
                        uri: 'bungalow:artist:test',
                        name: 'Dr. Sounds'
                    }
                ],
                album: {
                    name: 'Music for Sleep',
                    author: {
                        uri: 'bungalow:artist:test',
                        name: 'Dr. Sounds'
                    }
                },
                duration: 60 * 28,
                popularity: 0.1,
                added_by: {
                    name: 'Dr. Sounds',
                    id: 'drsounds',
                    uri: 'bungalow:user:drsounds'
                },
                added: new Date().getTime(),
                list: {
                    name: 'Test playlist',
                    id: '234512323523523',
                    uri: 'bungalow:playlist:234512323523523'
                }
            }
        ];
        ResourceContextStore.emitChange();
    }
  },

  /**
   * @param {function} callback
   */
  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  /**
   * @param {function} callback
   */
  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  },

  dispatcherIndex: AppDispatcher.register(function(payload) {
    var action = payload.action;
    var text;

    switch(action.actionType) {
      case ResourceContextStore.RESOURCE_CONTEXT_SUBSCRIBE:
        var contextUri = payloads.contextUri;
        ResourceContextStore.subscribe(contextUri);

        break;
      case ResourceContextStore.RESOURCE_CONTEXT_SELECT_ROWS:
        var indicies = paylods.indicies;
        var contextUri = payloads.contextUri;
        ResourceContextStore.selectRows(contextUri, indicies);
        ResourceContextStore.emitChange();

        break;
      case ResourceContextConstants.RESOURCE_CONTEXT_CREATE:
        text = action.text.trim();
        if (text !== '') {
          create(text);
          ResourceContextStore.emitChange();
        }
        break;

      case ResourceContextConstants.RESOURCE_CONTEXT_DESTROY:
        destroy(action.id);
        ResourceContextStore.emitChange();
        break;

      // add more cases for other actionTypes, like RESOURCE_UPDATE, etc.
    }

    return true; // No errors. Needed by promise in Dispatcher.
  })

});

module.exports = ResourceContextStore;