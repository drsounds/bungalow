var React = require('react');
var Link = require('./link.js');
var Image = require('./image.js');
export default class ImageLink extends React.Component {

    constructor(props) {
        super(props);
    }
    render() {
        return (
            <Link data-uri={this.props.uri}>
                <Image src={this.props.uri || this.props.src} />
            </Link>
        );
    }
}

ImageLink.PropTypes = {
    uri: React.PropTypes.string.isRequired,
    src: React.PropTypes.string.optional
};

