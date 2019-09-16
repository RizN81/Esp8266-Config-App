package com.espressif.iot.esptouch.util;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CHANGE_NETWORK_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.INTERNET;

/**
 * Created by niyaz sir on 8/8/2017.
 */

public class Constants {
	public static final String APP_PREFERENCE        = "com.emergency.alert";
	public static final String PREF_USER_FIRST_TIME  = "user_first_time";
	public static final String IS_ICON_CREATED       = "isIconCreated";
	public static final String PREF_IS_LOGIN         = "isUserLogin";
	public static final String PREF_IS_PARENT_APP    = "isParentApp";
	public static final String PREF_IS_REGISTERED    = "isRegistered";
	public static final String PREF_IS_SERVICE_START = "isServiceStart";
	public static final String PERMISSION_STATUS     = "permissionStatus";
	public static final String PREF_IS_SETTING_SAVED = "isSettingSaved";
	public static final String PREF_IS_PROFILE_SAVED = "isProfileSetup";
	public static final String PREF_INTERVAL         = "interval";
	public static final String PREF_SERVER_IP        = "serverIP";
	public static final String PREF_SERVER_PORT      = "serverPort";
	public static final String PREF_USER_EMAIL       = "userEmail";
	public static final String PREF_USER_PASSWORD    = "userPassword";
	public static final String PREF_USER_AUTO_ID     = "userAutoID";
	public static final String PREF_PARENT_AUTO_ID   = "parentAutoID";
	public static final String PREF_MSG_SOS1         = "sos1";
	public static final String PREF_MSG_SOS2         = "sos2";
	public static final String PREF_RECIPIENT_NO     = "recipientNo";
	public static final String PREF_USER_ADDRESS     = "userAddress";
	public static final String PREF_USER_MOBILE      = "mobile";
	public static final String SERVICE_REQUEST       = "service";
	public static final String URL_PREFIX            = "http://";
	public static final String FULLNAME              = "fullName";
	public static final String USERNAME              = "username";
	public static final String AUTOID                = "autoID";
	public static final String EMAIL                 = "email";
	public static final String PASSWORD              = "password";
	public static final String LOCATION              = "location";
	
	//Common Keys
	public static final String ID_KEY                        = "id";
	public static final String NAME_KEY                      = "name";
	public static final String EMAIL_ID_KEY                  = "emailid";
	public static final String PHONE_NO_KEY                  = "phoneno";
	public static final String ADDRESS_KEY                   = "address";
	public static final String DATE_KEY                      = "date";
	public static final String PARENT_ID_KEY                 = "parentID";
	public static final String USER_ID_KEY                   = "userID";
	public static final String SOS1_KEY                      = "SOS1";
	public static final String SOS2_KEY                      = "SOS2";
	public static final int    ACCESS_FINE_LOCATION_CONSTANT = 100;
	public static final int    REQUEST_PERMISSION_SETTING    = 101;
	
	public static final String LATITUED_KEY  = "latitude";
	public static final String LONGITUDE_KEY = "longitude";
	
	public static final String JSON_RESPONSE_RESULT  = "result";
	public static final String JSON_RESPONSE_MESSAGE = "message";
	public static final String JSON_RESPONSE_DATA    = "data";

	public static final String[] PERMISSIONS ={  ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, CHANGE_NETWORK_STATE, CHANGE_WIFI_STATE, INTERNET,INTERNET, ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION};
}

