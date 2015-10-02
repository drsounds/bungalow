var React = require('react');
export default class Link extends React.Component {

    constructor(props) {
        super(props);
        this._onClick = this._onClick.bind(this);
    }

    _onClick(event) {
        var uri = event.target.dataUri;
        // TODO Add navigation
        // 
        if (uri.startsWith('http:')) {
            window.open(uri);
            return;
        }
    }

    render() {
        return <a dataUri={this.props.uri} href="javacript:void(event)" onClick={this._onClick}>{this.props.children}</a>;
    }
}

Link.PropTypes = {
    uri: React.PropTypes.string.isRequired
};

