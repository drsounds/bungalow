var ui = require('@bungalow/ui');
var React = require('React');
var ResourceContext = ui.ResourceContext;
class UI extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        var resources = [
            {
                uri: 'bungalow:resource:2FORU',
                authors:[
                    {
                        uri: 'bungalow:author:test',
                        name: 'Test',
                    }
                ],
                duration: 60 * 3,
                album: {
                    name: 'Test Album',
                    uri: 'bungalow:album:1251215'
                },
                added_by: {
                    id: 'drsounds',
                    uri: 'bungalow:user:drsounds',
                    name: 'Dr. Sounds'
                }
            }
        ];

        var fields = ['name', 'authors', 'album', 'duration'];
        return (
            <ResourceContext resources={resources} fields={fields} />
        )
    }
}

React.render(<UI />, document.querySelector('#ui'));