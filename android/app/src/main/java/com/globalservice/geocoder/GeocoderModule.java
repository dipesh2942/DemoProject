package com.globalservice.geocoder;

import android.location.Address;
import android.location.Geocoder;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by JBSPL on 20-Jul-18.
 */
public class GeocoderModule extends ReactContextBaseJavaModule {

    public GeocoderModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "Geocoder";
    }

    @ReactMethod
    public void getAddress(String lat, String lng, Promise promise){
        String addressResult;

        //If lat and long is not empty then get address and set in editText
        if (!lat.isEmpty() || !lng.isEmpty()) {

            DecimalFormat decimalFormat = new DecimalFormat("00.0000000");
            double latitude = Double.parseDouble(decimalFormat.format(Double.parseDouble(lat)));
            double longitude = Double.parseDouble(decimalFormat.format(Double.parseDouble(lng)));

            try {
                Geocoder geocoder = new Geocoder(getCurrentActivity(), Locale.getDefault());

                //List<Address> addresses = geocoder.getFromLocation(21.7657742, 72.1387822, 1);
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && addresses.size() > 0) {

                    Address address = addresses.get(0);

                    StringBuilder builder = new StringBuilder();
                    //For addressLine
                    if (address.getMaxAddressLineIndex() != 0) {
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            builder.append(address.getAddressLine(i)).append("\n");
                        }
                    }

                    //For Sublocality
                    builder.append(address.getSubLocality()).append(",\n");

                    //Locality and Postal Code
                    builder.append(address.getLocality()).append("-");
                    builder.append(address.getPostalCode()).append(",\n");

                    //State and Country
                    builder.append(address.getAdminArea()).append(", ");
                    builder.append(address.getCountryName());

                    addressResult = builder.toString();
                    //printLog("ADDRESS", addressResult);
                } else {
                    addressResult = "No Address found";
                }
            } catch (IOException e) {
                e.printStackTrace();
                addressResult = "Address Fetching Error";
            }
        } else {
            addressResult = "Invalid Latitude Longitude";
        }
        promise.resolve(addressResult);
    }
}
