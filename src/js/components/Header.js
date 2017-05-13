const React = require('react');
const {Redirect} = require('react-router-dom');
const {MusicStore} = require('../stores/MusicStore');
const {Link} = require('react-router-dom');
const {uriToPath} = require('../utils');


export class Header extends React.Component {
    constructor(props) {
        super(props);
        
    }
    componentDidMount() {
        
    }
    render() {
        return (
            <table className="sp-header" style={{width: '100%'}}>
                <tbody>
                    <tr>
                        <td style={{width: '96pt', padding: '3pt'}} rowSpan="2">
                            <Link to={uriToPath(this.props.object.uri)}><img src={this.props.object.images ? this.props.object.images[0].url : ''} style={{width: '96pt'}} className="sp-cover-image" /></Link>
                        </td>
                        <td style={{height: '100%', padding: '3pt', verticalAlign: 'top'}}>
                            <small>{this.props.object.type}</small>
                            <h2 style={{minHeight: '25pt'}}><Link to={uriToPath(this.props.object.uri)}>{this.props.object.name}</Link></h2>
                            <div className="sp-toolbar">
                                <button className="btn btn-default">+ Follow</button>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            
                        </td>
                    </tr>
                </tbody>
            </table>
        )
    }
}