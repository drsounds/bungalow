const React = require('react');


export class Separator extends React.Component {
    constructor(props) {
        super(props);
        
    }
    componentDidMount() {
        
    }
    render() {
        return (
            <div className="divider">{this.props.children}</div>
        )
    }
}