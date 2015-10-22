var React = require('react');
var Link = require('./link.js');
var Image = require('./image.js');
var ImageLink = React.createClass({
    render: function() {
        return (
            React.createElement(Link,
                {
                    uri: this.props.uri
                },
                React.createElement(Image,
                    {
                        src: this.props.uri || this.props.src
                    }
                )
            )
        );
    }
}

ImageLink.PropTypes = {
    uri: React.PropTypes.string.isRequired,
    src: React.PropTypes.string.optional
};

module.exports = ImageLink;