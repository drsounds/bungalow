// From http://stackoverflow.com/questions/22538638/how-to-have-conditional-elements-and-keep-dry-with-facebook-reacts-jsx
export default class If extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        if (this.props.test) {
            return this.props.children;
        }
        else {
            return false;
        }
    }
}