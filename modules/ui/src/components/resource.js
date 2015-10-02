var React = require('react');
var PopularityBar = require('./popularity_bar.js');
export default class Resource extends React.Component {
    constructor(props) {
        super(props);
        this.renderFields = this.renderFields.bind(this);
        this._onDoubleClick = this._onDoubleClick.bind(this);

    }

    renderFields() {
        return this.props.fields.map((field) => {
            if (field == 'name') {
                return <td>{this.props.resource[field]}</td>;
            }
            if (field == 'authors') {
                if ('authors' in this.props.resource)
                    return this.props.resource.authors.map((resource) => {
                        return <Link uri={this.props.resource.author.uri}>{this.props.author.name}</Link>;
                });
            }
            if (field == 'duration') {
                return <td>0:00</td>;
            }
            if (field == 'popularity') {
                return <td><PopularityBar popularity={this.props.resource.popularity || 0} /></td>
            }
            if (field == 'album') {
                if ('album' in this.props.resource)
                    return <Link uri={this.props.resource.album.uri}>{this.props.resource.album.name}</Link>;
            }
            if (field == 'collection') {
                if ('collection' in this.props.resource)
                    return <Link uri={this.props.resource.list.uri}>{this.props.resource.list.name}</Link>;
            }
            if (field == 'addedBy') {
                if ('added_by' in this.props.resource)
                    return <Link uri={this.props.resource.added_by.uri}>{this.props.resource.added_by.name}</Link>;
            }
        });
    }

    _onDoubleClick(event) {
        this.props.onActivateResource(this);
    }

    render() {
        return (
            <tr className={'sp-resource ' + (this.props.active ? 'sp-resource-active' : '')} onDoubleClick={this._onDoubleClick}>
                {this.renderFields()}
            </tr>
        )
    }
}