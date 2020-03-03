import React, { Component } from 'react';
import { View } from 'react-native';
import  ResideMenu  from  'react-native-reside-menu';

export default class ResideDemo extends Component {
    constructor(props) {
        super(props);
        this.state = {
        };
    }

    render() {
        return (
            <ResideMenu
                onResideStateChange={(s) => { console.log(s) }}
                VisibleComponent={() => <View style={{ flex: 1, backgroundColor: '#eee' }} />}
                HiddenComponent={() => <View style={{ flex: 1, backgroundColor: '#fff' }} />}
                direction={'left'}
            />
        );
    }
}
