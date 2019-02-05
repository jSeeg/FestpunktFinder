package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Hilfsklasse zum erstellen der SQLlitedatenbank
 * Quelle: http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class MapDBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = MapDBHelper.class.getSimpleName();
    public static final String DB_NAME = "mapDB.db";
    public static final int DB_VERSION = 1;

    //Tabellendaten fuer die Map-Tabelle:
    public static final String TABLE_MAP_DB = "MapDB";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_PUNKT_NUMMER = "punktNummer";
    public static final String COLUMN_PROJEKT_NUMMER = "projektNummer";
    public static final String COLUMN_RECHTSWERT = "rechts";
    public static final String COLUMN_HOCHWERT = "hoch";
    public static final String COLUMN_HOEHE =  "hoehe";
    public static final String COLUMN_LATITUDE =  "latitude";
    public static final String COLUMN_LONGITUDE =  "longitude";
    public static final String COLUMN_PUNKT_ART =  "punktArt";
    public static final String COLUMN_ANMERKUNG =  "anmerkung";
    public static final String SQL_CREATE_TABLE_MAP =
            "CREATE TABLE " + TABLE_MAP_DB +
                    "("+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PUNKT_NUMMER + " TEXT NOT NULL, " +
                    COLUMN_RECHTSWERT + " REAL NOT NULL, " +
                    COLUMN_HOCHWERT + " REAL NOT NULL, " +
                    COLUMN_HOEHE + " REAL, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_PROJEKT_NUMMER + " TEXT, " +
                    COLUMN_PUNKT_ART + " TEXT, " +
                    COLUMN_ANMERKUNG + " TEXT);";

    //Tabellendaten fuer die Bilder-Tabelle:
    public static final String TABLE_BILDER = "BilderDB";
    //ID-String von oben wieder verwenden
    public static final String COLUMN_BILD = "bild";
    public static final String COLUMN_PUNKT_ID = "punktID";
    public static final String SQL_CREATE_TABLE_BILDER =
            "CREATE TABLE " + TABLE_BILDER +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_BILD + " BLOB NOT NULL, " +
                    COLUMN_PUNKT_ID + " INTEGER NOT NULL);";

    public MapDBHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "MapDBHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    /*
    Hier werden die Tabellen angelegt.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        try
        {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE_TABLE_MAP + " angelegt.");
            db.execSQL(SQL_CREATE_TABLE_MAP);
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE_TABLE_BILDER + " angelegt.");
            db.execSQL(SQL_CREATE_TABLE_BILDER);
        }
        catch (Exception exc)
        {
            Log.e(LOG_TAG, "Error: Fehler beim Anlegen der Tabelle: "+exc.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
