package nl.senseos.mytimeatsense.util;


public class Constants {

	public static class Status {

		public static final String PREFS_STATUS = "prefs_status";

        public static final String STATUS_DETECTED = "status_detected";

        public static final String STATUS_DEVICE_KEY = "status_device_key";

        public static final String STATUS_TIMESTAMP = "status_timestamp";

        public static final int STATE_NOT_DETECTED = 0;

		public static final int STATE_DESK_1 = 1;

        public static final int STATE_DESK_2 = 2;

        public static final int STATE_DESK_3 = 4;

        public static final int STATE_DESK_4 = 4;

        public static final int STATE_BIKE = 5;

		public static final String STATUS_TIME_OFFICE = "time_office";

		public static final String STATUS_TIME_BIKE = "time_bike";

        public static final String STATUS_LIGHT_LEVEL = "light_level";

        public static final String STATUS_LIGHT_FOCUS = "light_focus";

	}

	
	/**
	 * Set of predefined names for the data types that CommonSense supports. Useful for creating new
	 * sensor data value Intents.
	 * 
	 * @author Steven Mulder <steven@sense-os.nl>
	 */
	public static class SenseDataTypes {

	    public static final String BOOL = "bool";
	    public static final String FLOAT = "float";    
	    public static final String ARRAY = "array";    
	    public static final String INT = "int";
	    public static final String JSON = "json";
	    public static final String STRING = "string";
	    public static final String FILE = "file";    
	    public static final String JSON_TIME_SERIES = "json time serie";

	    private SenseDataTypes() {
	        // private constructor to prevent instantiation
	    }
	}

	public static class Url{

		/** Host name of CommonSense API */
	    public static final String API_URL = "https://api.sense-os.nl/";
        public static final String DEV_URL = "http://ibeaconapi.dev.sense-os.nl/";
        public static final String SENSORS_URL = API_URL+"sensors";
	    public static final String SENSOR_DEVICE_URL = API_URL + "sensors/%1/device";
	    public static final String LOGIN_URL = API_URL+"login";
	    public static final String LOGOUT_URL = API_URL+"logout";
	    public static final String CURRENT_USER_URL = API_URL+"users/current";

    }
	
	public static class Sensors {

		public static final String PREFS_SENSORS = "prefs_sensors";

		public static final String SENSOR_LIST_COMPLETE="sensor_list_complete";

		public static final String SENSOR_LIST_COMPLETE_TIME="sensor_list_complete_time";		
		/** Default page size for getting lists at CommonSense */
	    public static final int PAGE_SIZE = 1000;
	    	    
		public static final String SENSOR_NAME="name";		

		public static final String SENSOR_DISPLAY_NAME="display_name";		

		public static final String SENSOR_ID="id";		
				
		public static final String BEACON_SENSOR_NAME="beacon_sensor";
		
		public static final String BEACON_SENSOR_DISPLAY_NAME="beacon_sensor";
		
		public static final String BEACON_SENSOR_DESCRIPTION="detect beacon at the sense os office";

		public static final String BEACON_SENSOR = "beacon_sensor_json";
	    
	}

	public static class Auth {
		
		public static final String PREFS_CREDS = "prefs_creds";
		
		public static final String PREFS_CREDS_UNAME = "username";
		
		public static final String PREFS_CREDS_PASSWORD = "password";
		/**
		 * Key for login preference for session cookie.
		 * 
		 */
		public static final String LOGIN_COOKIE = "login_cookie";
		/**
		 * Key for login preference for session id.
		 */	
		public static final String LOGIN_SESSION_ID = "session_id";
		/**
         * Key for storing the online device type.
         */
        public static final String DEVICE_TYPE = "device_type";
        
        public static final String USER_ID = "user_id";
        
	}

    public static class Labels{

        public static final String PREFS_LABELS = "prefs_labels";

        public static final String LABEL_OFFICE = "label_office";

        public static final String LABEL_NAME = "name";

        public static final String LABEL_NAME_OFFICE = "Sense Office";

        public static final String LABEL_LIST_COMPLETE = "label_list_complete";

        public static final String LABEL_LIST_COMPLETE_TIME = "label_list_complete_time";
    }
}
