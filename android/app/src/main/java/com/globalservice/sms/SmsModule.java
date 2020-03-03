package com.globalservice.sms;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;


public class SmsModule extends ReactContextBaseJavaModule {

    public SmsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        SmsReceiver.bind(reactContext);
    }

    @Override
    public String getName() {
        return "SmsListener";
    }
}
