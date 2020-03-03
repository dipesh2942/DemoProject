package com.globalservice.contactPicker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;

public class ContactPicker extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final int CONTACT_PICKER_REQUEST_CODE = 467081;
    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
    private static final String E_PICKER_CANCELLED = "E_PICKER_CANCELLED";
    private static final String E_FAILED_TO_SHOW_PICKER = "E_FAILED_TO_SHOW_PICKER";
    private static final String E_FETCH_DETAIL_FAILED = "E_FAILED_FETCH_CONTACT_DETAIL";

    private Promise mPickerPromise;

    public ContactPicker(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ContactPicker";
    }

    @ReactMethod
    public void selectContact(Promise promise) {
        Activity currentActivity = getCurrentActivity();

        if (currentActivity == null) {
            promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
            return;
        }

        // Store the promise to resolve/reject when picker returns data
        mPickerPromise = promise;

        try {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            getCurrentActivity().startActivityForResult(intent, CONTACT_PICKER_REQUEST_CODE);
        } catch (Exception e) {
            mPickerPromise.reject(E_FAILED_TO_SHOW_PICKER, e);
            mPickerPromise = null;
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

        if (requestCode == CONTACT_PICKER_REQUEST_CODE) {

            if (mPickerPromise != null) {

                if (resultCode == Activity.RESULT_CANCELED) {

                    mPickerPromise.reject(E_PICKER_CANCELLED, "Contact picker was cancelled");

                } else if (resultCode == Activity.RESULT_OK) {

                    Uri uri = data.getData();
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

                    if (getCurrentActivity() != null) {
                        Cursor cursor = getCurrentActivity().getContentResolver().query(uri, projection,
                                null, null, null);
                        if (cursor != null) {
                            cursor.moveToFirst();
                            int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            String number = cursor.getString(numberColumnIndex);

                            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            String name = cursor.getString(nameColumnIndex);

                            cursor.close();
                            WritableNativeMap contactDetails = new WritableNativeMap();

                            contactDetails.putString("name", name);
                            contactDetails.putString("number", number);

                            mPickerPromise.resolve(contactDetails);
                        } else {
                            mPickerPromise.reject(E_FETCH_DETAIL_FAILED, "Fetching contact details failed");
                        }
                    } else {
                        mPickerPromise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
                    }
                } else {
                    mPickerPromise.reject(E_FETCH_DETAIL_FAILED, "Fetching contact details failed");
                }
                mPickerPromise = null;
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
