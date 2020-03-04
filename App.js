import React, { Component } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import codePush from "react-native-code-push";

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  onButtonPress = () => {
    codePush.sync({
      updateDialog: true,
      installMode: codePush.InstallMode.IMMEDIATE
    }).catch((e) => console.warn(e.message));
  }

  render() {
    return (
      <View>
        <TouchableOpacity onPress={this.onButtonPress}>
          <Text>Check for updates</Text>
        </TouchableOpacity>

        <Text> This is Updated Text After Taking Update AutoMatically </Text>
        <Text> This is Updated  </Text>
      </View>
    )
  }
}

let codePushOptions = { checkFrequency: codePush.CheckFrequency.MANUAL };
App = codePush(codePushOptions)(App);

export default App;
