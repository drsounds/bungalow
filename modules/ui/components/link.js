var React = require('react');
var Link = React.createClass({

    _onClick: function(event) {
        var uri = event.target.dataUri;
        // TODO Add navigation
        // 
        if (uri.startsWith('http:')) {
            window.open(uri);
            return;
        }
    },

    render: function () {
        return React.DOM.a({ 'data-uri': this.props.uri, href: 'javacript:void(event)', onClick: this._onClick}, this.props.children);
    }
});

Link.PropTypes = {
    uri: React.PropTypes.string.isRequired
};

module.exports = Link;