package nl.senseos.mytimeatsense.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

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

    public Cursor getBeacon(int id){

        SQLiteDatabase db;
        db = getReadableDatabase();
        return db.query(BeaconTable.TABLE_NAME, null,
                BeaconTable.COLUMN_NAME_ID +" = ?",
                new String[]{Integer.toString(id)}, null, null, null);
    }


    public Cursor getAllBeacons(){

        SQLiteDatabase db;
        db = getReadableDatabase();
        return db.query(BeaconTable.TABLE_NAME, null, null, null, null, null, null);
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
	 *
	 */
	public static abstract class DetectionTable implements BaseColumns {

		public static final String TABLE_NAME = "detections";
		public static final String COLUMN_ID = _ID;
		public static final String COLUMN_TIMESTAMP = "timestamp";
		public static final String COLUMN_DETECTION_RESULT = "detection_result";
		public static final String COLUMN_DEVICE = "chair_id";
        public static final String COLUMN_DEVICE_OCCUPIED = "occupied";
        public static final String COLUMN_DESK = "desk_id";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME
				+ " ("
				+ COLUMN_ID
				+ " INTEGER PRIMARY KEY ,"
				+ COLUMN_TIMESTAMP
				+ " LONG NOT NULL ,"
				+ COLUMN_DETECTION_RESULT 
				+ " BOOLEAN NOT NULL , "
				+ COLUMN_DEVICE
				+ " INT ,"
                + COLUMN_DEVICE_OCCUPIED
                + " BOOLEAN ,"
                + COLUMN_DESK
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
     *
    */
    public static abstract class BeaconTable implements BaseColumns {

        public static final String TABLE_NAME = "beacons";
        public static final String COLUMN_NAME_ID = _ID;
        public static final String COLUMN_NAME_NAME = "device_name";
        public static final String COLUMN_NAME_UUID = "uuid";
        public static final String COLUMN_NAME_MAJOR = "major";
        public static final String COLUMN_NAME_MINOR = "minor";
        public static final String COLUMN_NAME_TX = "tx";

        public static final int COLUMN_INDEX_ID = 0;
        public static final int COLUMN_INDEX_NAME = 1;
        public static final int COLUMN_INDEX_UUID = 2;
        public static final int COLUMN_INDEX_MAJOR = 3;
        public static final int COLUMN_INDEX_MINOR = 4;
        public static final int COLUMN_INDEX_TX = 5;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + " ("
                + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY ,"
                + COLUMN_NAME_NAME
                + " TEXT , "
                + COLUMN_NAME_UUID
                + " VARCHAR(32) ,"
                + COLUMN_NAME_MAJOR
                + " INT , "
                + COLUMN_NAME_MINOR
                + " INT , "
                + COLUMN_NAME_TX
                + " INT , "
                + "UNIQUE( "
                + COLUMN_NAME_UUID +","+ COLUMN_NAME_MAJOR +","+ COLUMN_NAME_MINOR
                + " ) "
                + " ) ";

        public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
                + TABLE_NAME;

        private static String getIdColumnName() {
            return COLUMN_NAME_ID;
        }
    }

    public  static abstract class DeskTable implements BaseColumns {

        public static final String TABLE_NAME = "desks";
        public static final String COLUMN_NAME_ID = _ID;
        public static final String COLUMN_NAME_KEY = "desk_key";

        public static final int COLUMN_INDEX_ID = 0;
        public static final int COLUMN_INDEX_KEY = 1;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + " ("
                + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY ,"
                + COLUMN_NAME_KEY
                + " INT "
                + " ) ";

        public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
                + TABLE_NAME;

        private static String getIdColumnName() {
            return COLUMN_NAME_ID;
        }
    }

    public  static abstract class ChairTable implements BaseColumns {

        public static final String TABLE_NAME = "chairs";
        public static final String COLUMN_NAME_ID = _ID;
        public static final String COLUMN_NAME_KEY = "chair_key";

        public static final int COLUMN_INDEX_ID = 0;
        public static final int COLUMN_INDEX_KEY = 1;

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_NAME
                + " ("
                + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY ,"
                + COLUMN_NAME_KEY
                + " INT "
                + " ) ";

        public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
                + TABLE_NAME;

        private static String getIdColumnName() {
            return COLUMN_NAME_ID;
        }
    }
}
