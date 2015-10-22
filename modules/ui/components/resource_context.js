var React = require('react');
var Resource = require('./resource.js');
var ResourceContextStore = require('../stores/ResourceContextStore.js');
var ResourceContextActions = require('../actions/ResourceContextActions.js');

function getResourceContextState(uri) {
    return {
        resources: ResourceContextStore.getResourcesForContext(uri)
    }
}

/**
 * ResourceContext is a table component representing a list of arbitrary resources (mostly tracks).
 * @param  {[type]}   )                    {                                 return this.props.columnHeaders.map((column) [description]
 * @param  {[type]}   React.DOM.span(                                                                                                                                             {                        className: 'fa fa-' + column                    },                    column                )            );         });    } [description]
 * @param  {Function} _onResourceSelected: function      (resource, i) {                                                                this.state.resources [description]
 * @return {[type]}                        [description]
 */
var ResourceContext = React.createClass ({

    renderColumnHeaders: function() {
        return this.props.columnHeaders.map((column) => {
            return React.DOM.th(
                {
                    key: 'columnheader-' + column,
                },
                React.DOM.span(
                    {
                        className: 'fa fa-' + column
                    },
                    column
                )
            ); // TODO add localization
        });
    },

    getInitialState: function() {
        return {
            resources: []
        }
    },

    componentDidMount: function() {
        ResourceContextStore.addChangeListener(this._onChange);
        ResourceContextActions.subscribe(this.props.uri);
    },

    componentWillUnmount: function() {
        ResourceContextStore.removeChangeListener(this._onChange);
    },

    _onSelectResource: function (resource, i) {
        ResourceStoreActions.selectResources(this.props.uri, [i]);
    },

    _onActivateResource: function (resource, i) {
        ResourceStoreActions.activateResource(this.props.uri, i);
    },


    _onChange: function() {
        this.setState(getResourceContextState(this.props.uri));
    },

    renderChildren: function() {
        console.log(this.state.resources);
        var resources = this.state.resources.map((resource, i) => {
            var t = React.createElement(
                Resource, 
                {
                    key: 'resource-' + i,
                    index: i,
                    resource: resource,
                    fields: this.props.columnHeaders,
                    onSelectResource: this._onSelectResource,
                    onActivateResource: this._onActivateResource
                }
            );
            console.log(t);
            return t;
        });
        console.log(resources);
        return resources;
    },

    render: function() {
       
        return (
            React.DOM.table(
                {
                    className: 'sp-table',
                    width: '100%'
                },
                React.DOM.thead(null,
                    React.DOM.tr(null,
                        this.renderColumnHeaders()
                        
                    )
                ),
                React.DOM.tbody(null,
                    this.renderChildren()
                )
            )
        )
    }
});

ResourceContext.propTypes = {
    uri: React.PropTypes.string.isRequired,
    columnHeaders: React.PropTypes.array.isRequired
};

module.exports = ResourceContext;