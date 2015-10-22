/**
 * ResourceContextActions
 */

var AppDispatcher = require('../dispatcher/AppDispatcher.js');
var ResourceContextConstants = require('../constants/ResourceContextConstants.js');

var ResourceContextActions = {

  /**
   * @param  {string} text
   */
  create: function(text) {
    AppDispatcher.handleViewAction({
      actionType: ResourceContextConstants.RESOURCE_CONTEXT_CREATE,
      text: text
    });
  },

  /**
   * @param  {string} id
   */
  destroy: function(id) {
    AppDispatcher.handleViewAction({
      actionType: ResourceContextConstants.RESOURCE_CONTEXT_DESTROY,
      id: id
    });
  },

  selectRows: function (contextUri, indices) {
    AppDispatcher.handleViewAction({
      actionType: ResourceContextConstants.RESOURCE_CONTEXT_SELECT_ROW,
      contextUri: contextUri,
      indicies: indicies
    });
  },

  subscribe: function (contextUri) {
    AppDispatcher.handleViewAction({
      actionType: ResourceContextConstants.RESOURCE_CONTEXT_SUBSCRIBE,
      contextUri: contextUri
    });
  }

};

module.exports = ResourceContextActions;