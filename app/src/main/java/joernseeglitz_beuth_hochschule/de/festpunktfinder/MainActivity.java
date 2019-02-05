package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends FragmentActivity {

    //Hauptmenuespinner
    Spinner spinner;

    ImageView imageViewIcons;

    Konstanten konstanten;

    //Config-Filename
    public static final String CONFIG_NAME = "ConfigFile";

    ArrayList<Fragment> fragments;

    //Index der Datenbeschaffungs-Intens
    private int PICK_IMAGE_REQUEST = 1;
    private int PICK_TEXTFILE_REQUEST = 2;
    private int GET_LOCATION_FROM_ACTIVITY = 3;

    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;

    public List<Bitmap> getBilder() {
        return bilder;
    }

    public void setBilder(List<Bitmap> bilder) {
        this.bilder = bilder;
    }

    private List<Bitmap> bilder = new ArrayList<>();



    private String punktNummerFuerBilder;
    private String projektNummerFuerBilder;

    private String projektNummerFuerDatei;
    private String dateiSucheFormat;
    private String dateiSucheKoordinatensystem;

    private List<GeodaetischerPunkt> kartenPunktListe;

    private Location locationAusActivity;

    private FragmentManager fragManager;
    private FragmentTransaction fragTransaction;

    private GeodaetischerPunkt selectedMarkerPunkt;

    //#################################################################

    /*
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
    /*
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


    public List<GeodaetischerPunkt> getKartenPunktListe() {
        return kartenPunktListe;
    }

    public void setKartenPunktListe(List<GeodaetischerPunkt> kartenPunktListe) {
        this.kartenPunktListe = kartenPunktListe;
    }

    //Einzelne Punkte werden in die Liste eingefuegt
    public void addToKartenPunktListe(GeodaetischerPunkt geodaetischerPunkt)
    {
        this.kartenPunktListe.add(geodaetischerPunkt);
    }

    public void setPunktNummerFuerBilder(String punktNummerFuerBilder) {
        if(isValidPunktNummer(punktNummerFuerBilder))this.punktNummerFuerBilder = punktNummerFuerBilder;
    }

    public void setProjektNummerFuerBilder(String projektNummerFuerBilder) {
        if (isValidProjektNummer(projektNummerFuerBilder))this.projektNummerFuerBilder = projektNummerFuerBilder;
    }

    public void setProjektNummerFuerDatei(String projektNummerFuerDatei) {
        if(isValidProjektNummer(projektNummerFuerDatei))this.projektNummerFuerDatei = projektNummerFuerDatei;
    }

    public String getDateiSucheFormat() {
        return dateiSucheFormat;
    }

    public void setDateiSucheFormat(String dateiSucheFormat) {
        this.dateiSucheFormat = dateiSucheFormat;
    }

    public String getDateiSucheKoordinatensystem() {
        return dateiSucheKoordinatensystem;
    }

    public void setDateiSucheKoordinatensystem(String dateiSucheKoordinatensystem) {
        this.dateiSucheKoordinatensystem = dateiSucheKoordinatensystem;
    }

    public Location getLocationAusActivity() {
        return locationAusActivity;
    }

    public void setLocationAusActivity(Location locationAusActivity) {
        this.locationAusActivity = locationAusActivity;
    }

    public GeodaetischerPunkt getSelectedMarkerPunkt() {
        return selectedMarkerPunkt;
    }

    public void setSelectedMarkerPunkt(GeodaetischerPunkt selectedMarkerPunkt) {
        this.selectedMarkerPunkt = selectedMarkerPunkt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageViewIcons = (ImageView) findViewById(R.id.imageViewIcon);

        //MapConfig-Intitialparameter setzen falls notwendig
        initialMapConfigSetzen();

        konstanten = new Konstanten(this);

        //Liste der Fragmente (Unterlayouts und Aktivitäten)
        fragments = new ArrayList<Fragment>();
        fragments.add(new StartFragment());
        fragments.add(new ImportFragment());
        fragments.add(new PunktuebersichtFragment());
        fragments.add(new joernseeglitz_beuth_hochschule.de.festpunktfinder.MapFragment());
        fragments.add(new EinstellungsFragment());


        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.dropdown_items));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(3,false);
        //Listener fuer das Dropdownmenue
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, parent.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                //textView.setText(parent.getSelectedItem().toString());
                try {
                    fragManager = getFragmentManager();
                    fragTransaction = fragManager.beginTransaction();
                    switch (position) {
                        case 0:
                            //Start
                            fragTransaction.replace(R.id.fragment_frame,fragments.get(0));
                            fragTransaction.commit();

                            imageViewIcons.setImageResource(android.R.drawable.ic_menu_myplaces);

                            break;
                        case 1:
                            //Import
                            fragTransaction.replace(R.id.fragment_frame, fragments.get(1), "TAGImportFrag");
                            fragTransaction.commit();

                            imageViewIcons.setImageResource(android.R.drawable.ic_menu_save);

                            break;
                        case 2:
                            //Punktuebersicht
                            fragTransaction.replace(R.id.fragment_frame, fragments.get(2),"TagPunktUeber");
                            fragTransaction.commit();

                            imageViewIcons.setImageResource(android.R.drawable.ic_menu_agenda);

                            break;
                        case 3:
                            //Karte
                            fragTransaction.replace(R.id.fragment_frame, fragments.get(3));
                            fragTransaction.commit();

                            imageViewIcons.setImageResource(android.R.drawable.ic_menu_mapmode);

                            break;
                        case 4:
                            //Einstellungen
                            fragTransaction.replace(R.id.fragment_frame, fragments.get(4));
                            fragTransaction.commit();

                            imageViewIcons.setImageResource(android.R.drawable.ic_menu_manage);

                            break;
                    }
                }
                catch(Exception e)
                {
                    String ex;
                    ex = e.getMessage().toString()
                            + "/// " + e.getCause().toString();
                    Log.e("ERROR",ex);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /*
    Methode um Bilder aus der Smartphone-Galerie auszuwaehlen
    Ergebnisse durch Handler fuer das ActivityResult
     */
    public void bilderSuche()
    {
        //Quelle: http://codetheory.in/android-pick-select-image-from-gallery-with-intents
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //Mehrfache Bildauswahl:
        //Quelle: http://stackoverflow.com/questions/19585815/select-multiple-images-from-android-gallery
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Bilder wählen"), PICK_IMAGE_REQUEST);
        Toast.makeText(this,"Für die Auswahl mehrer Bilder, bitte lange ein Bild tippen.",
                Toast.LENGTH_SHORT).show();
    }

    /*
    Methode um Textdateien auszuwaehlen
    Ergebnisse durch Handler fuer das ActivityResult
     */
    public void textDateiSuchen(String format, String koordinatensystem)
    {
        if (format.equals("XML"))
        {
            Intent intent = new Intent(this,
                    XmlEinleser.class);
            startActivity(intent);
        }
        else {
            //Benutzereingabeparameter setzen
            setDateiSucheFormat(format);
            setDateiSucheKoordinatensystem(koordinatensystem);

            //Intent initialisieren
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //intent.setType("text/plain");
            intent.setType("*/*");
            startActivityForResult(intent.createChooser(intent, "Datei wählen"), PICK_TEXTFILE_REQUEST);
        }
    }


    //Quelle: http://stackoverflow.com/questions/15124179/resizing-a-bitmap-to-a-fixed-value-but-without-changing-the-aspect-ratio
    public Bitmap scaleBitmap(Bitmap bm) {
        Konstanten konstanten = new Konstanten(this);
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

    //Handler um Bilder aus der Gallerie zu bekommen
    //Handler um Textdateien einzulesen
    //Quelle: http://codetheory.in/android-pick-select-image-from-gallery-with-intents/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //TODO: Ueberpruefung der Ausgabedateien aus dem Intent auf Konsistenz

        super.onActivityResult(requestCode, resultCode, data);

        //Verarbeitung des Bilderhinzufuegens:
        List<Bitmap> bilderL = new ArrayList<>();
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            //mehrere Bilder:
            //Quelle: http://stackoverflow.com/questions/19585815/select-multiple-images-from-android-gallery

            //fuer einzelnes Bild
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            List<String> imagesEncodedList = new ArrayList<String>();
            if(data.getData()!=null) {
                Uri uri = data.getData();
                // Get the cursor
                Cursor cursor = getContentResolver().query(uri,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imageEncoded = cursor.getString(columnIndex);
                cursor.close();


                try {
                    //Bitmap erzeugen
                    //dabei skalieren
                    Bitmap bitmap = scaleBitmap(
                            MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
                    // Log.d(TAG, String.valueOf(bitmap));

                    //Bitmap in Liste hinzufuegen
                    bilderL.add(bitmap);
                    setBilder(bilderL);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                //fuer mehrerre Bilder
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        mArrayUri.add(uri);
                        // Get the cursor
                        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String imageEncoded  = cursor.getString(columnIndex);
                        imagesEncodedList.add(imageEncoded);
                        cursor.close();

                        try {
                            //Bitmap erzeugen
                            //dabei skalieren
                            Bitmap bitmap = scaleBitmap(
                                    MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
                            // Log.d(TAG, String.valueOf(bitmap));
                            bilderL.add(bitmap);
                            setBilder(bilderL);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                }
            }

            //Bilderliste mit Punkt in der Datenbank verknuepfen
            //Aufruf jeweils fuer ein bestimmten Punkt
            String punktNummer = punktNummerFuerBilder;
            String projektNummer = projektNummerFuerBilder;

            MapDBDataSource dataSource = new MapDBDataSource(this);
            Log.d("MainActivity", "Die Datenquelle wird geöffnet.");
            dataSource.open();

            dataSource.insertInBilderDB(punktNummer,projektNummer,getBilder());

            if (getBilder().size()> 1) {
                Toast.makeText(MainActivity.this, "Bilder eingeladen.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if (getBilder().size()>0)
                {
                    Toast.makeText(MainActivity.this, "Bild eingeladen.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Achtung, kein Bild eingeladen!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            Log.d("MainActivity", "Die Datenquelle wird geschlossen.");
            dataSource.close();

            super.onActivityResult(requestCode, resultCode, data);

        }


        //####################################################################################
        //Verarbeitung des TextDateihinzufuegens:
        if (requestCode == PICK_TEXTFILE_REQUEST)
        {
            if(data != null) {
                Uri uri = data.getData();
                Log.i("MainActivity","Textfdatei einlesen wird gestartet.");
                textDateiEinlesen(uri);

            }
            else
            {
                Toast.makeText(this,"Keine Datei gewählt!",Toast.LENGTH_SHORT).show();
                Log.i("MainActivity","Textfdatei einlesen wurde nicht gestartet.");
            }
        }

        //Fuer die getLocationActivity aufruf aus dem Importfragment wird hiermit durch das nicht
        //behandeln der Anfrage der Importfragment-interne onActivityResult-Handler aufgerufen
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
    Methode zum Weiterverarbeiten der erhaltenen Textdatei
    einlesen der Datei
    Ausschreiben der Punkte
     */
    public void textDateiEinlesen(Uri uri)
    {
        //TODO: Dateiformat-Konsistenz pruefen


        BufferedReader reader;
        File file = new File(uri.getPath());
        //List<String> lines = new ArrayList<>();

        //Speicherlesen-Berechtigung-Abhandeln
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.d("PERMISSON", "in shouldShowRationale");
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]
                                    {android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
                }
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
            }
        } else {
            try
            {
                //Möglichweise nicht die bessere Variante
                //TODO: BufferedReader in Gange kriegen
                Scanner scan = new Scanner(file);
                String str;
                String [] strSplited;
                str = "";
                String punktNummer = "", punktArt = "", anmerkung = "";
                double rechtsWert,hochWert, longitude, latitude, hoehe;
                GeodaetischerPunkt geodaetischerPunkt = null;
                boolean punktNummerVorhanden = false;
                boolean warPunktNummerVorhanden = false;
                int vorhandenePunkte = 0;
                int punktAnzahl = 0;

                while(scan.hasNextLine()) {
                    punktNummerVorhanden = false;
                    str = scan.nextLine();
                    str = str.trim();

                    if(!str.isEmpty()) {
                        punktAnzahl++;

                        //Format- und Koordinatensystemvarianten behandeln

                        //PKT-Format
                        if(getDateiSucheFormat().equals("PKT")) {
                            if(getDateiSucheKoordinatensystem().equals("WGS84")) {
                                //#################################################################
                                //Fall: TXT - PKT - WGS84

                                //doppelte Leerzeichen entfernen
                                str = str.replaceAll(" +", " ");
                                //Zeile aufspalten mit Leerzeichen als Trennzeichen
                                strSplited = str.split(" ");
                                if (strSplited.length >= 5) {
                                    punktNummer = strSplited[0].trim();
                                    punktArt = strSplited[1].trim();
                                    longitude = Double.parseDouble(strSplited[2].trim());
                                    latitude = Double.parseDouble(strSplited[3].trim());
                                    hoehe = Double.parseDouble(strSplited[4].trim());
                                    geodaetischerPunkt = new GeodaetischerPunkt(punktNummer, longitude, latitude, hoehe,
                                            this.projektNummerFuerDatei, punktArt, 1,"");
                                } else {
                                    //TODO: Zu wenig Argumente abfangen
                                }
                                //##################################################################
                            }
                            else if(getDateiSucheKoordinatensystem().equals("ETRS89"))
                            {
                                //#################################################################
                                //Fall: TXT - PKT - ETRS89

                                //doppelte Leerzeichen entfernen
                                str = str.replaceAll(" +", " ");
                                //Zeile aufspalten mit Leerzeichen als Trennzeichen
                                strSplited = str.split(" ");
                                if (strSplited.length >= 5) {
                                    punktNummer = strSplited[0].trim();
                                    punktArt = strSplited[1].trim();
                                    rechtsWert = Double.parseDouble(strSplited[2].trim());
                                    hochWert = Double.parseDouble(strSplited[3].trim());
                                    hoehe = Double.parseDouble(strSplited[4].trim());
                                    geodaetischerPunkt = new GeodaetischerPunkt(punktNummer, rechtsWert, hochWert, hoehe,
                                            this.projektNummerFuerDatei, punktArt, 1,"");
                                } else {
                                    //TODO: Zu wenig Argumente abfangen
                                }
                                //##################################################################
                            }
                        }
                        else if (getDateiSucheFormat().equals("CSV"))
                        {
                            //Fall: TXT - CSV

                            //Zeile aufspalten mit Leerzeichen als Trennzeichen
                            strSplited = str.split(";");
                            if (strSplited.length >= 8) {
                                punktNummer = strSplited[0].trim();
                                punktArt = strSplited[1].trim();
                                if (strSplited[2].trim().equals(""))
                                {
                                    rechtsWert = 0.0;
                                }
                                else {
                                    rechtsWert = Double.parseDouble(strSplited[2].trim());
                                }
                                if (strSplited[3].trim().equals(""))
                                {
                                    hochWert = 0.0;
                                }
                                else {
                                    hochWert = Double.parseDouble(strSplited[3].trim());
                                }
                                if (strSplited[4].trim().equals(""))
                                {
                                    hoehe = 0.0;
                                }
                                else {
                                    hoehe = Double.parseDouble(strSplited[4].trim());
                                }
                                if (strSplited[5].trim().equals(""))
                                {
                                    longitude = 0.0;
                                }
                                else {
                                    longitude = Double.parseDouble(strSplited[5].trim());
                                }
                                if (strSplited[6].trim().equals(""))
                                {
                                    latitude = 0.0;
                                }
                                else {
                                    latitude = Double.parseDouble(strSplited[6].trim());
                                }
                                anmerkung = strSplited[7].trim();
                                geodaetischerPunkt = new GeodaetischerPunkt(punktNummer, rechtsWert, hochWert, hoehe,
                                        longitude,latitude,this.projektNummerFuerDatei, punktArt, anmerkung);
                            } else {
                                //TODO: Zu wenig Argumente abfangen
                            }
                        }

                        MapDBDataSource dataSource = new MapDBDataSource(this);
                        Log.d("MAINACTIVITY:", "Die Datenquelle wird geöffnet.");
                        dataSource.open();

                        List<GeodaetischerPunkt> punkteListe = dataSource.getAllMapDB();

                        //ueberpruefen ob der Punkt in dem Projekt bereits Vorhanden ist
                        for (GeodaetischerPunkt punkt : punkteListe
                                ) {
                            if (this.projektNummerFuerDatei.equals(punkt.getProjektNummer())) {
                                if (punktNummer.equals(punkt.getPunktNummer())) {
                                    punktNummerVorhanden = true;
                                    warPunktNummerVorhanden = true;
                                }
                            }
                        }
                        if(punktNummerVorhanden == false) {
                            dataSource.insertPunktInMapDB(geodaetischerPunkt.getPunktNummer(),
                                    geodaetischerPunkt.getRechts(),geodaetischerPunkt.getHoch(),
                                    geodaetischerPunkt.getHoehe(),
                                    geodaetischerPunkt.getLongitude(), geodaetischerPunkt.getLatitude(),
                                    geodaetischerPunkt.getProjektNummer(),
                                    geodaetischerPunkt.getPunktArt(),
                                    geodaetischerPunkt.getAnmerkung());
                        }
                        else
                        {
                            vorhandenePunkte++;
                        }

                        Log.d("MAINACTIVITY:", "Die Datenquelle wird geschlossen.");
                        dataSource.close();
                    }
                }
                scan.close();

                if(warPunktNummerVorhanden == true)
                {
                    if(punktAnzahl> 1) {
                        if(vorhandenePunkte > 1) {
                            Toast.makeText(this, "Es konnten " + vorhandenePunkte + " Punkte von "
                                            + punktAnzahl +
                                            "Punkten nicht eingeladen werden. Es gab bereits Punkte mit identischer Punktnummer!"
                                    , Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(this, "Ein Punkte von "
                                            + punktAnzahl +
                                            "Punkten konnte nicht eingeladen werden. Es gab bereits ein Punkt mit identischer Punktnummer!"
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(this, "Der Punkt konnte nicht eingeladen werden." +
                                        "Es gab bereits einen Punkt mit identischer Punktnummer!"
                                , Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(this, "Es wurden alle Punkte in das Projekt: "+
                                    this.projektNummerFuerDatei + " eingeladen."
                            , Toast.LENGTH_LONG).show();
                }


            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                Log.e("DATEIIMPORTERROR:",e.getMessage());
            }
        }
    }

    //Quelle: http://stackoverflow.com/questions/15124179/resizing-a-bitmap-to-a-fixed-value-but-without-changing-the-aspect-ratio
    public Bitmap scaleBitmapThumbnail(Bitmap bm) {
        Konstanten konstanten = new Konstanten(this);
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.v("Pictures", "Width and height are " + width + "--" + height);

        if (width > height) {
            // landscape
            float ratio = (float) width / konstanten.getBilderThumbnailMaxWidth();
            width = konstanten.getBilderThumbnailMaxWidth();
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / konstanten.getBilderThumbnailMaxHeight();
            height = konstanten.getBilderThumbnailMaxHeight();
            width = (int)(width / ratio);
        } else {
            // square
            height = konstanten.getBilderThumbnailMaxHeight();
            width = konstanten.getBilderThumbnailMaxWidth();
        }

        Log.v("Pictures", "after scaling Width and height are " + width + "--" + height);

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }


    public void punktInPunktUebersichtDarstellen()
    {
        fragTransaction = fragManager.beginTransaction();
//        PunktuebersichtFragment frag = (PunktuebersichtFragment) fragManager.findFragmentByTag("TagPunktUeber");
        PunktuebersichtFragment frag = new PunktuebersichtFragment();

        //Punktuebersicht aufrufen
        fragTransaction.replace(R.id.fragment_frame, frag);
//        fragTransaction.addToBackStack(null);
        fragTransaction.commit();

        spinner.setSelection(2);
    }

    /**
     * Methode um die Config-Datei-Parameter zu setzen
     * Fuer den Login einer externen Datenbank
     * Werte werden dauerhaft gespeichert
     * @param benutzerName String
     * @param passwort String
     * @param datenbankName String
     * @param serverName String
     * @param url String
     */
    public void setConfigExternDB(String benutzerName, String passwort, String datenbankName,
                                  String serverName, String url)
    {
        SharedPreferences configs = getSharedPreferences(CONFIG_NAME,0);
        SharedPreferences.Editor editor = configs.edit();

        editor.putString("externDBUser",benutzerName);
        editor.putString("externDBPasswort", passwort);
        editor.putString("externDBDatenbank",datenbankName);
        editor.putString("externDBServer",serverName);
        editor.putString("externDBUrl",url);

        editor.commit();

    }

    /**
     * Methode um Config-Datei-Parameter zu erhalten
     * Fuer Login einer externen Datenbank
     * @return Konstanten-Objekt
     */
    public Konstanten getConfigExternDB() {
        //Preferences Config-Daten wiederherstellen
        SharedPreferences configs = getSharedPreferences(CONFIG_NAME, 0);

        konstanten.setExternDBUser(configs.getString("externDBUser", null));
        konstanten.setExternDBPasswort(configs.getString("externDBPasswort", null));
        konstanten.setExternDBDatenbank(configs.getString("externDBDatenbank", null));
        konstanten.setExternDBServer(configs.getString("externDBServer", null));
        konstanten.setExternDBUrl(configs.getString("externDBUrl", null));

        return konstanten;
    }


    /**
     * Methode um beim App-Start abzufragen ob die Mapeinstellungen (Positionsabfrageintervall und
     * Map-Zoom-Entfernung) gesetzt wurden.
     * Sind Diese nicht gesetzt, werden default Werte initialisiert.
     */
    private void initialMapConfigSetzen()
    {
        SharedPreferences configs = getSharedPreferences(CONFIG_NAME,0);

        if(configs.getString("mapPosInterval",null) == null) {
            SharedPreferences.Editor editor = configs.edit();
            editor.putString("mapPosInterval", String.valueOf(1000));
            editor.commit();
        }
        else if(configs.getString("mapPosInterval",null).trim().equals(""))
        {
            SharedPreferences.Editor editor = configs.edit();
            editor.putString("mapPosInterval", String.valueOf(1000));
            editor.commit();
        }
        if(configs.getString("mapZoomEntfernung",null) == null) {
            SharedPreferences.Editor editor = configs.edit();
            editor.putString("mapZoomEntfernung", String.valueOf(100));
            editor.commit();
        }
        else if(configs.getString("mapZoomEntfernung",null).trim().equals(""))
        {
            SharedPreferences.Editor editor = configs.edit();
            editor.putString("mapZoomEntfernung", String.valueOf(100));
            editor.commit();
        }
    }

}
