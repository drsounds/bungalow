const React = require('react');
const {Redirect} = require('react-router-dom');
const {MusicStore} = require('../stores/MusicStore');
const {Link} = require('react-router-dom');
const {uriToPath} = require('../utils');
const numeral = require('numeral');
numeral.locale('sv');
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
                            <h2 style={{minHeight: '25pt'}}><Link to={uriToPath(this.props.object.uri)}>{this.props.object.name}</Link></h2>
                            <div>
                             <button className="btn btn-default">+ Follow</button>
                            </div>
                            <p dangerouslySetInnerHTML={{__html: this.props.object.description}}></p>
                        </td>
                        <td style={{padding: '3pt', verticalAlign: 'top'}}>
                        {this.props.object.followers && false &&
                            <div style={{textAlign: 'right'}}>
                                <h3>Followers</h3>
                                <small>{numeral(this.props.object.followers.total).format('0,0')}</small>
                            </div>
                        }
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