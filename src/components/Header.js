const React = require('react');
const {Redirect} = require('react-router');
const {MusicStore} = require('../stores/MusicStore');


export class HeaderView extends React.Component {
    constructor(props) {
        super(props);
        
    }
    componentDidMount() {
        
    }
    render() {
        return (
            <table style={{width: '100%'}}>
                <tr>
                    <td style={{width: '96pt'}}>
                        <img src={this.props.object.images ? this.props.object.images[0].url : ''} style={{width: '96pt'}} className="sp-cover-image" />
                    </td>
                    <td style={{verticalAlign: 'top'}}>
                        <h3><Link to={this.props.object.href}>{this.props.object.name}</Link></h3>
                    </td>
                </tr>
            </table>
        )
    }
}