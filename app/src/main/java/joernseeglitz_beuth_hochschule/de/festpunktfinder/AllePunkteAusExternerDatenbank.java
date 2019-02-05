package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Quelle: http://www.androidhive.info/2012/05/how-to-connect-android-with-php-mysql/
 */
public class AllePunkteAusExternerDatenbank extends ListActivity {

    //Progress Dialog
    private ProgressDialog pDialog;

    Konstanten konstanten;

    List<GeodaetischerPunkt> geodaetischerPunktListe = new ArrayList<>();

    public List<GeodaetischerPunkt> getImportPunktListe() {
        return importPunktListe;
    }

    public void setImportPunktListe(List<GeodaetischerPunkt> importPunktListe) {
        this.importPunktListe = importPunktListe;
    }

    List<GeodaetischerPunkt> importPunktListe = new ArrayList<>();
    GeodaetischerPunkt geodaetischerPunkt;

    //punkte JSON-Array
    JSONArray jsonPunkte = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        konstanten = new Konstanten(this);

        konstanten = getConfigExternDB();

        //punkte in Hintergrund-Thread laden
        new LoadAllePunkte(this).execute();

    }

    /**
     * Methode um Config-Datei-Parameter zu erhalten
     * Fuer Login einer externen Datenbank
     * @return Konstanten-Objekt
     */
    public Konstanten getConfigExternDB() {
        //Preferences Config-Daten wiederherstellen
        SharedPreferences configs = getSharedPreferences("ConfigFile", 0);

        konstanten.setExternDBUser(configs.getString("externDBUser", null));
        konstanten.setExternDBPasswort(configs.getString("externDBPasswort", null));
        konstanten.setExternDBDatenbank(configs.getString("externDBDatenbank", null));
        konstanten.setExternDBServer(configs.getString("externDBServer", null));
        konstanten.setExternDBUrl(configs.getString("externDBUrl", null));

        return konstanten;
    }


    /**
     * Hintergrund Async Task um alle Punkte zu laden mittels HTTP Request
     */

    class LoadAllePunkte extends AsyncTask<String, String, String> {
        /**
         * Vor dem Hintergrund-Thread-Start soll ein Progress Dialog gezeigt werden
         */

        public Activity activity;

        public LoadAllePunkte(Activity parentActivity)
        {
            this.activity = parentActivity;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AllePunkteAusExternerDatenbank.this);
            pDialog.setMessage("Lade Punkte. Bitte warten...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            pDialog.dismiss();
            finish();
        }

        /**
         * alle Punkte von url laden
         */
        protected String doInBackground(String... args){

            //String s = args[0];
            BufferedReader bufferedReader = null;
            //JSON String von url bekommen
            String resultString = null;

            try {
                String parameterString =
                        "?db_benutzer="+konstanten.getExternDBUser()+
                                "&db_passwort="+konstanten.getExternDBPasswort()+
                                "&db_datenbank="+konstanten.getExternDBDatenbank()+
                                "&db_server="+konstanten.getExternDBServer();
                URL url = new URL(konstanten.getExternDBUrl()+parameterString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                resultString = bufferedReader.readLine();

                try {
                    JSONObject jsonObject = new JSONObject(resultString);

                    jsonPunkte = jsonObject.getJSONArray("punkte");

                    for (int i = 0; i < jsonPunkte.length(); i++) {


                        String punktNummer = jsonPunkte.getJSONObject(i).getString("punktNummer");
                        String projektNummer = jsonPunkte.getJSONObject(i).getString("projektNummer");
                        String rechtsWert = jsonPunkte.getJSONObject(i).getString("rechtsWert");
                        String hochWert = jsonPunkte.getJSONObject(i).getString("hochWert");
                        String hoehe = jsonPunkte.getJSONObject(i).getString("hoehe");
                        String latitude = jsonPunkte.getJSONObject(i).getString("latitude");
                        String longitude = jsonPunkte.getJSONObject(i).getString("longitude");
                        String punktArt = jsonPunkte.getJSONObject(i).getString("punktArt");

                        geodaetischerPunkt = new GeodaetischerPunkt(punktNummer,
                                Double.parseDouble(rechtsWert), Double.parseDouble(hochWert),
                                Double.parseDouble(hoehe), Double.parseDouble(longitude),
                                Double.parseDouble(latitude), projektNummer, punktArt,"");
                        geodaetischerPunktListe.add(geodaetischerPunkt);
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }

                setImportPunktListe(geodaetischerPunktListe);


                List<GeodaetischerPunkt> finalImportPunkteListe = getImportPunktListe();

                if(finalImportPunkteListe.size()>0)
                {
                    MapDBDataSource dataSource = new MapDBDataSource(activity);
                    Log.d("EXTERNACTIVITY:", "Die Datenquelle wird geöffnet.");
                    dataSource.open();

                    List<GeodaetischerPunkt> punkteListe = dataSource.getAllMapDB();
                    boolean punktNummerVorhanden = false;
                    boolean warPunktNummerVorhanden = false;
                    int vorhandenePunkte = 0;
                    int punktAnzahl = finalImportPunkteListe.size();

                    for (GeodaetischerPunkt dbPunkt: finalImportPunkteListe) {
                        //ueberpruefen ob der Punkt in dem Projekt bereits Vorhanden ist
                        for (GeodaetischerPunkt punkt : punkteListe
                                ) {
                            if (dbPunkt.getProjektNummer().equals(punkt.getProjektNummer())) {
                                if (dbPunkt.getPunktNummer().equals(punkt.getPunktNummer())) {
                                    punktNummerVorhanden = true;
                                    warPunktNummerVorhanden = true;
                                }
                            }
                        }
                        if(punktNummerVorhanden == false) {
                            dataSource.insertPunktInMapDB(dbPunkt.getPunktNummer(),
                                    dbPunkt.getRechts(),dbPunkt.getHoch(),
                                    dbPunkt.getHoehe(),
                                    dbPunkt.getLongitude(), dbPunkt.getLatitude(),
                                    dbPunkt.getProjektNummer(),
                                    dbPunkt.getPunktArt(), "");
                        }
                        else
                        {
                            vorhandenePunkte++;
                        }
                    }
                    Log.d("EXTERNACTIVITY:", "Die Datenquelle wird geschlossen.");
                    dataSource.close();

                    if(warPunktNummerVorhanden == true)
                    {
                        if(punktAnzahl> 1) {
                            if(vorhandenePunkte > 1) {
                                Toast.makeText(getApplicationContext(), "Es konnten " + vorhandenePunkte + " Punkte von "
                                                + punktAnzahl +
                                                "Punkten nicht eingeladen werden. Es gab bereits Punkte mit identischer Punktnummer!"
                                        , Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "Ein Punkte von "
                                                + punktAnzahl +
                                                "Punkten konnte nicht eingeladen werden. Es gab bereits ein Punkt mit identischer Punktnummer!"
                                        , Toast.LENGTH_LONG).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Der Punkt konnte nicht eingeladen werden." +
                                            "Es gab bereits einen Punkt mit identischer Punktnummer!"
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Es wurden alle Punkte eingeladen."
                                , Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Error:\nEs konnten keine Punkte eingelesen werden!",Toast.LENGTH_LONG).show();
                }


                return resultString;
            }
            catch (Exception e)
            {
                return null;
            }

        }

    }
}
