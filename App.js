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
    });
  }

  render() {
    return (
      <View>
        <TouchableOpacity onPress={this.onButtonPress}>
          <Text>Check for updates</Text>
        </TouchableOpacity>

        
      </View>
    )
  }
}

let codePushOptions = { checkFrequency: codePush.CheckFrequency.MANUAL };
App = codePush(codePushOptions)(App);

export default App;
