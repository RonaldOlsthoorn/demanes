package nl.senseos.mytimeatsense.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import nl.senseos.mytimeatsense.bluetooth.iBeacon;

/*
 * DBHelper is the applications connection to the database.
 * All the queries are stored as functions of this class.
 * These functions are called by all the classes of the application
 * that need information from the database, or need to update/insert/delete.
 * 
 * Also, all the information about the databases' layout are stored in the inner classes
 * each inner class represents a table.
 * 
 * To secure thread safety, singleton pattern is used. Only ONE instance of this class exists.
 */
public class DBHelper extends SQLiteOpenHelper implements BaseColumns {

    private static final String MARK_FOR_DELETION="DELETED";

	public static final String TAG = DBHelper.class.getSimpleName();
	public static int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Demanes.db";
	private static DBHelper singleton;

	// Returns the DBHelper object. Singleton pattern is used.
	public static DBHelper getDBHelper(Context context) {
		if (singleton == null) {
			singleton = new DBHelper(context);
		}
		return singleton;
	}

	// Constructor for new DBHelper object.
	private DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public Cursor getCompleteLog() {

		SQLiteDatabase db;
		db = getReadableDatabase();
		return db.query(DetectionTable.TABLE_NAME, null, null, null, null, null,
				null);
	}

    public Cursor getAllBeacons(){

        SQLiteDatabase db;
        db = getReadableDatabase();
        return db.query(BeaconTable.TABLE_NAME, null,
                BeaconTable.COLUMN_UUID+" NOT LIKE ?",
                new String[]{MARK_FOR_DELETION}, null, null, null);
    }

    public Cursor getUnsavedBeacons(){

        SQLiteDatabase db;
        db = getReadableDatabase();
        return db.query(BeaconTable.TABLE_NAME, null,
                BeaconTable.TABLE_NAME+'.'+BeaconTable.COLUMN_REMOTE_ID+"=-1", null, null, null,
                null);
    }

    public void markOrDelete(iBeacon beacon){

        if(beacon.getRemoteId()==-1){
            deleteOrIgnore(BeaconTable.TABLE_NAME, beacon.getLocalId());
        }else{

            ContentValues v = new ContentValues();
            v.put(BeaconTable.COLUMN_UUID,MARK_FOR_DELETION);
            v.put(BeaconTable.COLUMN_MAJOR,0);
            v.put(BeaconTable.COLUMN_MINOR,0);

            updateOrIgnore(BeaconTable.TABLE_NAME,beacon.getLocalId(),v);
        }
        getMatchingBeacon(beacon);
    }

    public Cursor getDeletedBeacons(){

        SQLiteDatabase db= getReadableDatabase();
        return db.query(BeaconTable.TABLE_NAME, null, BeaconTable.COLUMN_UUID+"="+MARK_FOR_DELETION,
                null,null,null,null);
    }

    public iBeacon getMatchingBeacon(iBeacon beacon){

        SQLiteDatabase db= getReadableDatabase();
        Cursor c = db.query(BeaconTable.TABLE_NAME, null,
                        BeaconTable.COLUMN_UUID+"=? AND "
                        +BeaconTable.COLUMN_MAJOR+"=? AND "
                        +BeaconTable.COLUMN_MINOR+"=?",
                new String[]{beacon.getUUID(),
                        Integer.toString(beacon.getMajor()),
                        Integer.toString(beacon.getMinor())},
                null, null, null);

        if(c.getCount()==0){
            return null;
        }
        c.moveToFirst();
        beacon.setLocalId(c.getLong(0));
        return beacon;
    }

	public long insertOrIgnore(String table, ContentValues values) {

		long res = -1;
		Log.d(TAG, "insertOrIgnore on " + values);
		SQLiteDatabase db = getWritableDatabase();
		try {
			res = db.insertOrThrow(table, null, values);
		} catch (SQLException e) {
			Log.d(TAG, "insertOrIgnore on " + values + " fail");
		}
		//db.close();
		return res;
	}

	public boolean updateOrIgnore(String table, long id, ContentValues values) {

		boolean res = false;
		Log.d(TAG, "updateOrIgnore on " + table + " values " + values + " "
				+ id);
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.update(table, values, _ID + "=?", new String[] { id + "" });
			res = true;

		} catch (SQLException e) {
			Log.d(TAG, "updateOrIgnore on " + table + " values " + values + " "
					+ id + " fail");
			res = false;
		}
		//db.close();
		return res;
	}

	public int deleteOrIgnore(String table, long id) {

		int res = -1;
		Log.d(TAG, "deleteOrIgnore on " + id);
		SQLiteDatabase db = getWritableDatabase();
		try {
			res = db.delete(table, _ID + "=?", new String[]{id + ""});
		} catch (SQLException e) {
			Log.d(TAG, "deleteOrIgnore on " + id + " fail");
		}
		db.close();
		return res;
	}
	
	public int deleteAllRows(String table){
		
		int res = -1;
		Log.d(TAG, "deleteAllRows on " + table);
		SQLiteDatabase db = getWritableDatabase();
		try {
			res = db.delete(table,null, null);
		} catch (SQLException e) {
			Log.d(TAG, "deleteAllRows on " + table + " fail");
		}
		db.close();
		return res;
	}

	/*
	 * Called when the database is first created. Creates all the tables and
	 * triggers.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DetectionTable.SQL_CREATE_TABLE);
        db.execSQL(BeaconTable.SQL_CREATE_TABLE);
    }

	/*
	 * Called when the database is updated. Simply removes all the tables and
	 * recreates them
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DetectionTable.SQL_DELETE_ENTRIES);
        db.execSQL(BeaconTable.SQL_DELETE_ENTRIES);
		onCreate(db);
		DATABASE_VERSION = newVersion;
	}

	/*
	 * Inner class representing the table containing all the detections
	 */
	public static abstract class DetectionTable implements BaseColumns {

		public static final String TABLE_NAME = "detections";
		public static final String COLUMN_ID = _ID;
		public static final String COLUMN_TIMESTAMP = "timestamp";
		public static final String COLUMN_DETECTION_RESULT = "detection_result";
		public static final String COLUMN_BEACON = "beacon_id";

		public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME
				+ " ("
				+ COLUMN_ID
				+ " INTEGER PRIMARY KEY ,"
				+ COLUMN_TIMESTAMP
				+ " LONG NOT NULL ,"
				+ COLUMN_DETECTION_RESULT 
				+ " BOOLEAN NOT NULL , "
				+ COLUMN_BEACON
				+ " INT "
				 + " )";

		public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
				+ TABLE_NAME;

		private static String getIdColumnName() {
			return COLUMN_ID;
		}
	}

    /*
  * Inner class representing the table containing all the known beacons
  */
    public static abstract class BeaconTable implements BaseColumns {

        public static final String TABLE_NAME = "beacons";
        public static final String COLUMN_ID = _ID;
        public static final String COLUMN_MAC = "mac_adress";
        public static final String COLUMN_NAME = "device_name";
        public static final String COLUMN_UUID = "uuid";
        public static final String COLUMN_MAJOR = "major";
        public static final String COLUMN_MINOR = "minor";
        public static final String COLUMN_TX = "tx";
        public static final String COLUMN_REMOTE_ID = "remote_id";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + " ("
                + COLUMN_ID
                + " INTEGER PRIMARY KEY ,"
                + COLUMN_MAC
                + " TEXT ,"
                + COLUMN_NAME
                + " TEXT , "
                + COLUMN_UUID
                + " VARCHAR(32) ,"
                + COLUMN_MAJOR
                + " INT , "
                + COLUMN_MINOR
                + " INT , "
                + COLUMN_TX
                + " INT , "
                + COLUMN_REMOTE_ID
                + " INT ,"
                + "UNIQUE( "
                + COLUMN_MAC+","+COLUMN_UUID+","+COLUMN_MAJOR+","+COLUMN_MINOR
                + " ) "
                + " ) ";

        public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
                + TABLE_NAME;

        private static String getIdColumnName() {
            return COLUMN_ID;
        }
    }
}
