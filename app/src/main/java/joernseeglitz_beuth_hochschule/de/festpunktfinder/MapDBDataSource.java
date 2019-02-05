package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Klasse als Data Access Object
 * Verwaltung vom Datenmanagement
 * Quelle: http://www.programmierenlernenhq.de/sqlite-datenbank-in-android-app-integrieren/
 */
public class MapDBDataSource {
    private static final String LOG_TAG = MapDBDataSource.class.getSimpleName();

    public SQLiteDatabase getDatenbank() {
        return datenbank;
    }

    private SQLiteDatabase datenbank;
    private MapDBHelper dbHelper;

    private Context activityContext;

    //String-Array der GeodaetischerPunkt-Spalten
    private String [] columns_mapDB =
            {
                    MapDBHelper.COLUMN_ID,
                    MapDBHelper.COLUMN_PUNKT_NUMMER,
                    MapDBHelper.COLUMN_RECHTSWERT,
                    MapDBHelper.COLUMN_HOCHWERT,
                    MapDBHelper.COLUMN_HOEHE,
                    MapDBHelper.COLUMN_LONGITUDE,
                    MapDBHelper.COLUMN_LATITUDE,
                    MapDBHelper.COLUMN_PROJEKT_NUMMER,
                    MapDBHelper.COLUMN_PUNKT_ART,
                    MapDBHelper.COLUMN_ANMERKUNG
            };

    public String[] getColumns_bilderDB() {
        return columns_bilderDB;
    }

    //String-Array der BilderDB-Spalten
    private String [] columns_bilderDB =
            {
                    MapDBHelper.COLUMN_ID,
                    MapDBHelper.COLUMN_BILD,
                    MapDBHelper.COLUMN_PUNKT_ID
            };

    //Konstruktor der MapDBDataSource-Klasse
    //bekommt den Context uebergeben
    //erstellt ein MapDBHelper-Objekt
    public MapDBDataSource(Context context)
    {
        Log.d(LOG_TAG, "MapDBDataSource erzeugt den dbHelper.");
        dbHelper = new MapDBHelper(context);

        this.activityContext = context;
    }

    //Oeffnet eine Referenz zu der Datenbank
    //mit Schreibrechten geoeffnet
    public void open()
    {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird angefragt.");
        datenbank = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG,"Datenbank-Referenz erhalten. Pfad zur Datenbank: " +
                datenbank.getPath());
    }

    //Schließt die Datenbankverbindung
    public void close()
    {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank geschlossen.");
    }

    /**
     * Methode um einen vollstaendigen GeodaetischerPunkt Punkt in die TABLE_MAP_DB einzufuegen
     * Hierbei sind sowohl UTM als auch WGS Koordinaten durch Transformation gesetzt
     * @return GeodaetischerPunkt
     */
    public GeodaetischerPunkt insertPunktInMapDB(String punktNummer, double rechtswert, double hochWert,
                                                 double hoehe, double longitude, double latitude,
                                                 String projektNummer, String punktArt, String anmerkung)
    {
        //Paramenter, die in die Insertfunktion uebergeben werden
        ContentValues values = new ContentValues();
        values.put(MapDBHelper.COLUMN_PUNKT_NUMMER, punktNummer);
        values.put(MapDBHelper.COLUMN_RECHTSWERT, rechtswert);
        values.put(MapDBHelper.COLUMN_HOCHWERT, hochWert);
        values.put(MapDBHelper.COLUMN_HOEHE,hoehe);
        values.put(MapDBHelper.COLUMN_LONGITUDE,longitude);
        values.put(MapDBHelper.COLUMN_LATITUDE,latitude);
        if (Objects.equals(punktArt,""))
        {
            punktArt = null;
        }
        values.put(MapDBHelper.COLUMN_PUNKT_ART,punktArt);
        if (Objects.equals(projektNummer,""))
        {
            projektNummer = null;
        }
        values.put(MapDBHelper.COLUMN_PROJEKT_NUMMER, projektNummer);
        if (Objects.equals(anmerkung,""))
        {
            anmerkung = null;
        }
        values.put(MapDBHelper.COLUMN_ANMERKUNG,anmerkung);

        //Insertfunktion
        //liefert die Datenbankposition (ID / Cursor) beim einfuegen in die Datenbank
        long insetID = datenbank.insert(MapDBHelper.TABLE_MAP_DB,null,values);
        Cursor cursor = datenbank.query(MapDBHelper.TABLE_MAP_DB,
                columns_mapDB,MapDBHelper.COLUMN_ID + "=" + insetID,
                null,null,null,null,null);
        cursor.moveToFirst();
        GeodaetischerPunkt geodaetischerPunkt = cursorToMapDB(cursor);
        cursor.close();

        return geodaetischerPunkt;
    }

    //Methode um die Insert-Anweisung auszufuehren fuer die Tabelle TABLE_MAP_DB
    public GeodaetischerPunkt insertRechtsHochPunktInMapDB(String punktNummer, double rechtswert, double hochWert,
                                                           double hoehe, String projektNummer, String punktArt, String anmerkung)
    {
        //Paramenter, die in die Insertfunktion uebergeben werden
        ContentValues values = new ContentValues();
        values.put(MapDBHelper.COLUMN_PUNKT_NUMMER, punktNummer);
        values.put(MapDBHelper.COLUMN_RECHTSWERT, rechtswert);
        values.put(MapDBHelper.COLUMN_HOCHWERT, hochWert);
        values.put(MapDBHelper.COLUMN_HOEHE,hoehe);
        if (Objects.equals(punktArt,""))
        {
            punktArt = null;
        }
        values.put(MapDBHelper.COLUMN_PUNKT_ART,punktArt);
        if (Objects.equals(projektNummer,""))
        {
            projektNummer = null;
        }
        values.put(MapDBHelper.COLUMN_PROJEKT_NUMMER, projektNummer);
        if (Objects.equals(anmerkung,""))
        {
            anmerkung = null;
        }
        values.put(MapDBHelper.COLUMN_ANMERKUNG,anmerkung);

        //Insertfunktion
        //liefert die Datenbankposition (ID / Cursor) beim einfuegen in die Datenbank
        long insetID = datenbank.insert(MapDBHelper.TABLE_MAP_DB,null,values);
        Cursor cursor = datenbank.query(MapDBHelper.TABLE_MAP_DB,
                columns_mapDB,MapDBHelper.COLUMN_ID + "=" + insetID,
                null,null,null,null,null);
        cursor.moveToFirst();
        GeodaetischerPunkt geodaetischerPunkt = cursorToMapDB(cursor);
        cursor.close();

        return geodaetischerPunkt;
    }

    //Methode um die Insert-Anweisung auszufuehren fuer die Tabelle TABLE_MAP_DB
    public GeodaetischerPunkt insertLatLonPunktInMapDB(String punktNummer, double longitude, double latitude,
                                                       double hoehe, String projektNummer, String punktArt, String anmerkung)
    {
        //Paramenter, die in die Insertfunktion uebergeben werden
        ContentValues values = new ContentValues();
        values.put(MapDBHelper.COLUMN_PUNKT_NUMMER, punktNummer);
        values.put(MapDBHelper.COLUMN_RECHTSWERT, 0.0);
        values.put(MapDBHelper.COLUMN_HOCHWERT, 0.0);
        values.put(MapDBHelper.COLUMN_LONGITUDE, longitude);
        values.put(MapDBHelper.COLUMN_LATITUDE, latitude);
        values.put(MapDBHelper.COLUMN_HOEHE,hoehe);
        if (Objects.equals(punktArt,""))
        {
            punktArt = null;
        }
        values.put(MapDBHelper.COLUMN_PUNKT_ART,punktArt);
        if (Objects.equals(projektNummer,""))
        {
            projektNummer = null;
        }
        values.put(MapDBHelper.COLUMN_PROJEKT_NUMMER, projektNummer);
        if (Objects.equals(anmerkung,""))
        {
            anmerkung = null;
        }
        values.put(MapDBHelper.COLUMN_ANMERKUNG,anmerkung);

        //Insertfunktion
        //liefert die Datenbankposition (ID / Cursor) beim einfuegen in die Datenbank
        long insetID = datenbank.insert(MapDBHelper.TABLE_MAP_DB,null,values);
        Cursor cursor = datenbank.query(MapDBHelper.TABLE_MAP_DB,
                columns_mapDB,MapDBHelper.COLUMN_ID + "=" + insetID,
                null,null,null,null,null);
        cursor.moveToFirst();
        GeodaetischerPunkt geodaetischerPunkt = cursorToMapDB(cursor);
        cursor.close();

        return geodaetischerPunkt;
    }


    /**
     * Methode um die Insert-Anweisung auszufuehren fuer die Tabelle TABLE_BILDER
     * @param punktNum Punktnummer
     * @param proj Projektnummer
     * @param bilder Liste der Bilder
     * @return gibt ein GeodaetischerPunkt-Objekt mit Bildern zurueck
     */
    public GeodaetischerPunkt insertInBilderDB(String punktNum, String proj, List<Bitmap> bilder)
    {
        ContentValues values = new ContentValues();

        //Paramenter, die in die Insertfunktion uebergeben werden
        //PunktID fuer das Bild bekommen
        Cursor cursorPunktID;
        if (proj.equals("")) {
            cursorPunktID = datenbank.rawQuery("SELECT ID FROM MapDB WHERE punktNummer =? AND " +
                            "(projektNummer IS NULL OR projektNummer = '')",
                    new String[]{punktNum});
        }
        else
        {
            cursorPunktID = datenbank.rawQuery(
                    "SELECT ID FROM MapDB WHERE punktNummer =? AND projektNummer =?",
                    new String[]{punktNum, proj});
        }
        cursorPunktID.moveToFirst();
        int pIDIndex = cursorPunktID.getColumnIndex(MapDBHelper.COLUMN_ID);
        int pID = cursorPunktID.getInt(pIDIndex);

        String sqlInsert = "INSERT INTO ?(?,?) VALUES (?,?);";



        //Liste durchlaufen
        for (Bitmap bild:bilder
                ) {
            //Bilder vor dem Einladen in die DB komprimieren
//            double bild_size_alt, bild_size_neu;
//            bild_size_alt = bild.getByteCount();
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            bild.compress(Bitmap.CompressFormat.JPEG,50,out);
//            byte[] bild_byte = out.toByteArray();
//            Bitmap bild_compressed = BitmapFactory.decodeByteArray(bild_byte,0,bild_byte.length);
//            bild_size_neu = bild_compressed.getByteCount();
//            Log.v("Bild_Komprimierung", String.valueOf(bild_size_neu) + " von " +
//                    String.valueOf(bild_size_alt) + " [Byte]");

            //Bild in Byte-Array umwandeln
//            byte[] data = getBitmapAsByteArray(bild_compressed);
            byte[] data = getBitmapAsByteArray(bild);

            ContentValues cv = new ContentValues();
            cv.put(MapDBHelper.COLUMN_BILD,data);
            cv.put(MapDBHelper.COLUMN_PUNKT_ID,pID);

            //In die Datenbank einfuegen:
            datenbank.insertWithOnConflict(MapDBHelper.TABLE_BILDER,null,cv,
                    SQLiteDatabase.CONFLICT_REPLACE);

//            datenbank.execSQL(sqlInsert,new Object[]
//                    {MapDBHelper.TABLE_BILDER,MapDBHelper.COLUMN_BILD,MapDBHelper.COLUMN_PUNKT_ID
//                            ,data,pID});
        }
        //Holt alle Eintraege mit der bestimmten Punkt-ID
        Cursor cursor = datenbank.query(
                MapDBHelper.TABLE_BILDER,columns_bilderDB,MapDBHelper.COLUMN_PUNKT_ID +"=?",
                new String []{String.valueOf(pID)},null,null,null);
        cursor.moveToFirst();
        GeodaetischerPunkt bilderDB;
        List<Bitmap> sqlBilder = new ArrayList<Bitmap>();
        //Liste der Bilder aus der Datenbank zusammentragen
        while (!cursor.isAfterLast())
        {
            try {
                int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);
                byte[] bild = cursor.getBlob(idBild);
                sqlBilder.add(BitmapFactory.decodeByteArray(bild, 0, bild.length));
            }
            catch(Exception ex)
            {
                Log.e("DATASourceImageImport",ex.getMessage());
            }
            cursor.moveToNext();
        }
        cursor.close();
        bilderDB = new GeodaetischerPunkt(punktNum,proj,sqlBilder);

        return bilderDB;
    }

    //Quelle: http://stackoverflow.com/questions/9357668/how-to-store-image-in-sqlite-database
    //rechnet ein Bitmap in ein byte-Array um (um dieses in der Datenbank abzulegen)
    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    //Liefert die Werte aus der MAP_DB Datenbank an der Cursor-Position
    //Gibt ein gefuelltes GeodaetischerPunkt-Objekt zurueck
    private GeodaetischerPunkt cursorToMapDB(Cursor cursor)
    {
        int idIndex = cursor.getColumnIndex(MapDBHelper.COLUMN_ID);
        int idPunktNummer = cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_NUMMER);
        int idRechts = cursor.getColumnIndex(MapDBHelper.COLUMN_RECHTSWERT);
        int idHoch = cursor.getColumnIndex(MapDBHelper.COLUMN_HOCHWERT);
        int idHoehe = cursor.getColumnIndex(MapDBHelper.COLUMN_HOEHE);
        int idLongitude = cursor.getColumnIndex(MapDBHelper.COLUMN_LONGITUDE);
        int idLatitude = cursor.getColumnIndex(MapDBHelper.COLUMN_LATITUDE);
        int idProjektNummer = cursor.getColumnIndex(MapDBHelper.COLUMN_PROJEKT_NUMMER);
        int idPunktArt = cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_ART);
        int idAnmerkung = cursor.getColumnIndex(MapDBHelper.COLUMN_ANMERKUNG);

        long id = cursor.getLong(idIndex);
        String punktNum = cursor.getString(idPunktNummer);
        double re = cursor.getDouble(idRechts);
        double ho = cursor.getDouble(idHoch);
        double hoe = cursor.getDouble(idHoehe);
        double longitude = cursor.getDouble(idLongitude);
        double latitude = cursor.getDouble(idLatitude);
        String proj = cursor.getString(idProjektNummer);
        String art = cursor.getString(idPunktArt);
        String anmerkung = cursor.getString(idAnmerkung);

        GeodaetischerPunkt geodaetischerPunkt = new GeodaetischerPunkt(punktNum,re,ho,hoe,
                longitude,latitude,proj,art,anmerkung);

        return geodaetischerPunkt;
    }

    //Liefert die Werte aus der MAP_DB Datenbank an der Cursor-Position
    //Gibt ein gefuelltes GeodaetischerPunkt-Objekt zurueck
    private GeodaetischerPunkt cursorLatLonToMapDB(Cursor cursor)
    {
        int idIndex = cursor.getColumnIndex(MapDBHelper.COLUMN_ID);
        int idPunktNummer = cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_NUMMER);
        int idLongitude = cursor.getColumnIndex(MapDBHelper.COLUMN_LONGITUDE);
        int idLatitude = cursor.getColumnIndex(MapDBHelper.COLUMN_LATITUDE);
        int idHoehe = cursor.getColumnIndex(MapDBHelper.COLUMN_HOEHE);
        int idProjektNummer = cursor.getColumnIndex(MapDBHelper.COLUMN_PROJEKT_NUMMER);
        int idPunktArt = cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_ART);
        int idAnmerkung = cursor.getColumnIndex(MapDBHelper.COLUMN_ANMERKUNG);

        long id = cursor.getLong(idIndex);
        String punktNum = cursor.getString(idPunktNummer);
        double lon = cursor.getDouble(idLongitude);
        double lat = cursor.getDouble(idLatitude);
        double hoe = cursor.getDouble(idHoehe);
        String proj = cursor.getString(idProjektNummer);
        String art = cursor.getString(idPunktArt);
        String anmerkung = cursor.getString(idAnmerkung);

        GeodaetischerPunkt geodaetischerPunkt =
                new GeodaetischerPunkt(punktNum,lon,lat,hoe,proj,art,1,anmerkung);

        return geodaetischerPunkt;
    }

    //Liefert die Werte aus der BILDER Datenbank an der Cursor-Position
    //Gibt ein gefuelltes GeodaetischerPunkt-Objekt zurueck
    private GeodaetischerPunkt cursorToBilderDB(Cursor cursor)
    {
        int idIndex = cursor.getColumnIndex(MapDBHelper.COLUMN_ID);
        int idPunktNummer = cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_NUMMER);
        int idProjektNummer = cursor.getColumnIndex(MapDBHelper.COLUMN_PROJEKT_NUMMER);
        int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);

        long id = cursor.getLong(idIndex);
        String punktNum = cursor.getString(idPunktNummer);
        String proj = cursor.getString(idProjektNummer);
        byte [] bild = cursor.getBlob(idBild);
        List<Bitmap> bildListe = new ArrayList<Bitmap>();
        bildListe.add(BitmapFactory.decodeByteArray(bild,0,bild.length));

        GeodaetischerPunkt geodaetischerPunkt = new GeodaetischerPunkt(punktNum,proj,bildListe);

        return geodaetischerPunkt;
    }

    //Listet alle Elemente aus der MAP_DB Datenbank auf
    //Gibt eine Liste von GeodaetischerPunkt-Objekten zurueck
    public List<GeodaetischerPunkt> getAllMapDB()
    {
        List<GeodaetischerPunkt> geodaetischerPunktListe = new ArrayList<>();

        Cursor cursor = datenbank.query(
                MapDBHelper.TABLE_MAP_DB, columns_mapDB,null,null,null,null,
                null,null);
        cursor.moveToFirst();
        GeodaetischerPunkt geodaetischerPunkt;

        while (!cursor.isAfterLast())
        {
            geodaetischerPunkt = cursorToMapDB(cursor);
            geodaetischerPunktListe.add(geodaetischerPunkt);
            Log.d(LOG_TAG, "Punktnummer: " + geodaetischerPunkt.getPunktNummer() +
                    "; Rechtswert: " + geodaetischerPunkt.getRechts() +
                    "; Hochwert: " + geodaetischerPunkt.getHoch() +
                    "; Hoehe: " + geodaetischerPunkt.getHoehe() +
                    "; Latitude: " + geodaetischerPunkt.getLatitude() +
                    "; Longitude: " + geodaetischerPunkt.getLongitude() +
                    "; Projekt: " + geodaetischerPunkt.getProjektNummer() +
                    "; Punktart: " + geodaetischerPunkt.getPunktArt() +
                    "; Anmerkung: " + geodaetischerPunkt.getAnmerkung());
            cursor.moveToNext();
        }
        cursor.close();

        return geodaetischerPunktListe;
    }

    //Listet alle Elemente aus der MAP_DB Datenbank eines bestimmten Projekts auf
    //Gibt eine Liste von GeodaetischerPunkt-Objekten zurueck
    public List<GeodaetischerPunkt> getAllMapDBforProject(String projektNummer)
    {
        List<GeodaetischerPunkt> geodaetischerPunktListe = new ArrayList<>();
        Cursor cursor;
        if(!projektNummer.equals("") && projektNummer != null) {
            cursor = datenbank.query(
                    MapDBHelper.TABLE_MAP_DB, columns_mapDB,
                    MapDBHelper.COLUMN_PROJEKT_NUMMER + " LIKE ?", new String[]{projektNummer}, null, null,
                    null, null);
        }
        else
        {
            cursor = datenbank.query(
                    MapDBHelper.TABLE_MAP_DB, columns_mapDB,
                    MapDBHelper.COLUMN_PROJEKT_NUMMER + " IS NULL", null, null, null,
                    null, null);
        }
        cursor.moveToFirst();
        GeodaetischerPunkt geodaetischerPunkt;

        while (!cursor.isAfterLast())
        {
            geodaetischerPunkt = cursorToMapDB(cursor);
            geodaetischerPunktListe.add(geodaetischerPunkt);
            Log.d(LOG_TAG, "Punktnummer: " + geodaetischerPunkt.getPunktNummer() +
                    "; Rechtswert: " + geodaetischerPunkt.getRechts() +
                    "; Hochwert: " + geodaetischerPunkt.getHoch() +
                    "; Hoehe: " + geodaetischerPunkt.getHoehe() +
                    "; Latitude: " + geodaetischerPunkt.getLatitude() +
                    "; Longitude: " + geodaetischerPunkt.getLongitude() +
                    "; Projekt: " + geodaetischerPunkt.getProjektNummer() +
                    "; Punktart: " + geodaetischerPunkt.getPunktArt() +
                    "; Anmerkung: " + geodaetischerPunkt.getAnmerkung());
            cursor.moveToNext();
        }
        cursor.close();

        return geodaetischerPunktListe;
    }

    //Listet alle Elemente aus der MAP_DB Datenbank eines bestimmten Punktes auf
    //Gibt eine Liste von GeodaetischerPunkt-Objekten zurueck
    public List<GeodaetischerPunkt> getAllMapDBforPunkt(String projektNummer, String punktNummer)
    {
        List<GeodaetischerPunkt> geodaetischerPunktListe = new ArrayList<>();
        Cursor cursor;

        if(projektNummer != null && !projektNummer.equals("")) {
            cursor = datenbank.query(
                    MapDBHelper.TABLE_MAP_DB, columns_mapDB,
                    MapDBHelper.COLUMN_PROJEKT_NUMMER + " LIKE ?"
                            +" AND "+MapDBHelper.COLUMN_PUNKT_NUMMER+" LIKE ?",
                    new String[]{projektNummer,punktNummer}, null, null,
                    null, null);
        }
        else
        {
            cursor = datenbank.query(
                    MapDBHelper.TABLE_MAP_DB, columns_mapDB,
                    MapDBHelper.COLUMN_PROJEKT_NUMMER + " IS NULL"
                            +" AND "+MapDBHelper.COLUMN_PUNKT_NUMMER+" LIKE ?",
                    new String[]{punktNummer}, null, null,
                    null, null);
        }
        cursor.moveToFirst();
        GeodaetischerPunkt geodaetischerPunkt;

        while (!cursor.isAfterLast())
        {
            geodaetischerPunkt = cursorToMapDB(cursor);
            geodaetischerPunktListe.add(geodaetischerPunkt);
            Log.d(LOG_TAG, "Punktnummer: " + geodaetischerPunkt.getPunktNummer() +
                    "; Rechtswert: " + geodaetischerPunkt.getRechts() +
                    "; Hochwert: " + geodaetischerPunkt.getHoch() +
                    "; Hoehe: " + geodaetischerPunkt.getHoehe() +
                    "; Latitude: " + geodaetischerPunkt.getLatitude() +
                    "; Longitude: " + geodaetischerPunkt.getLongitude() +
                    "; Projekt: " + geodaetischerPunkt.getProjektNummer() +
                    "; Punktart: " + geodaetischerPunkt.getPunktArt() +
                    "; Anmerkung: " + geodaetischerPunkt.getAnmerkung());
            cursor.moveToNext();
        }
        cursor.close();

        return geodaetischerPunktListe;
    }

    //Listet alle Elemente aus der MAP_DB Datenbank auf
    //Gibt eine Liste von GeodaetischerPunkt-Objekten zurueck
    public List<GeodaetischerPunkt> getAllBilderDB()
    {
        List<GeodaetischerPunkt> bilderDBListe = new ArrayList<>();

        Cursor cursor = datenbank.query(
                MapDBHelper.TABLE_BILDER, columns_bilderDB,null,null,null,null,
                null,null);
        cursor.moveToFirst();
        GeodaetischerPunkt bilderDB;
        long lastID = 0;
        boolean first = true;
        List<Bitmap> sqlBilder = new ArrayList<Bitmap>();

        while (!cursor.isAfterLast())
        {
            int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);
            byte [] bild = cursor.getBlob(idBild);
            int idPunktID = cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_ID);
            long pID = cursor.getLong(idPunktID);
            if (first == true)
            {
                lastID = pID;
            }
            if(lastID == pID)
            {
                sqlBilder.add(BitmapFactory.decodeByteArray(bild,0,bild.length));
                if(cursor.isLast())
                {
                    bilderDB = new GeodaetischerPunkt(sqlBilder,pID);
                    bilderDBListe.add(bilderDB);
                }
            }
            else
            {
                bilderDB = new GeodaetischerPunkt(sqlBilder,pID);
                bilderDBListe.add(bilderDB);
                sqlBilder = new ArrayList<Bitmap>();
                sqlBilder.add(BitmapFactory.decodeByteArray(bild,0,bild.length));
            }

            cursor.moveToNext();
            first = false;
        }
        cursor.close();

        return bilderDBListe;
    }

    //Listet alle Elemente aus der MAP_DB Datenbank auf
    //Gibt eine Liste von GeodaetischerPunkt-Objekten zurueck
    public List<GeodaetischerPunkt> getAllBilderDBforPunkt(String projektNummer, String punktNummer)
    {
        List<GeodaetischerPunkt> bilderDBListe = new ArrayList<>();

        Cursor cursor;

        //ID- der gewuenschten Punkte bekommen
        Cursor cursorPunktID;
        if (projektNummer != null && !projektNummer.equals("")) {
            cursorPunktID = datenbank.rawQuery(
                    "SELECT ID FROM MapDB WHERE punktNummer =? AND projektNummer =?",
                    new String[]{punktNummer, projektNummer});
        }
        else
        {
            cursorPunktID = datenbank.rawQuery("SELECT ID FROM MapDB WHERE punktNummer =? AND " +
                            "(projektNummer IS NULL OR projektNummer = '')",
                    new String[]{punktNummer});
        }
        cursorPunktID.moveToFirst();
        int pIDIndex = cursorPunktID.getColumnIndex(MapDBHelper.COLUMN_ID);
        int punktID = cursorPunktID.getInt(pIDIndex);

        //Eintraege aus der Bilder-Datenbank bekommen
        cursor = datenbank.rawQuery("SELECT * FROM BilderDB WHERE punktID =?",
                new String[]{String.valueOf(punktID)});

        cursor.moveToFirst();
        GeodaetischerPunkt bilderDB;
        long lastID = 0;
        boolean first = true;
        List<Bitmap> sqlBilder = new ArrayList<Bitmap>();

        while (!cursor.isAfterLast())
        {
            int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);
            byte [] bild = cursor.getBlob(idBild);
            int idPunktID = cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_ID);
            long pID = cursor.getLong(idPunktID);
            if (first == true)
            {
                lastID = pID;
            }
            if(lastID == pID)
            {
                sqlBilder.add(BitmapFactory.decodeByteArray(bild,0,bild.length));
                if(cursor.isLast())
                {
                    bilderDB = new GeodaetischerPunkt(sqlBilder,pID);
                    bilderDBListe.add(bilderDB);
                }
            }
            else
            {
                bilderDB = new GeodaetischerPunkt(sqlBilder,pID);
                bilderDBListe.add(bilderDB);
                sqlBilder = new ArrayList<Bitmap>();
                sqlBilder.add(BitmapFactory.decodeByteArray(bild,0,bild.length));
            }

            cursor.moveToNext();
            first = false;
        }
        cursor.close();

        return bilderDBListe;
    }

    /**
     * Gibt die Anzahl an geodaetischen Punkten in der MapDB Datenbank aus.
     * @return Integerwert der Anzahl
     */
    public int getPunktAnzahl()
    {
        int punktAnzahl = 0;

        Cursor cursor;

        cursor = datenbank.rawQuery("SELECT COUNT(*) FROM MapDB;",null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            punktAnzahl = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return punktAnzahl;
    }

    /**
     * Gibt die Anzahl an Bilder in der BilderDB Datenbank aus.
     * @return Integerwert der Anzahl
     */
    public int getBilderAnzahl()
    {
        int bilderAnzahl = 0;

        Cursor cursor;

        cursor = datenbank.rawQuery("SELECT COUNT(*) FROM BilderDB;",null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            bilderAnzahl = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return bilderAnzahl;
    }

    /**
     * Gibt die Anzahl an einzelnen Projekten in der MapDB Datenbank aus.
     * @return Integerwert der Anzahl
     */
    public int getProjekteAnzahl()
    {
        int projekteAnzahl = 0;

        Cursor cursor;

        cursor = datenbank.rawQuery("SELECT COUNT(DISTINCT projektNummer) FROM MapDB;",null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            projekteAnzahl = cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();

        return projekteAnzahl;
    }

    /**
     * Methode um die Datenbankgroesse zu berechnen.
     * Gibt die Groeße in Megabytes zurueck
     * @return double DB-Groesse
     */
    public double getDatenbankGroesse()
    {
        File f = activityContext.getDatabasePath("mapDB.db");
        double dbSize = f.length();
        //Byte in MB umrechnen
        dbSize = dbSize/1048576;

        return dbSize;
    }

    //Quelle: http://stackoverflow.com/questions/15124179/resizing-a-bitmap-to-a-fixed-value-but-without-changing-the-aspect-ratio
    public Bitmap scaleBitmap(Bitmap bm) {
        Konstanten konstanten = new Konstanten(activityContext);
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.v("Pictures", "Width and height are " + width + "--" + height);

        if (width > height) {
            // landscape
            float ratio = (float) width / konstanten.getBilderMaxWidth();
            width = konstanten.getBilderMaxWidth();
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / konstanten.getBilderMaxHeight();
            height = konstanten.getBilderMaxHeight();
            width = (int)(width / ratio);
        } else {
            // square
            height = konstanten.getBilderMaxHeight();
            width = konstanten.getBilderMaxWidth();
        }

        Log.v("Pictures", "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

}
