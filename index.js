import {
    AppRegistry,
    AsyncStorage
} from 'react-native';
import { createStackNavigator } from 'react-navigation';
import ResideDemo from './screens/Demo';

const MainScreen = require('./screens/MainScreen').default
const Auth0Sample = require('./app/index').default

const Navigation = createStackNavigator(
    {
        Main: { screen: MainScreen },
        Auth0Sample: { screen: Auth0Sample }
    },
    {
        initialRouteName: 'Auth0Sample',
    })

export default Navigation;

const BackgroundService = async (data) => {

    //To check if data coming from SMS Receiver
    if (data['body'] != null) {
        const smsDataJson = {
            originatingAddress: data['originatingAddress'],
            body: data['body']
        }
        AsyncStorage.setItem('smsData', JSON.stringify(smsDataJson));
    }

    //To check if data coming from Count Receiver
    if (data['counter'] != null) {
        AsyncStorage.setItem('counter', JSON.stringify(data));
    }

    //To check if data coming from Image Download Receiver
    if (data['progress'] != null) {
        AsyncStorage.setItem('uri', JSON.stringify(data));
    }
}

AppRegistry.registerHeadlessTask('BackgroundService', () => BackgroundService)
AppRegistry.registerComponent('GlobalService', () => MainScreen);
