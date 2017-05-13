const React = require('react');
const {Redirect} = require('react-router-dom');
const {MusicStore} = require('../stores/MusicStore');
const {uriToPath} = require('../utils');
const {Link} = require('react-router-dom');
const Loading = require('react-loader');


export class Album extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            object: this.props.object
        }
    }
    componentDidMount() {
        let uri = this.props.uri;
        MusicStore.fetchObjectsFromCollection(this.props.uri);
        MusicStore.addChangeListener(() => {
            let uri = this.props.uri;
            this.setState({
                loaded: true,
                objects: MusicStore.state.resources[uri].objects
            });
        });
    }
    render() {
        return (
            <Loader loading={this.state.object != null}>
                {this.state.object &&
                <table className="sp-table" style={{width: '100%'}}>
                   
                    <tbody>
                        <tr>
                            <td style={{verticalAlign: 'top', width: '64pt'}}>
                                <Link to={uriToPath(this.state.object.uri)}><img src={this.state.object.images[0].src} /></Link>
                            </td>
                            <td style={{verticalAlign: 'top'}}>
                                <h3><Link to={uriToPath(this.state.object.uri)}>{this.state.object.name}</Link></h3>
                                <PlayContext uri={this.state.object.uri + ':track'} />
                            </td>
                        </tr>
                    </tbody>
                </table>}
            </Loader>
        )
    }
}