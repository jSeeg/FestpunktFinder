package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Klasse fuer das Bereitstellen verschiedener Konstanten fuer verschiedenste Anwendungen in der App
 */
public class Konstanten {

    /*
    Konstante fuer den Abstand von Standorten um das Bewegen der Kamera beim Standortwechsel
    auf den aktuellen Standort durchzuguehren.
    Wird im MapFragment verwendet.
    */
    private float abstandMapCameraZoom;

    /*
    Konstante fuer das Intervall in der der Standort abgefragt werden soll.
    Wird im MapFragment verwendet.
     */
    private long intervalLocationRequest;

    /*
        Konstante fuer die maximale Sollhoehe eines Bildes.
    */
    private int bilderMaxHeight;

    /*
    Konstante fuer die maximale Sollbreite eines Bildes.
     */
    private int bilderMaxWidth;

    /*
    Konstante fuer die maximale Sollhoehe eines Bilderthumbnails.
    */
    private int bilderThumbnailMaxHeight;

    /*
    Konstante fuer die maximale Sollbreite eines Bilderthumbnails.
     */
    private int bilderThumbnailMaxWidth;

    /*
    Uebergabe des Context fuer z.B. das ausgeben von Nachrichten in der App
    mit Hilfe von Toast.
     */
    private static Context context;

    /*
    Konstante um in verschiedenen Stellen ein leeres Projekt mit einem konstanten String zu versehen
    Auch beim abrufen soll dieser erkannt werden und entsprechend gehandhabt.
     */
    private String leeresProjektString;

    /*
    Editierbare Loginparameter fuer den Zugriff auf eine externe Datenbank.
    Benutzername, Passwort, Datenbankname, Servername, Verbisdungs-URL
     */
    private String externDBUser;

    private String externDBPasswort;

    private String externDBDatenbank;

    private String externDBServer;

    private String externDBUrl;


    /*
    EPSG-Codes fuer die Koordinatentransformation
     */
    private String epsgWGS84;
    private String epsgUTM33;

    /*
    Konstruktor der Konstanten.
    Hier werden die Konstanten initialisiert beim Erstellen des Objektes.
     */
    public Konstanten(Context c)
    {
        context = c;

        SharedPreferences configs = context.getSharedPreferences("ConfigFile", 0);
        //Abstand auf 100 m setzen
//        setAbstandMapCameraZoom(100);
        setAbstandMapCameraZoom(Float.parseFloat(configs.getString("mapZoomEntfernung",null)));
        //Intervall auf 1 Sekunde (1000 ms)
//        setIntervalLocationRequest(1000);
        setIntervalLocationRequest(Long.parseLong(configs.getString("mapPosInterval",null)));

//        this.bilderMaxHeight = getScreenSizeHeight() - 1000;
//        this.bilderMaxWidth = getScreenSizeWidth() - 1000;
        this.bilderMaxHeight = 1200;
        this.bilderMaxWidth = 1200;

        this.bilderThumbnailMaxHeight = 500;
        this.bilderThumbnailMaxWidth = 500;

        this.leeresProjektString = "<kein Projekt>";

        this.epsgWGS84 = "EPSG:4326";
        this.epsgUTM33 = "EPSG:25833";

        //externe Datenbankzugriffsparameter werden beim Appstart aus dem Config File gelesen


    }

    public Konstanten()
    {
        this.epsgWGS84 = "EPSG:4326";
        this.epsgUTM33 = "EPSG:25833";
    }

    private int getScreenSizeWidth()
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    private int getScreenSizeHeight()
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    public String getLeeresProjektString() {
        return leeresProjektString;
    }

    public long getIntervalLocationRequest() {
        return intervalLocationRequest;
    }

    public void setIntervalLocationRequest(long intervalLocationRequest) {
        if (intervalLocationRequest < 500 || intervalLocationRequest > 30000)
        {
            Log.d("Error_set","setIntervallLocation hat einen ungueltigen werd erhalten!");
            Toast.makeText(context,"Ungültiger Wert für das LocationRequest-Intervall gesetzt!",
                    Toast.LENGTH_SHORT).show();

        }
        else {
            this.intervalLocationRequest = intervalLocationRequest;
        }
    }

    public float getAbstandMapCameraZoom() {
        return abstandMapCameraZoom;
    }

    public void setAbstandMapCameraZoom(float abstandMapCameraZoom) {
        this.abstandMapCameraZoom = abstandMapCameraZoom;
    }

    public int getBilderMaxHeight() {
        return bilderMaxHeight;
    }

    public int getBilderMaxWidth() {
        return bilderMaxWidth;
    }

    public int getBilderThumbnailMaxHeight() {
        return bilderThumbnailMaxHeight;
    }

    public int getBilderThumbnailMaxWidth() {
        return bilderThumbnailMaxWidth;
    }

    public String getExternDBUser() {
        return externDBUser;
    }

    public void setExternDBUser(String externDBUser) {
        this.externDBUser = externDBUser;
    }

    public String getExternDBPasswort() {
        return externDBPasswort;
    }

    public void setExternDBPasswort(String externDBPasswort) {
        this.externDBPasswort = externDBPasswort;
    }

    public String getExternDBDatenbank() {
        return externDBDatenbank;
    }

    public void setExternDBDatenbank(String externDBDatenbank) {
        this.externDBDatenbank = externDBDatenbank;
    }

    public String getExternDBServer() {
        return externDBServer;
    }

    public void setExternDBServer(String externDBServer) {
        this.externDBServer = externDBServer;
    }

    public String getExternDBUrl() {
        return externDBUrl;
    }

    public void setExternDBUrl(String externDBUrl) {
        this.externDBUrl = externDBUrl;
    }

    public String getEpsgWGS84() {
        return epsgWGS84;
    }

    public void setEpsgWGS84(String epsgWGS84) {
        this.epsgWGS84 = epsgWGS84;
    }

    public String getEpsgUTM33() {
        return epsgUTM33;
    }

    public void setEpsgUTM33(String epsgUTM33) {
        this.epsgUTM33 = epsgUTM33;
    }
}
