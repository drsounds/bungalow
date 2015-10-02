var React = require('react');
var Resource = require('./resource.js');
export default class ResourceContext extends React.Component {
    constructor(props) {
        super(props);
        this.renderChildren = this.renderChildren.bind(this);
        this.renderColumnHeaders = this.renderColumnHeaders.bind(this);
    }

    renderColumnHeaders() {
        return this.props.columnHeaders.map((column) => {
            return <th><span className={'fa fa-' + column}> {column}</span></th>; // TODO add localization
        });
    }

    renderChildren() {
        console.log(this.props.resources);
        var resources = this.props.resources.map((resource) => {
            var t = <Resource resource={resource} fields={this.props.columnHeaders} />;
            console.log(t);
            return t;
        });
        console.log(resources);
        return resources;
    }

    render() {
        var rows = this.props.resources.map((resource) => {
            var t = <Resource resource={resource} key={resource.uri} fields={this.props.columnHeaders} />;
            console.log(t);
            return t;
        });
        return (
            <table className={'sp-resource-context'}>
                <thead>
                    <tr>
                        {this.renderColumnHeaders()}
                    </tr>
                </thead>
                <tbody>{rows}</tbody>
            </table>
        )
    }
}

ResourceContext.propTypes = {
    resources: React.PropTypes.array.isRequired,
    columnHeaders: React.PropTypes.array.isRequired
};