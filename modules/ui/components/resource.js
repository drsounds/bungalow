var React = require('react');
// var PopularityBar = require('./popularity_bar.js');
var Link = require('./link.js');
var Resource = React.createClass({
    componentDidMount: function() {
      this.setState({active: false});
    },
    renderFields: function () {
        return this.props.fields.map((field, i) => {

                console.log(field)
                if (field == 'name') {

                    return React.DOM.td({key: 'td-' + i}, this.props.resource[field]);
                }
                if (field == 'authors') {

                    if ('authors' in this.props.resource) {
                        return React.DOM.td(
                            {key: 'td-' + i},
                            this.props.resource.authors.map((resource) => {
                                return React.createElement(Link, {key:i, uri: resource.uri}, resource.name)
                            })
                        );
                    }
                }
                if (field == 'duration') {
                    return React.DOM.td({key: 'td-' + i}, '00:00');
                }
                if (field == 'popularity') {
                    // return <td><PopularityBar popularity={this.props.resource.popularity || 0} /></td>
                }
                if (field == 'album') {
                    if ('album' in this.props.resource)
                        return React.DOM.td(
                            {
                                key: 'td-' + i
                            }, 
                            React.createElement(Link, {key: i, uri: this.props.resource.album.uri}, this.props.resource.album.name)
                        )
                }
                if (field == 'list') {
                    if ('list' in this.props.resource)
                        return React.DOM.td(
                            {key: 'td-' + i},
                            React.createElement(Link, {key: i, uri: this.props.resource.list.uri}, this.props.resource.list.name)
                        );
                }
                if (field == 'added_by') {
                    if ('added_by' in this.props.resource)
                        return React.DOM.td(
                            {key: 'td-' + i},
                            React.createElement(Link, {key: i, uri: this.props.resource.added_by.uri}, this.props.resource.added_by.name)
                        );
                }
            }
        );  
    },

    _onMouseDown: function (event) {
        if ('onMouseDown' in this.props)
            this.props.onMouseDown(this);
        if ('onSelectResource' in this.props)
            this.props.onSelectResource(this.props.resource, this.props.index);
    },

    _onDoubleClick: function(event) {
        if ('onActivateResource' in this.props)
            this.props.onActivateResource(this);
    },

    render: function() {
        var active = this.state !== null ? this.state.active : false;
        var selected = this.state !== null ? this.state.selected : false;
        return (
            React.DOM.tr(
                {
                    className: 'sp-resource ' + (active ? 'sp-resource-active' : '') + (selected ? 'sp-track-selected' : ''),
                    onDoubleClick: this._onDoubleClick,
                    onMouesDown: this._onMouseDown
                },
                this.renderFields()
            )
        )
    }
});

module.exports = Resource;