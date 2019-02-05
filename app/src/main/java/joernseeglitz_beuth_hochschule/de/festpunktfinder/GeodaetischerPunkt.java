package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import org.cts.CRSFactory;
import org.cts.IllegalCoordinateException;
import org.cts.crs.CRSException;
import org.cts.crs.CoordinateReferenceSystem;
import org.cts.crs.GeodeticCRS;
import org.cts.op.CoordinateOperation;
import org.cts.op.CoordinateOperationFactory;
import org.cts.registry.EPSGRegistry;
import org.cts.registry.RegistryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasse zur Erstellung eines geodaetischn Punktobjektes
 */
public class GeodaetischerPunkt {
    private String punktNummer;
    private String projektNummer;
    private String punktArt;

    private String anmerkung;
    private double rechts;
    private double hoch;
    private double hoehe;
    private double latitude;
    private double longitude;

    /*
    Die Koordinatensystem-ID wird dazu verwendet um zu differenzieren welche Koordinaten
    beim erstellen des Objektes innerhalb des Konstruktors verwendet werden.
    0 = UTM-Koordinaten
    1 = geographische-Koordinaten
     */
    private int koordinatenSystemID;

    private List<Bitmap> bildListe;
    private long punktID;


    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Hoehe und Projektnummer, Punktart,
     Anmerkung uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              double hoehe,double longitude, double latitude,
                              String projektNummer, String punktArt, String anmerkung){
        Konstanten konstanten = new Konstanten();

        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        if(isValidLongitude(longitude))this.longitude = longitude;
        if(isValidLatitude(latitude))this.latitude = latitude;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;
        if (isValidPunktArt(punktArt))this.punktArt = punktArt;
        if (isValidAnmerkung(anmerkung))this.anmerkung = anmerkung;

        if (this.rechts == 0 && this.hoch == 0)
        {
            double[] rechtsHoch = cts_trafo(konstanten.getEpsgWGS84(),konstanten.getEpsgUTM33(),
                    this.getLongitude(),this.getLatitude());
            if (isValidRechtsWert(rechtsHoch[0])) this.rechts = rechtsHoch[0];
            if (isValidHochWert(rechtsHoch[1])) this.hoch = rechtsHoch[1];
        }
        else if(this.longitude == 0 && this.latitude == 0)
        {
            double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                    this.getRechts(),this.getHoch());
            if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
            if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
        }
    }

    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert und Hoehe uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert, double hoehe){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert)) this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidHoehe(hoehe))this.hoehe = hoehe;


        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }
    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Hoehe und Projektnummer
     uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              double hoehe, String projektNummer){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }
    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Hoehe und Projektnummer, Punktart
     uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              double hoehe, String projektNummer, String punktArt, String anmerkung){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;
        if (isValidPunktArt(punktArt))this.punktArt = punktArt;
        if (isValidAnmerkung(anmerkung))this.anmerkung = anmerkung;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }

    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Hoehe und Projektnummer, Punktart
     uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert_oder_longitude
            , double hochWert_oder_latitude, double hoehe, String projektNummer, String punktArt
            , int koordinatenSystemID, String anmerkung){
        Konstanten konstanten = new Konstanten();

        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;

        switch (koordinatenSystemID) {
            case 0: //ETRS
                if (isValidRechtsWert(rechtsWert_oder_longitude)) this.rechts = rechtsWert_oder_longitude;
                if (isValidHochWert(hochWert_oder_latitude)) this.hoch = hochWert_oder_latitude;

                double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                        this.getRechts(),this.getHoch());
                if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
                if (isValidHochWert(longLat[1])) this.latitude = longLat[1];

                break;
            case 1: //geogr.
                if(isValidLongitude(rechtsWert_oder_longitude)) this.longitude = rechtsWert_oder_longitude;
                if(isValidLatitude(hochWert_oder_latitude)) this.latitude = hochWert_oder_latitude;

                double[] rechtsHoch = cts_trafo(konstanten.getEpsgWGS84(),konstanten.getEpsgUTM33(),
                        this.getLongitude(),this.getLatitude());
                if (isValidRechtsWert(rechtsHoch[0])) this.rechts = rechtsHoch[0];
                if (isValidHochWert(rechtsHoch[1])) this.hoch = rechtsHoch[1];

                break;
            default:
                throw new IllegalArgumentException("KoordinatensystemID ist ungueltig!");
        }
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;
        if (isValidPunktArt(punktArt))this.punktArt = punktArt;
        if (isValidAnmerkung(anmerkung))this.anmerkung = anmerkung;
    }

    /**
     Konstruktor bei der Punktnummer, Rechtswert und Hochwert uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }
    /**
     Konstruktor bei der Punktnummer, Rechtswert und Hochwert uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert_oder_longitude,
                              double hochWert_oder_latitude,
                              int koordinatenSystemID){
        Konstanten konstanten = new Konstanten();

        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        switch (koordinatenSystemID) {
            case 0: //ETRS
                if (isValidRechtsWert(rechtsWert_oder_longitude)) this.rechts = rechtsWert_oder_longitude;
                if (isValidHochWert(hochWert_oder_latitude)) this.hoch = hochWert_oder_latitude;

                double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                        this.getRechts(),this.getHoch());
                if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
                if (isValidHochWert(longLat[1])) this.latitude = longLat[1];

                break;
            case 1: //geogr.
                if(isValidLongitude(rechtsWert_oder_longitude)) this.longitude = rechtsWert_oder_longitude;
                if(isValidLatitude(hochWert_oder_latitude)) this.latitude = hochWert_oder_latitude;

                double[] rechtsHoch = cts_trafo(konstanten.getEpsgWGS84(),konstanten.getEpsgUTM33(),
                        this.getLongitude(),this.getLatitude());
                if (isValidRechtsWert(rechtsHoch[0])) this.rechts = rechtsHoch[0];
                if (isValidHochWert(rechtsHoch[1])) this.hoch = rechtsHoch[1];

                break;
            default:
                throw new IllegalArgumentException("KoordinatensystemID ist ungueltig!");
        }
    }
    /**
     Konstruktor bei der Punktnummer, Rechtswert und Hochwert uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              String projektNummer){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }
    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Projektnummer, Punktart uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              String projektNummer, String punktArt){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;
        if (isValidPunktArt(punktArt))this.punktArt = punktArt;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }

    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Hoehe und ein Liste von Bitmap
     uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              double hoehe, List<Bitmap> bilderListe){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        if (isValidBilderListe(bilderListe))this.bildListe = bilderListe;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }
    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Hoehe und ein Liste von Bitmap
     und die Projektnummer uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              double hoehe, List<Bitmap> bilderListe, String projektNummer){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        if (isValidBilderListe(bilderListe))this.bildListe = bilderListe;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }
    /**
     Konstruktor bei der Punktnummer, Rechtswert, Hochwert, Hoehe und ein Liste von Bitmap
     und die Projektnummer, Punktart uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, double rechtsWert, double hochWert,
                              double hoehe, List<Bitmap> bilderListe,
                              String projektNummer, String punktArt){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidRechtsWert(rechtsWert))this.rechts = rechtsWert;
        if (isValidHochWert(hochWert))this.hoch = hochWert;
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        if (isValidBilderListe(bilderListe))this.bildListe = bilderListe;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;
        if (isValidPunktArt(punktArt))this.punktArt = punktArt;

        Konstanten konstanten = new Konstanten();
        double[] longLat = cts_trafo(konstanten.getEpsgUTM33(),konstanten.getEpsgWGS84(),
                this.getRechts(),this.getHoch());
        if (isValidRechtsWert(longLat[0])) this.longitude = longLat[0];
        if (isValidHochWert(longLat[1])) this.latitude = longLat[1];
    }

    /**
     Konstruktor bei der Punktnummer, eine Liste von Bitmap
     und die Projektnummer uebergeben werden
     */
    public GeodaetischerPunkt(String punktNummer, String projektNummer, List<Bitmap> bilderListe){
        if (isValidPunktNummer(punktNummer)) this.punktNummer = punktNummer;
        if (isValidBilderListe(bilderListe))this.bildListe = bilderListe;
        if (isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;
    }

    /**
     Konstruktor bei der eine Liste von Bitmap und Punkt-ID
     uebergeben werden
     */
    public GeodaetischerPunkt(List<Bitmap> bilderListe, long punktID){
        if (isValidBilderListe(bilderListe))this.bildListe = bilderListe;
        if (isValidPunktID(punktID))this.punktID = punktID;
    }

    /**
     Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
     */
    private static boolean isValidPunktNummer(String punktNummer)
    {
        if(punktNummer != null && !punktNummer.trim().isEmpty()) {
            return true;
        }
        else
        {
            throw new IllegalArgumentException("Punktnummer ist leer oder null!");
        }
    }
    /**
     Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
     */
    private static boolean isValidProjektNummer(String projektNummer)
    {
        if (projektNummer == null)
        {
            return true;
        }
        else {
            if (!projektNummer.trim().isEmpty()) {
                return true;
            } else {
                if (projektNummer.isEmpty())
                {
                    return true;
                }
                else {
                    throw new IllegalArgumentException("Projektnummer ist leer!");
                }
            }
        }
    }
    /*
   Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
    */
    private static boolean isValidPunktArt(String punktArt)
    {
        if (punktArt == null)
        {
            return true;
        }
        else {
            if (!punktArt.trim().isEmpty()) {
                return true;
            } else {
                if (punktArt.isEmpty())
                {
                    return true;
                }
                else {
                    throw new IllegalArgumentException("Punktart ist leer!");
                }
            }
        }
    }

    /*
  Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
   */
    private static boolean isValidAnmerkung(String anmerkung)
    {
        if (anmerkung == null)
        {
            return true;
        }
        else {
            if (!anmerkung.trim().isEmpty()) {
                return true;
            } else {
                if (anmerkung.isEmpty())
                {
                    return true;
                }
                else {
                    throw new IllegalArgumentException("Anmerkung ist leer!");
                }
            }
        }
    }

    /*
    Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
     */
    private static boolean isValidRechtsWert(double rechtsWert)
    {
        if(rechtsWert >= 0.0 && rechtsWert < 10000000) {
            return true;
        }
        else
        {
            throw new IllegalArgumentException("RechtsWert ist ungueltig!");
        }
    }
    /*
   Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
    */
    private static boolean isValidHochWert(double hochWert)
    {
        if(hochWert >= 0.0 && hochWert < 10000000) {
            return true;
        }
        else
        {
            throw new IllegalArgumentException("HochWert ist ungueltig!");
        }
    }
    /*
   Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
    */
    private static boolean isValidHoehe(double hoehe)
    {
        if(hoehe > -1000 && hoehe < 100000) {
            return true;
        }
        else
        {
            throw new IllegalArgumentException("Hoehe ist ungueltig!");
        }
    }
    /*
       Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
        */
    private static boolean isValidLatitude(double latitude)
    {
        if(latitude >= 0.0 && latitude < 181) {
            return true;
        }
        else
        {
            throw new IllegalArgumentException("Latitude ist ungueltig!");
        }
    }
    /*
      Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
       */
    private static boolean isValidLongitude(double longitude)
    {
        if(longitude >= 0.0 && longitude < 361) {
            return true;
        }
        else
        {
            throw new IllegalArgumentException("Longitude ist ungueltig!");
        }
    }
    /*
  Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
   */
    private static boolean isValidPunktID(long punktID)
    {
        if(punktID > 0 ) {
            return true;
        }
        else
        {
            throw new IllegalArgumentException("PunktID ist ungueltig!");
        }
    }
    /*
  Methode zum ueberpruefen der Gueltigkeit der uebergebenen Werte.
   */
    private static boolean isValidBilderListe(List<Bitmap> bilderListe)
    {
        if (bilderListe != null)
        {
            for (Bitmap bild:bilderListe
                    ) {
                if (bild == null)
                {
                    throw new IllegalArgumentException("Bilder ist ungueltig!");
                }
                else
                {
                    return true;
                }
            }
            return true;
        }
        else
        {
            throw new IllegalArgumentException("Bilder ist ungueltig!");
        }
    }




    public String getPunktNummer() {
        return punktNummer;
    }

    public void setPunktNummer(String punktNummer) {
        if(isValidPunktNummer(punktNummer)) {
            this.punktNummer = punktNummer;
        }
        else
        {
            Log.w("UNGUELTIGE_EINGABE","Punktnummer ist leer oder null!");
        }
    }

    public String getPunktArt() {
        return punktArt;
    }

    public void setPunktArt(String punktArt) {
        if (isValidPunktArt(punktArt))
            this.punktArt = punktArt;
        else
        {
            Log.w("UNGUELTIGE_EINGABE","Punktart ist leer oder null!");
        }
    }

    public String getProjektNummer() {
        return projektNummer;
    }

    public void setProjektNummer(String projektNummer) {
        if(isValidProjektNummer(projektNummer))this.projektNummer = projektNummer;
        else Log.w("UNGUELTIGE_EINGABE","Projektnummer ist leer oder null!");
    }

    public double getRechts() {
        return rechts;
    }

    public void setRechts(double rechts) {
        if (isValidRechtsWert(rechts))this.rechts = rechts;
        else Log.w("UNGUELTIGE_EINGABE","Rechtswert ist ungueltig!");
    }

    public double getHoch() {
        return hoch;
    }

    public void setHoch(double hoch) {
        if(isValidHochWert(hoch)) this.hoch = hoch;
        else Log.w("UNGUELTIGE_EINGABE","Hochwert ist ungueltig!");
    }

    public double getHoehe() {
        return hoehe;
    }

    public void setHoehe(double hoehe) {
        if (isValidHoehe(hoehe))this.hoehe = hoehe;
        else Log.w("UNGUELTIGE_EINGABE","Hoehe ist ungueltig!");
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        if(isValidLatitude(latitude))this.latitude = latitude;
        else Log.w("UNGUELTIGE_EINGABE","Latitude ist ungueltig!");
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        if(isValidLongitude(longitude))this.longitude = longitude;
        else Log.w("UNGUELTIGE_EINGABE","Longitude ist ungueltig!");
    }

    public List<Bitmap> getBildListe() {
        return bildListe;
    }

    public void setBildListe(List<Bitmap> bildListe) {
        if (isValidBilderListe(bildListe))this.bildListe = bildListe;
        else Log.w("UNGUELTIGE_EINGABE","Bilderliste ist ungueltig!");
    }

    public long getPunktID() {
        return punktID;
    }

    public void setPunktID(int punktID) {
        if(isValidPunktID(punktID))this.punktID = punktID;
        else Log.w("UNGUELTIGE_EINGABE","Rechtswert ist ungueltig!");
    }

    public String getAnmerkung() {
        return anmerkung;
    }

    public void setAnmerkung(String anmerkung) {
        if (isValidPunktArt(anmerkung))
            this.punktArt = anmerkung;
        else
        {
            Log.w("UNGUELTIGE_EINGABE","Punktart ist leer oder null!");
        }
    }

    /**
     * Transformation mit Hilfe Orbisgis CTS
     * http://orbisgis.org/
     * src: https://github.com/orbisgis/cts
     * hierbei Library geladen ueber gradle-dependencies
     *
     * Quelle: http://gis.stackexchange.com/questions/151567/how-to-do-a-coordinate-transformation-with-epsg-codes-in-cts
     *
     * @param ausgangsEPSG EPSG-Code des Ausgangssystems
     * @param zielEPSG EPSG-Code des Zielsystems
     * @param rechtsWert_oder_longitude Koordinaten des Rechtswert oder der geogr. Laenge
     * @param hochWert_oder_latitude Koordinaten des Hochwert oder der geogr. Breite
     * @return gibt ein zweistelliges Array der transformierten Koordinaten zurueck
     */
    public double[] cts_trafo(String ausgangsEPSG, String zielEPSG,
                              double rechtsWert_oder_longitude, double hochWert_oder_latitude)
    {
        double[] koordinaten = new double[2];
        double[]dd = null;

        koordinaten[0] = rechtsWert_oder_longitude;
        koordinaten[1] = hochWert_oder_latitude;

        CRSFactory crsFactory = new CRSFactory();
        RegistryManager registryManager = crsFactory.getRegistryManager();
        registryManager.addRegistry(new EPSGRegistry());
        try {
            CoordinateReferenceSystem crs1 = crsFactory.getCRS(ausgangsEPSG);
            CoordinateReferenceSystem crs2 = crsFactory.getCRS(zielEPSG);

            GeodeticCRS sourceGCRS = (GeodeticCRS) crs1;
            GeodeticCRS targetGCRS = (GeodeticCRS) crs2;
            List<CoordinateOperation> coordOps =
                    CoordinateOperationFactory.createCoordinateOperations(sourceGCRS,targetGCRS);

            if(coordOps != null && coordOps.size() !=0)
            {
                for(CoordinateOperation op:coordOps)
                {
                    try {
                        dd = op.transform(koordinaten);
                        for(int i = 0; i< dd.length;i++)
                        {
                            System.out.println(dd[i]); // for debugging
                        }
                    } catch (IllegalCoordinateException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (CRSException e) {
            e.printStackTrace();
        }
        return dd;
    }
}
