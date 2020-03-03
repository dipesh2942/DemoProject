import React, { Component } from 'react';
import {
    View,
    Text,
    YellowBox,
    StyleSheet,
    PixelRatio,
    AsyncStorage,
    TouchableNativeFeedback,
    ScrollView,
    DeviceEventEmitter,
    ToastAndroid,
    Image,
    ProgressBarAndroid
} from 'react-native';

import Geocoder from '../packages/Geocoder';
import ContactPicker from '../packages/ContactPicker';
import FilePickerManager from 'react-native-file-picker';
import ImageDownloader from '../packages/ImageDownloader';

YellowBox.ignoreWarnings(['Warning: isMounted(...) is deprecated', 'Module RCTImageLoader']);

export default class MainScreen extends Component {

    static navigationOptions = {
        title: "Native Functionality"
    }

    constructor(props) {
        super(props)

        this.state = {
            message: null,
            latitude: null,
            longitude: null,
            error: null,
            address: null,
            countMessage: null,
            uri: {
                data: 'http://meeconline.com/wp-content/uploads/2014/08/placeholder.png',
                currentProgress: 0,
                progress: 0
            },
        };
    };

    componentDidMount = () => {

        //To start download service in background
        ImageDownloader.startService();

        //To check if anything exists in Storage
        this.checkforAvailableStorage();

        //To listen incoming messages
        this.smsListener = DeviceEventEmitter.addListener('onSmsReceive', message => {
            this.setState({ message: message });
        })

        //To listen counter
        this.countListener = DeviceEventEmitter.addListener('onCountChange', count => {
            this.setState({ countMessage: count });
        })

        //To listen Image Downloader
        this.downloadListener = DeviceEventEmitter.addListener('onDownloadComplete', uri => {
            let loading = (uri.progress != 1)
            this.setState({ uri: uri, loading: loading });
        })

        //For Getting Geo Location
        navigator.geolocation.getCurrentPosition(
            (position) => {

                this.setState({
                    latitude: position.coords.latitude.toFixed(7),
                    longitude: position.coords.longitude.toFixed(7),
                    error: null
                });

                this.getAddress();
            },
            (error) => this.setState({ error: error.message }),
            { enableHighAccuracy: true, timeout: 20000, maximumAge: 1000 }
        );
    }

    componentWillUnmount = () => {
        //To remove listener
        this.smsListener != null && this.smsListener.remove();
        this.countListener != null && this.countListener.remove();
        this.downloadListener != null && this.downloadListener.remove();
    };

    checkforAvailableStorage = () => {
        //To check if there is any message stored in background task
        AsyncStorage.getItem('smsData').then(data => {
            if (data) {
                this.setState({ message: JSON.parse(data) })
                AsyncStorage.removeItem('smsData');
            }
        })

        //To check if there is any counter stored in background task
        AsyncStorage.getItem('counter').then(data => {
            if (data) {
                this.setState({ countMessage: JSON.parse(data) })
                AsyncStorage.removeItem('counter');
            }
        })

        //To check if there is any image stored in background task
        AsyncStorage.getItem('uri').then(data => {
            if (data) {
                this.setState({ uri: JSON.parse(data) })
                AsyncStorage.removeItem('uri');
            }
        })
    }

    getAddress = async () => {
        await Geocoder.getAddress(this.state.latitude, this.state.longitude)
            .then(address => {
                if (address != null) {
                    this.setState({
                        address: address,
                        error: null
                    });
                }
            })
            .catch(error => {
                console.error(error)
                this.setState({ address: "Cant Get Address" })
            });;
    }

    openFilePicker = () => {
        FilePickerManager.showFilePicker(null, (response) => {
            console.log('Response = ', response);

            if (response.didCancel) {
                ToastAndroid.show('User cancelled file picker', ToastAndroid.SHORT)
            }
            else if (response.error) {
                //console.warn('FilePickerManager Error: ', response.error);
                ToastAndroid.show(response.error.message, ToastAndroid.SHORT)
            }
            else {
                this.setState({
                    file: response
                });
            }
        });
    }

    openContactPicker = async () => {
        await ContactPicker.selectContact()
            .then(result => {
                this.setState({ contact: result })
            })
            .catch(error => {
                ToastAndroid.show(error.message, ToastAndroid.show)
                this.setState({ contact: null })
            });
    }

    startCount = () => {
        ImageDownloader.startCount();
    }

    stopCount = () => {
        ImageDownloader.stopCount();
    }

    getImage = () => {
        var url = 'https://i.imgur.com/4G4yYex.jpg';
        //url = 'https://www.imaging-resource.com/?ACT=44&fid=17&d=5548&f=dubai-gigapixel-sky-out.jpg';
        //url = 'https://cdn.pixabay.com/photo/2017/01/21/13/59/ice-1997289_960_720.jpg'

        ImageDownloader.getImage(url);
        this.setState({ loading: true })
    }

    render() {

        let { message } = this.state;
        let { countMessage } = this.state;
        let { uri } = this.state;
        let isDisableCounter = false;

        if (countMessage != null) {
            if (countMessage.counter.indexOf('Finished') >= 0 || countMessage.counter.indexOf('Cancelled') >= 0) {
                isDisableCounter = false;
            } else {
                isDisableCounter = true;
            }
        }

        return (
            <ScrollView>
                <View style={styles.container}>
                    {/* Background Service DEMO */}
                    <TitleCard title='Background Service'>
                        <TextView name='Count' value={this.state.countMessage != null && this.state.countMessage.counter} />

                        <View style={{ flexDirection: 'row', flex: 1, marginTop: 10 }}>
                            <CustomButton name="Start" onPress={this.startCount} disabled={isDisableCounter} />
                            <CustomButton name="Stop" onPress={this.stopCount} disabled={!isDisableCounter} />
                        </View>
                    </TitleCard>

                    {/* Image Downlaod Service DEMO */}
                    <TitleCard title='Image Downloader'>
                        {
                            this.state.loading
                                ?
                                <View style={{ flexDirection: 'column', flex: 1, marginTop: 10 }}>
                                    <ProgressBarAndroid
                                        styleAttr="Horizontal"
                                        indeterminate={!(this.state.loading)}
                                        style={{ marginTop: 10 }}
                                        progress={uri.progress != null && uri.progress}
                                    />
                                    <Text style={{ marginTop: 10, flex: 1, alignSelf: 'flex-end' }}> {uri.currentProgress}/100</Text>
                                </View>
                                :
                                <Image
                                    source={{ uri: uri.data }}
                                    style={{ width: 200, height: 150, justifyContent: 'center', alignSelf: 'center' }}
                                    resizeMethod='resize'
                                />
                        }
                        <View style={{ flexDirection: 'row', flex: 1, marginTop: 10 }}>
                            <CustomButton name="Download" onPress={this.getImage} />
                        </View>
                    </TitleCard>

                    {/* SMS Background Service DEMO */}
                    <TitleCard title='SMS Service'>
                        <TextView name='Sender' value={message != null && message.originatingAddress} />
                        <TextView name='Body' value={message != null && message.body} />
                    </TitleCard>

                    {/* User Current Location */}
                    <TitleCard title='User Current Location'>
                        <TextView name='Latitude' value={this.state.latitude} />
                        <TextView name='Longitude' value={this.state.longitude} />
                        <TextView name='Address' value={this.state.address != null && this.state.address} />
                    </TitleCard>

                    {/* File Picker */}
                    <TitleCard title='File Picker'>
                        <CustomButton name='Open File Picker' onPress={this.openFilePicker} style={{ marginTop: 5, marginBottom: 5 }} />
                        <TextView name='File Name' value={this.state.file != null && this.state.file.fileName} style={{ alignItems: 'flex-start' }} />
                        <TextView name='File Type' value={this.state.file != null && this.state.file.type} style={{ alignItems: 'flex-start' }} />
                        <TextView name='File Path' value={this.state.file != null && this.state.file.path} style={{ alignItems: 'flex-start' }} />
                        <TextView name='File Uri' value={this.state.file != null && this.state.file.uri} style={{ alignItems: 'flex-start' }} />
                    </TitleCard>

                    {/* Contact Picker */}
                    <TitleCard title='Contact Picker'>
                        <CustomButton name='Contact Picker' onPress={this.openContactPicker} style={{ marginTop: 5, marginBottom: 5 }} />
                        <TextView name='Name' value={this.state.contact != null && this.state.contact.name} style={{ alignItems: 'flex-start' }} />
                        <TextView name='Number' value={this.state.contact != null && this.state.contact.number} style={{ alignItems: 'flex-start' }} />
                    </TitleCard>
                </View>
            </ScrollView>
        );
    }
}

const TextView = (props) => (
    <View style={[{ flexDirection: 'row', alignItems: 'center' }, props.style]}>
        <Text style={[{ flex: 1 }, styles.content]}>{props.name} :</Text>
        <Text style={[{ flex: 2 }, styles.content]}>{props.value}</Text>
    </View>
)

const CustomButton = (props) => (
    <TouchableNativeFeedback background={TouchableNativeFeedback.Ripple('#484848', false)} onPress={props.onPress} disabled={props.disabled}>
        <View style={[styles.button, props.style]}>
            <Text style={{ color: '#000', textAlign: 'center' }}>{props.name}</Text>
        </View>
    </TouchableNativeFeedback>
)

const TitleCard = (props) => (
    <View>
        <View style={styles.card}>
            <Text style={styles.title}>{props.title}</Text>
            {props.children}
        </View>
    </View>
)

const normalizeFontSize = (size) => {
    let deviceSize = PixelRatio.get() | 0;

    if (deviceSize === 2) {
        return size * 1.15;
    }

    if (deviceSize === 3) {
        return size * 1.35;
    }

    return size * deviceSize;
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        paddingBottom: 15
    },
    card: {
        padding: 15,
        elevation: 2,
        borderRadius: 3,
        backgroundColor: 'white',
        marginLeft: 10,
        marginRight: 10,
        marginTop: 10
    },
    title: {
        fontSize: normalizeFontSize(16),
        fontWeight: 'bold',
        paddingBottom: 5
    },
    content: {
        fontSize: normalizeFontSize(15),
        fontWeight: 'normal',
    },
    button: {
        backgroundColor: '#c5c5c5',
        padding: 10,
        marginLeft: 2,
        marginRight: 2,
        flex: 1
    },
});