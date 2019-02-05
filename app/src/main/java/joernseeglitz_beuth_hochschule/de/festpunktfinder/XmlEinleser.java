package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.util.Base64;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Klasse um XML-Dateien einzulesen
 */
public class XmlEinleser extends ListActivity {

    //Progress Dialog
    private ProgressDialog pDialog;

    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;

    private Uri dateiUri;

    Konstanten konstanten;

    List<GeodaetischerPunkt> geodaetischerPunktListe = new ArrayList<>();

    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        konstanten = new Konstanten(this);

        //Intent initialisieren
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent.createChooser(intent, "Datei wählen"), 4);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO: Ueberpruefung der Ausgabedateien aus dem Intent auf Konsistenz
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4) {
            if (data != null) {
                dateiUri = data.getData();
                Log.i("XmlEinleser", "Textfdatei einlesen wird gestartet.");

                //punkte in Hintergrund-Thread laden
                new LoadAllePunkte(this).execute();
            } else {
                Toast.makeText(this, "Keine Datei gewählt!", Toast.LENGTH_SHORT).show();
                Log.i("XmlEinleser", "Textfdatei einlesen wurde nicht gestartet.");
            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "XmlEinleser Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://joernseeglitz_beuth_hochschule.de.festpunktfinder/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "XmlEinleser Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://joernseeglitz_beuth_hochschule.de.festpunktfinder/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * Hintergrund Async Task um alle Punkte zu laden mittels HTTP Request
     */

    class LoadAllePunkte extends AsyncTask<String, String, String> {
        /**
         * Vor dem Hintergrund-Thread-Start soll ein Progress Dialog gezeigt werden
         */

        public Activity activity;

        public LoadAllePunkte(Activity parentActivity) {
            this.activity = parentActivity;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(XmlEinleser.this);
            pDialog.setMessage("Lade Punkte. Bitte warten...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            finish();
        }

        /**
         * alle Punkte von Pfad laden
         */
        protected String doInBackground(String... args) {

            //Speicherlesen-Berechtigung-Abhandeln
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d("PERMISSON", "in shouldShowRationale");
                    if (Build.VERSION.SDK_INT >= 23) {
                        requestPermissions(new String[]
                                        {Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
                    }
                } else {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
                }
            } else {
                try {
                    //XML einlesen

                    /*
                    Quelle: https://www.sitepoint.com/learning-to-parse-xml-data-in-your-android-app/
                     */
                    XmlPullParserFactory pullParserFactory;
                    try {
                        pullParserFactory = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = pullParserFactory.newPullParser();


                        String xmlPath = getPath(getApplicationContext(),dateiUri);

                        String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

                        FileInputStream inputStream = new FileInputStream(xmlPath);

                        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                        parser.setInput(inputStream, null);

                        //XML-Datei auslesen und geodaetischePunkteliste fuellen
                        parseXML(parser);

                        //Punkte in Datenbank einladen
                        if(geodaetischerPunktListe.size()>0)
                        {
                            MapDBDataSource dataSource = new MapDBDataSource(activity);
                            Log.d("XMLACTIVITY:", "Die Datenquelle wird geöffnet.");
                            dataSource.open();

                            List<GeodaetischerPunkt> punkteListe = dataSource.getAllMapDB();
                            boolean punktNummerVorhanden = false;
                            boolean warPunktNummerVorhanden = false;
                            int vorhandenePunkte = 0;
                            int punktAnzahl = geodaetischerPunktListe.size();

                            for (GeodaetischerPunkt xmlPunkt: geodaetischerPunktListe) {
                                //ueberpruefen ob der Punkt in dem Projekt bereits Vorhanden ist
                                for (GeodaetischerPunkt punkt : punkteListe
                                        ) {
                                    if (xmlPunkt.getProjektNummer().equals(punkt.getProjektNummer())) {
                                        if (xmlPunkt.getPunktNummer().equals(punkt.getPunktNummer())) {
                                            punktNummerVorhanden = true;
                                            warPunktNummerVorhanden = true;
                                        }
                                    }
                                }
                                if(punktNummerVorhanden == false) {
                                    dataSource.insertPunktInMapDB(xmlPunkt.getPunktNummer(),
                                            xmlPunkt.getRechts(),xmlPunkt.getHoch(),
                                            xmlPunkt.getHoehe(),
                                            xmlPunkt.getLongitude(), xmlPunkt.getLatitude(),
                                            xmlPunkt.getProjektNummer(),
                                            xmlPunkt.getPunktArt(),
                                            xmlPunkt.getAnmerkung());

                                    if(xmlPunkt.getBildListe() != null
                                            && xmlPunkt.getBildListe().size()>0) {
                                        dataSource.insertInBilderDB(xmlPunkt.getPunktNummer(),
                                                xmlPunkt.getProjektNummer(), xmlPunkt.getBildListe());
                                    }
                                }
                                else
                                {
                                    vorhandenePunkte++;
                                }
                            }
                            Log.d("XMLACTIVITY:", "Die Datenquelle wird geschlossen.");
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


                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * Methode um XML umzusetzen
         * <p/>
         * Quelle: https://www.sitepoint.com/learning-to-parse-xml-data-in-your-android-app/
         *
         * sowie
         * Quelle: http://theopentutorials.com/tutorials/android/xml/android-simple-xmlpullparser-tutorial/
         *
         * @param parser uebergebener XmlPullParser
         * @throws XmlPullParserException
         * @throws IOException
         */
        private void parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
            List<GeodaetischerPunkt> xmlPunkte = null;

            int eventType = parser.getEventType();

            GeodaetischerPunkt geodaetischerPunkt = null;

            List<Bitmap> bilderListe = new ArrayList<>();

            String text ="";

            String punktNummer = null;
            String projektNummer = null;
            String punktArt = null;
            String anmerkung = null;
            double rechtsWert = 0;
            double hochWert = 0;
            double hoehe = 0;
            double latitude = 0;
            double longitude = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {


                String  tagName = parser.getName();


                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("GeodaetischerPunkt")
                                ) {
                            bilderListe = new ArrayList<>();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;


                    case XmlPullParser.END_TAG:
                        switch (tagName) {
                            case "PunktNummer":
                                punktNummer = text.trim();
                                break;
                            case "ProjektNummer":
                                projektNummer = text.trim();
                                break;
                            case "PunktArt":
                                punktArt = text.trim();
                                break;
                            case "Anmerkung":
                                anmerkung = text.trim();
                                break;
                            case "RechtsWert":
                                rechtsWert = Double.parseDouble(
                                        text.trim().replace(",", "."));
                                break;
                            case "HochWert":
                                hochWert = Double.parseDouble(
                                        text.trim().trim().replace(",", "."));
                                break;
                            case "Hoehe":
                                hoehe = Double.parseDouble(
                                        text.trim().trim().replace(",", "."));
                                break;
                            case "Longitude":
                                longitude = Double.parseDouble(
                                        text.trim().trim().replace(",", "."));
                                break;
                            case "Latitude":
                                latitude = Double.parseDouble(
                                        text.trim().trim().replace(",", "."));
                                break;
                            case "Bild":
                                byte[] bild = Base64.decode(
                                        text.trim().trim().getBytes(), Base64.DEFAULT);
                                bilderListe.add(scaleBitmap(
                                        BitmapFactory.decodeByteArray(bild, 0, bild.length)
                                        )
                                );
                                break;
                        }
                        if (tagName.equalsIgnoreCase("GeodaetischerPunkt")
                                ) {
                            //Geodaetischer Punkt erstellen mit den XML-Werten
                            geodaetischerPunkt = new GeodaetischerPunkt(punktNummer, rechtsWert,
                                    hochWert, hoehe, longitude, latitude, projektNummer, punktArt
                                    , anmerkung);
                            if(bilderListe != null && bilderListe.size()>0) {
                                //Falls vorhanden Bilderliste hinzuuegen
                                geodaetischerPunkt.setBildListe(bilderListe);
                            }
                            //GeodaetischerPunkt der Liste hinzufuegen
                            geodaetischerPunktListe.add(geodaetischerPunkt);
                        }
                        break;
                    default:
                        break;
                }

                eventType = parser.next();
            }
        }


    }

    /**
     * Quelle: http://stackoverflow.com/questions/33208911/get-realpath-return-null-on-android-marshmallow
     * <p/>
     * <p/>
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    //Quelle: http://stackoverflow.com/questions/15124179/resizing-a-bitmap-to-a-fixed-value-but-without-changing-the-aspect-ratio
    //Beitrag:  answered Apr 5 '14 at 15:29 von: Coen Damen
    //letzte Sichtung: 21.06.16 21:30 Uhr
    public Bitmap scaleBitmap(Bitmap bm) {
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
