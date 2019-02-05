package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ImportFragment extends Fragment {
    TextView textView;
    TextView textViewDateiHinzufuegenNeueProjektnummer,
            textViewDateiHinzufuegenBestehendeProjektnummern;
    TextView textViewImportKartePunktNummer;

    RadioButton radioButton1, radioButton2, radioButton3;
    RadioButton radioButtonDateiHinzufuegenNeuesProjekt, radioButtonDateiHinzufuegenProjektWaehlen;

    RadioGroup radioGroup1;
    RadioGroup radioGroupDateiHinzufuegen;

    Button punktHinzufuegenButton;
    Button bilderButton;
    Button dateiSuchenButton;
    Button importKarteButton;
    Button importKarteAlleButton;
    Button importExternAllePunkteButton;
    Button importKarteUmkreisButton;

    LinearLayout manuelleEingabeLayout;
    LinearLayout dateiSuchenLayout;
    LinearLayout kartenImportLayout;
    LinearLayout externerImportLayout;
    LinearLayout dateiSuchenKoordinatensystem;

    Context context;

    private static final String LOG_TAG = ImportFragment.class.getSimpleName();

    EditText editTextProjektNummer, editTextPunktnummer, editTextRechtsWert, editTextHochWert,
            editTextHoehe,editTextLongitude,editTextLatitude,editTextPunktArt, editTextAnmerkung;
    EditText editTextDateiHinzufuegenNeuesProjekt;
    EditText editTextImportUmkreis;


    Spinner spinnerDateiHinzufuegenProjekte;
    Spinner spinnerBilderProjektNummer;
    Spinner spinnerBilderPunktNummer;
    Spinner spinnerImportKarteProjektNummer;
    Spinner spinnerImportKartePunktNummer;
    Spinner spinnerDateiHinzufuegenFormat;
    Spinner spinnerDateiHinzufuegenKoordinatensystem;

    Konstanten konstanten;

    List<GeodaetischerPunkt> finalInKartenImportPunkte;
    List<GeodaetischerPunkt> umkreisPunktListe;

    private Location standortFragment;

    public Location getStandortFragment() {
        return standortFragment;
    }

    public void setStandortFragment(Location standortFragment) {
        this.standortFragment = standortFragment;
    }

    private double umkreis;


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Quelle: http://www.techotopia.com/index.php/Using_Fragments_in_Android_Studio_-_An_Example
        // und http://stackoverflow.com/questions/17400230/onclick-not-working-inside-of-fragment
        final View view = inflater.inflate(R.layout.importfragment, container, false);
        //Radiobuttons
        radioButton1 = (RadioButton) view.findViewById(R.id.radioButton);
        radioButton2 = (RadioButton) view.findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) view.findViewById(R.id.radioButton3);
        radioGroup1 = (RadioGroup) view.findViewById(R.id.radioGroup);
        radioGroupDateiHinzufuegen = (RadioGroup) view.findViewById(R.id.radioGroupDateiHinzufuegen);
        radioButtonDateiHinzufuegenNeuesProjekt = (RadioButton)
                view.findViewById(R.id.radioButtonDateiHinzufuegenNeuesProjekt);
        radioButtonDateiHinzufuegenProjektWaehlen = (RadioButton)
                view.findViewById(R.id.radioButtonDateiHinzufuegenZuProjektHinzufuegen);

        //TextViews
        textView = (TextView) view.findViewById(R.id.textView5);
        textViewDateiHinzufuegenNeueProjektnummer = (TextView) view.findViewById(R.id.textView18);
        textViewDateiHinzufuegenNeueProjektnummer.setVisibility(TextView.GONE);
        textViewDateiHinzufuegenBestehendeProjektnummern = (TextView) view.findViewById(R.id.textView19);
        textViewDateiHinzufuegenBestehendeProjektnummern.setVisibility(TextView.GONE);
        textViewImportKartePunktNummer = (TextView) view.findViewById(R.id.textViewImportKartePunktNummer);
        textViewImportKartePunktNummer.setVisibility(TextView.GONE);

        //Buttons
        dateiSuchenButton = (Button) view.findViewById(R.id.buttonDateiSuchenundImportieren);
        punktHinzufuegenButton = (Button) view.findViewById(R.id.buttonPunktHinzufuegen);
        bilderButton = (Button) view.findViewById(R.id.importButtonBilder);
        importKarteButton = (Button) view.findViewById(R.id.importButtonKarteEinladen);
        importKarteAlleButton = (Button) view.findViewById(R.id.importButtonKarteEinladenAlle);
        importExternAllePunkteButton = (Button) view.findViewById(R.id.importButtonExterneDBAllePunkte);
        importKarteUmkreisButton = (Button) view.findViewById(R.id.importButtonKarteEinladenUmkreis);

        //Layouts
        manuelleEingabeLayout = (LinearLayout) view.findViewById(R.id.ManuelleEingabenLinearLayout);
        manuelleEingabeLayout.setVisibility(LinearLayout.GONE);
        dateiSuchenLayout = (LinearLayout) view.findViewById(R.id.DateiSuchenLinearLayout);
        dateiSuchenLayout.setVisibility(LinearLayout.GONE);
        kartenImportLayout = (LinearLayout) view.findViewById(R.id.DatenInKarteLadenLinearLayout);
        kartenImportLayout.setVisibility(LinearLayout.GONE);
        externerImportLayout = (LinearLayout) view.findViewById(R.id.ExternerDatenbankLinearLayout);
        externerImportLayout.setVisibility(LinearLayout.GONE);
        dateiSuchenKoordinatensystem = (LinearLayout) view.findViewById(R.id.DateiHinzufuegenKoordinatensystemLinearLayout);
        dateiSuchenKoordinatensystem.setVisibility(LinearLayout.GONE);

        context = getActivity();

        konstanten = new Konstanten(context);

        //editTexts
        editTextProjektNummer = (EditText) view.findViewById(R.id.editTextProjektNummer);
        editTextPunktnummer = (EditText) view.findViewById(R.id.editTextPunktNummer);
        editTextRechtsWert = (EditText) view.findViewById(R.id.editTextRechtsWert);
        editTextHochWert = (EditText) view.findViewById(R.id.editTextHochWert);
        editTextHoehe = (EditText) view.findViewById(R.id.editTextHoehe);
        editTextLongitude = (EditText) view.findViewById(R.id.editTextLongitude);
        editTextLatitude = (EditText) view.findViewById(R.id.editTextLatitude);
        editTextPunktArt = (EditText) view.findViewById(R.id.editTextPunktArt);
        editTextDateiHinzufuegenNeuesProjekt = (EditText) view.findViewById(R.id.editTextDateiHinzufuegenProjektName);
        editTextDateiHinzufuegenNeuesProjekt.setVisibility(EditText.GONE);
        editTextImportUmkreis = (EditText) view.findViewById(R.id.editTextPunktUmkreisRadius);
        editTextAnmerkung = (EditText) view.findViewById(R.id.editTextAnmerkung);

        //Spinners
        spinnerDateiHinzufuegenProjekte = (Spinner) view.findViewById(R.id.spinnerDateiHinzufuegenProjekte);
        spinnerDateiHinzufuegenProjekte.setVisibility(Spinner.GONE);
        spinnerBilderProjektNummer = (Spinner) view.findViewById(R.id.spinnerBildZuPunktHinzufuegenProjekt);
        spinnerBilderPunktNummer = (Spinner) view.findViewById(R.id.spinnerBildZuPunktHinzufuegenPunktNummer);
        spinnerImportKarteProjektNummer = (Spinner) view.findViewById(R.id.spinnerImportKarteProjektNummer);
        spinnerImportKartePunktNummer = (Spinner) view.findViewById(R.id.spinnerImportKartePunktNummer);
        spinnerImportKartePunktNummer.setVisibility(Spinner.GONE);
        spinnerDateiHinzufuegenFormat = (Spinner) view.findViewById(R.id.spinnerDateiHinzufuegenFormat);
        spinnerDateiHinzufuegenKoordinatensystem = (Spinner) view.findViewById(R.id.spinnerDateiHinzufuegenSystem);


        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioButton:
                        //Datei Suchen
                        textView.setText(getString(R.string.import_info1));
                        textView.setBackgroundColor(Color.LTGRAY);
                        manuelleEingabeLayout.setVisibility(LinearLayout.GONE);
                        kartenImportLayout.setVisibility(LinearLayout.GONE);
                        externerImportLayout.setVisibility(LinearLayout.GONE);
                        dateiSuchenLayout.setVisibility(LinearLayout.VISIBLE);
                        break;
                    case R.id.radioButton2:
                        //Import in Karte
                        textView.setText(getString(R.string.import_info2));
                        textView.setBackgroundColor(Color.LTGRAY);
                        manuelleEingabeLayout.setVisibility(LinearLayout.GONE);
                        dateiSuchenLayout.setVisibility(LinearLayout.GONE);
                        externerImportLayout.setVisibility(LinearLayout.GONE);
                        kartenImportLayout.setVisibility(LinearLayout.VISIBLE);
                        spinnerImportKarteFuellen();
                        break;
                    case R.id.radioButton3:
                        //Import aus externer Datenbank
                        textView.setText(getString(R.string.import_info3));
                        textView.setBackgroundColor(Color.LTGRAY);
                        manuelleEingabeLayout.setVisibility(LinearLayout.GONE);
                        dateiSuchenLayout.setVisibility(LinearLayout.GONE);
                        kartenImportLayout.setVisibility(LinearLayout.GONE);
                        externerImportLayout.setVisibility(LinearLayout.VISIBLE);
                        break;
                    case R.id.radioButtonManuelleEingabe:
                        //Manuelle Eingabe
                        textView.setText(getString(R.string.import_info4));
                        textView.setBackgroundColor(Color.LTGRAY);
                        dateiSuchenLayout.setVisibility(LinearLayout.GONE);
                        kartenImportLayout.setVisibility(LinearLayout.GONE);
                        externerImportLayout.setVisibility(LinearLayout.GONE);
                        manuelleEingabeLayout.setVisibility(LinearLayout.VISIBLE);
                        spinnerBilderFuellen();
                        break;
                    default:
                }
            }
        });


        radioGroupDateiHinzufuegen.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButtonDateiHinzufuegenNeuesProjekt:
                        textViewDateiHinzufuegenBestehendeProjektnummern.setVisibility(TextView.GONE);
                        spinnerDateiHinzufuegenProjekte.setVisibility(Spinner.GONE);
                        textViewDateiHinzufuegenNeueProjektnummer.setVisibility(TextView.VISIBLE);
                        editTextDateiHinzufuegenNeuesProjekt.setVisibility(EditText.VISIBLE);
                        break;
                    case R.id.radioButtonDateiHinzufuegenZuProjektHinzufuegen:
                        textViewDateiHinzufuegenBestehendeProjektnummern.setVisibility(TextView.VISIBLE);
                        spinnerDateiHinzufuegenProjekte.setVisibility(Spinner.VISIBLE);
                        textViewDateiHinzufuegenNeueProjektnummer.setVisibility(TextView.GONE);
                        editTextDateiHinzufuegenNeuesProjekt.setVisibility(EditText.GONE);
                        spinnerProjekteFuellen();
                        break;
                    default:
                }
            }
        });

        /*
        Methode um Dateien auf dem Smartphone zu suchen und Punktdaten
         zu extrahieren und importieren in die Datenbank
         Button-Cklick-Event-Handler
         */
        dateiSuchenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Projektnummerangabe ueberpruefen

                Boolean gueltigeProjektNummer = false;
                String projektNummer = "";

                if (radioButtonDateiHinzufuegenNeuesProjekt.isChecked()) {
                    if ( editTextDateiHinzufuegenNeuesProjekt.getText() != null
                            && !editTextDateiHinzufuegenNeuesProjekt.getText().toString().isEmpty()
                            && !editTextDateiHinzufuegenNeuesProjekt.getText().toString().trim().isEmpty()) {
                        projektNummer = editTextDateiHinzufuegenNeuesProjekt.getText().toString().trim();
                        gueltigeProjektNummer = true;
                    }
                    else
                    {
                        Toast.makeText(context,"Bitte eine Projektnummer angeben.",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    if (radioButtonDateiHinzufuegenProjektWaehlen.isChecked()) {
                        if(spinnerDateiHinzufuegenProjekte.getSelectedItem() != null
                                && !spinnerDateiHinzufuegenProjekte.getSelectedItem().toString().isEmpty()&&
                                !spinnerDateiHinzufuegenProjekte.getSelectedItem().toString().trim().isEmpty())
                        {
                            projektNummer = spinnerDateiHinzufuegenProjekte.getSelectedItem().toString().trim();
                            gueltigeProjektNummer = true;
                        }
                        else
                        {
                            Toast.makeText(context,"Bitte eine Projektnummer angeben.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(context,"Bitte eine Projektnummer angeben.",Toast.LENGTH_SHORT).show();
                    }
                }



                if(gueltigeProjektNummer == true) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.setProjektNummerFuerDatei(projektNummer);
                    //Aufruf der Methode zum Suchen und Öffnen der Datei aus der MainActivity
                    if(spinnerDateiHinzufuegenFormat.getSelectedItem().toString().equals("PKT")) {
                        //Im PKT-Fall muss das Koordinatensystem mit uebergeben werden
                        activity.textDateiSuchen(spinnerDateiHinzufuegenFormat.getSelectedItem().toString(),
                                spinnerDateiHinzufuegenKoordinatensystem.getSelectedItem().toString());
                    }
                    else
                    {
                        //In allen anderen Faellen wird ein leerer String als Koordinatensystem uebergeben
                        //Informationen hierzu lassen sich direkt aus der Datei ziehen
                        activity.textDateiSuchen(spinnerDateiHinzufuegenFormat.getSelectedItem().toString(),
                                "");
                    }
                }
            }
        });



        //Methode um Punkten Bilder zuzuordnen
        //Button-Cklick-Event-Handler
        bilderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Datenbank bereitstellen
                MapDBDataSource dataSource = new MapDBDataSource(context);
                Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                dataSource.open();

                String projektNummer = spinnerBilderProjektNummer.getSelectedItem().toString().trim();
                if (projektNummer.equals(konstanten.getLeeresProjektString()))
                {
                    projektNummer = "";
                }
                String punktNummer = spinnerBilderPunktNummer.getSelectedItem().toString().trim();
                boolean punktNummerVorhanden = false;

                List<GeodaetischerPunkt> punkteListe = dataSource.getAllMapDB();

                //TODO: Punktnummerfindung funktioniert noch nicht richtig
                //Fehler bei gleicher PNum anderer ProjNum
                //Behoben?

                //editTextProjektNummer ueberpuefen der Punktnummer
                if(punktNummer.isEmpty())
                {
                    Toast.makeText(context,"Bitte Punktnummer eingeben!",Toast.LENGTH_SHORT).show();
                }
                else {
                    if (punkteListe.size() != 0) {
                        //Ueberpruefen ob die Punkt bereits existiert:
                        if (projektNummer.isEmpty() || projektNummer == null) {
                            //Ist keine Projektnummer vergeben, nur nach Punktnummer suchen
                            for (GeodaetischerPunkt punkt : punkteListe
                                    ) {
                                if (projektNummer.equals(punkt.getProjektNummer())||
                                        punkt.getProjektNummer()==null) {
                                    if (punktNummer.equals(punkt.getPunktNummer())) {
                                        punktNummerVorhanden = true;
                                    }
                                }
                            }
                        } else {
                            //Ist eine Projektnummer vorhanden, so muss in diesem Projekt
                            // auf eine bereits existierende Punktnummer ueberprueft werden
                            for (GeodaetischerPunkt punkt : punkteListe
                                    ) {
                                if (projektNummer.equals(punkt.getProjektNummer())) {
                                    if (punktNummer.equals(punkt.getPunktNummer())) {
                                        punktNummerVorhanden = true;
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        //keine Punkte in der Datenbank vorhanden
                        punktNummerVorhanden = false;
                    }

                    if (punktNummerVorhanden == false) {
                        Toast.makeText(context,
                                "Die Punktnummer existiert nicht in diesem Projekt (falls angegeben).",
                                Toast.LENGTH_SHORT).show();
                    } else {

                        MainActivity activity = (MainActivity) getActivity();
                        activity.setPunktNummerFuerBilder(punktNummer);
                        activity.setProjektNummerFuerBilder(projektNummer);

                        //Starten der Bildersuchfunktion aus der Mainactivity
                        activity.bilderSuche();
                    }
                }

                Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                dataSource.close();
            }
        });



        //Methode um Punkten manuell in die Datenbank einzufuegen
        //Button-Cklick-Event-Handler
        punktHinzufuegenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeodaetischerPunkt geodaetischerPunkt;

                //Datenbank bereitstellen
                MapDBDataSource dataSource = new MapDBDataSource(context);
                Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                dataSource.open();

                String projektNummer = editTextProjektNummer.getText().toString().trim();
                String punktNummer = editTextPunktnummer.getText().toString().trim();
                String rechtsWertStr = editTextRechtsWert.getText().toString().trim();
                String hochWertStr = editTextHochWert.getText().toString().trim();
                String hoeheStr = editTextHoehe.getText().toString().trim();
                String longitudeStr = editTextLongitude.getText().toString().trim();
                String latitudeStr = editTextLatitude.getText().toString().trim();
                String punktArt = editTextPunktArt.getText().toString().trim();
                String anmerkung = editTextAnmerkung.getText().toString().trim();
                double rechtsWert, hochWert, hoehe, latitude, longitude;
                boolean punktNummerVorhanden = false;
                hoehe = 0;

                List<GeodaetischerPunkt> punkteListe = dataSource.getAllMapDB();

                if(!rechtsWertStr.isEmpty() && !hochWertStr.isEmpty()
                        && !longitudeStr.isEmpty() && !latitudeStr.isEmpty() ||
                        ((!rechtsWertStr.isEmpty() && hochWertStr.isEmpty()) ||
                                (rechtsWertStr.isEmpty() && !hochWertStr.isEmpty()) ||
                                (!longitudeStr.isEmpty() && latitudeStr.isEmpty()) ||
                                (longitudeStr.isEmpty() && !latitudeStr.isEmpty()))) {

                    Toast.makeText(context,
                            "Bitte die Koordinaten in nur einem Koordinatensystem angeben.",
                            Toast.LENGTH_LONG).show();
                } else {

                    //editTextProjektNummer
                    if (punktNummer.isEmpty()) {
                        Toast.makeText(context, "Bitte Punktnummer eingeben!", Toast.LENGTH_SHORT).show();
                    } else {
                        //Ueberpruefen ob die Punkt bereits existiert:
                        if (projektNummer.isEmpty() || projektNummer == null) {
                            //Ist keine Projektnummer vergeben, nur nach Punktnummer suchen
                            for (GeodaetischerPunkt punkt : punkteListe
                                    ) {
                                if (projektNummer.equals(punkt.getProjektNummer()) ||
                                        punkt.getProjektNummer() == null) {
                                    if (punktNummer.equals(punkt.getPunktNummer())) {
                                        punktNummerVorhanden = true;
                                    }
                                }
                            }
                        } else {
                            //Ist eine Projektnummer vorhanden, so muss in diesem Projekt
                            // auf eine bereits existierende Punktnummer ueberprueft werden
                            for (GeodaetischerPunkt punkt : punkteListe
                                    ) {
                                if (projektNummer.equals(punkt.getProjektNummer())) {
                                    if (punktNummer.equals(punkt.getPunktNummer())) {
                                        punktNummerVorhanden = true;
                                    }
                                }
                            }
                        }

                        if (punktNummerVorhanden == true) {
                            Toast.makeText(context,
                                    "Die Punktnummer existiert bereits in diesem Projekt (falls angegeben).",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (hoeheStr.isEmpty()) {
                                Toast.makeText(context, "Bitte eine Höhe angeben.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    hoehe = Double.parseDouble(hoeheStr);
                                } catch (Exception e) {
                                    Log.w(LOG_TAG, "Ungueltiges Parsen:");
                                    Log.w(LOG_TAG, e.getMessage().toString());
                                    Toast.makeText(context,
                                            "Ungültiger Wert. Es konnte keine gültige Zahl ermittelt werden."
                                            , Toast.LENGTH_SHORT).show();
                                }

                                if (rechtsWertStr.isEmpty() ||
                                        hochWertStr.isEmpty()) {
                                    if (latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
                                        Toast.makeText(context,
                                                "Bitte Rechts- und Hochwert oder Längen- und Breitengrad angeben.",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Latitude-Longitude-Punkteingabe
                                        try {
                                            latitude = Double.parseDouble(latitudeStr);
                                            longitude = Double.parseDouble(longitudeStr);


                                            //geodaetischer Punkt erzeugen
                                            //zusaetzliche Gueltigkeiten ueberpruefen
                                            geodaetischerPunkt = new GeodaetischerPunkt(punktNummer,
                                                    longitude, latitude, hoehe, projektNummer, punktArt, 1, anmerkung);

                                            //Punkt in Datenbank einfuegen
                                            dataSource.insertPunktInMapDB(geodaetischerPunkt.getPunktNummer(),
                                                    geodaetischerPunkt.getRechts(), geodaetischerPunkt.getHoch(),
                                                    geodaetischerPunkt.getHoehe(),
                                                    geodaetischerPunkt.getLongitude(), geodaetischerPunkt.getLatitude(),
                                                    geodaetischerPunkt.getProjektNummer(),
                                                    geodaetischerPunkt.getPunktArt(),geodaetischerPunkt.getAnmerkung());

                                            Toast.makeText(context, "Punkt wurde hinzugefügt.",
                                                    Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.w(LOG_TAG, "Ungueltiges Parsen:");
                                            Log.w(LOG_TAG, e.getMessage().toString());
                                            Toast.makeText(context,
                                                    "Ungültige Werteingabe."
                                                    , Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                } else {
                                    //UTM-Punkteingabe
                                    try {
                                        rechtsWert = Double.parseDouble(rechtsWertStr);
                                        hochWert = Double.parseDouble(hochWertStr);

                                        //geodaetischer Punkt erzeugen
                                        //zusaetzliche Gueltigkeiten ueberpruefen
                                        geodaetischerPunkt = new GeodaetischerPunkt(punktNummer,
                                                rechtsWert, hochWert, hoehe, projektNummer, punktArt, anmerkung);

                                        //Punkt in Datenbank einfuegen
                                        dataSource.insertPunktInMapDB(geodaetischerPunkt.getPunktNummer(),
                                                geodaetischerPunkt.getRechts(), geodaetischerPunkt.getHoch(),
                                                geodaetischerPunkt.getHoehe(),
                                                geodaetischerPunkt.getLongitude(), geodaetischerPunkt.getLatitude(),
                                                geodaetischerPunkt.getProjektNummer(),
                                                geodaetischerPunkt.getPunktArt(),
                                                geodaetischerPunkt.getAnmerkung());

                                        Toast.makeText(context, "Punkt wurde hinzugefügt.",
                                                Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        Log.w(LOG_TAG, "Ungueltiges Parsen:");
                                        Log.w(LOG_TAG, e.getMessage().toString());
                                        Toast.makeText(context,
                                                "Ungültige Werteingabe."
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }


                Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                dataSource.close();
                spinnerBilderFuellen();
            }
        });




        //Methode um Punkten aus der Datenbank in die Karte zu importieren
        //Button-Cklick-Event-Handler
        importKarteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String projekt = spinnerImportKarteProjektNummer.getSelectedItem().toString();
                final List<GeodaetischerPunkt> geodaetischerPunktListe;
                boolean latlonIsNull = false;
                boolean punktExists = false;
                MainActivity activity = (MainActivity)getActivity();

                if(projekt.equals(konstanten.getLeeresProjektString()))
                {
                    //Punktweises importieren der Punkte ohne Projektnummer

                    projekt = "";

                    String punktNummer = spinnerImportKartePunktNummer.getSelectedItem().toString();

                    finalInKartenImportPunkte = new ArrayList<>();

                    //Datenbank bereitstellen
                    MapDBDataSource dataSource = new MapDBDataSource(context);
                    Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                    dataSource.open();

                    //Datenbankpunkteliste fuer den speziellen Punkt
                    geodaetischerPunktListe = dataSource.getAllMapDBforPunkt(projekt,punktNummer);

                    Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                    dataSource.close();

                    if(geodaetischerPunktListe.size()>1)
                    {
                        Toast.makeText(context,"Ein Fehler ist aufgetreten." +
                                "\nIdentische Punkte vorhanden!" +
                                "\nEs wurde nichts eingeladen.",Toast.LENGTH_LONG);
                    }
                    else {
                        //Ueberpruefen ob Punkt bereits vorhanden
                        final List<GeodaetischerPunkt> kartenPunkte = activity.getKartenPunktListe();

                        if(kartenPunkte != null && kartenPunkte.size()>0) {
                            finalInKartenImportPunkte = kartenPunkte;
                        }

                        for (GeodaetischerPunkt punkt : geodaetischerPunktListe) {
                            if (punkt.getLongitude() == 0 || punkt.getLatitude() == 0) {
                                latlonIsNull = true;
                            }
                            if(kartenPunkte != null && kartenPunkte.size()>0) {
                                for (GeodaetischerPunkt kartenPunkt : kartenPunkte) {
                                    if (kartenPunkt.getProjektNummer().equals(punkt.getProjektNummer())
                                            && kartenPunkt.getPunktNummer().equals(punkt.getPunktNummer())) {
                                        punktExists = true;
                                    }
                                }
                                if(!punktExists)
                                {
                                    finalInKartenImportPunkte.add(punkt);
                                }
                            }
                            else
                            {
                                finalInKartenImportPunkte = geodaetischerPunktListe;
                            }
                        }
                        if (!punktExists) {
                            if (latlonIsNull) {

                                //Quelle: http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android
                                //Beitrag von: Steve Haley
                                //Letzte Sichtung: 01.07.2016 8:10 Uhr
                                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //Yes button clicked

                                                MainActivity activity = (MainActivity) getActivity();
                                                activity.setKartenPunktListe(finalInKartenImportPunkte);
                                                Toast.makeText(context, "Der Punkt wurde in die Karte importiert!", Toast.LENGTH_LONG).show();
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //No button clicked
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage("Längen- oder Breitengrad = 0 festgestellt!" +
                                        "\nMöchten Sie dennoch den Punkt einladen?")
                                        .setPositiveButton("Yes", dialogClickListener)
                                        .setNegativeButton("No", dialogClickListener).show();
                            } else {
                                activity.setKartenPunktListe(geodaetischerPunktListe);
                                Toast.makeText(context, "Der Punkt wurde in die Karte importiert!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                else
                {
                    //alle Punkte des Projektes importieren

                    finalInKartenImportPunkte = new ArrayList<>();
                    //Ueberpruefen ob Punkt bereits vorhanden
                    List<GeodaetischerPunkt> kartenPunkte = activity.getKartenPunktListe();

                    if(kartenPunkte != null && kartenPunkte.size()>0) {
                        finalInKartenImportPunkte = kartenPunkte;
                    }

                    //Datenbank bereitstellen
                    MapDBDataSource dataSource = new MapDBDataSource(context);
                    Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                    dataSource.open();

                    geodaetischerPunktListe = dataSource.getAllMapDBforProject(projekt);

                    Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                    dataSource.close();

                    for (GeodaetischerPunkt punkt:geodaetischerPunktListe) {
                        if (punkt.getLongitude() == 0 || punkt.getLatitude() == 0) {
                            latlonIsNull = true;
                        }

                        if (kartenPunkte != null && kartenPunkte.size() > 0) {
                            for (GeodaetischerPunkt kartenPunkt : kartenPunkte) {
                                if (kartenPunkt.getProjektNummer().equals(punkt.getProjektNummer())
                                        && kartenPunkt.getPunktNummer().equals(punkt.getPunktNummer())) {
                                    punktExists = true;
                                }
                            }
                            if (!punktExists) {
                                finalInKartenImportPunkte.add(punkt);
                            }
                            punktExists = false;
                        }
                        else
                        {
                            finalInKartenImportPunkte = geodaetischerPunktListe;
                        }
                    }
                    if(latlonIsNull) {

                        //Quelle: http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android
                        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        MainActivity activity = (MainActivity)getActivity();
                                        activity.setKartenPunktListe(finalInKartenImportPunkte);
                                        Toast.makeText(context,"Die Punkte wurde in die Karte importiert!",Toast.LENGTH_LONG).show();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Längen- oder Breitengrad = 0 festgestellt!" +
                                "\nMöchten Sie dennoch alle Punkte einladen?")
                                .setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                    else
                    {
                        activity.setKartenPunktListe(finalInKartenImportPunkte);

                        Toast.makeText(context,"Die Punkte wurde in die Karte importiert!",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });


        /**
         * Button Click Listener
         * Methode um alle Punkte in die KArtenansicht einzuladen
         */
        importKarteAlleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alle Punkte importieren

                allePunkteInKarteEinladen(0);
            }
        });

        /*
        Button Click Listerner fuer den Import aller Punkte aus einer externen Datenbank
         */
        importExternAllePunkteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO: Internet-Permission-Abfrage einrichten
                //dafuer vorgesehene Activity starten
                Intent intent = new Intent(getActivity().getApplicationContext(),
                        AllePunkteAusExternerDatenbank.class);
                startActivity(intent);
            }
        });

        /**
         * Button Click Listener um Punkte aus dem Standortumkreis einzuladen
         */
        importKarteUmkreisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String umkeisString = editTextImportUmkreis.getText().toString().trim();

                if (umkeisString.equals(""))
                {
                    Toast.makeText(context,"Bitte ein Umkreis angeben.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    umkreis = Double.parseDouble(umkeisString);

                    //Standort bekommen
                    Intent intentLocation = new Intent(context, GetLocationActivity.class);
                    //getActivity().startActivityForResult(intentLocation, 3);
                    startActivityForResult(intentLocation, 3);

                    MainActivity activity = (MainActivity)getActivity();
                    if(activity.getKartenPunktListe() != null && activity.getKartenPunktListe().size()>0) {
                        if (umkreis > 0) {
                            Toast.makeText(context, "Punkte im Umkreis von " + umkeisString
                                    + " m wurden in die Karte eingeladen.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Umkreis auf 0 m gesetzt." +
                                    "\nAlle Punkte in die Karte eingeladen.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });


        /*
        Listener fuer den Bilder-Hinzufuegen-ProjektNummern-Spinner.
        Fuellt den Bilder-Hinzufuegen-PunktNummern-Spinner für das jeweilige Projekt.
         */
        spinnerBilderProjektNummer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try
                {
                    String projekt = spinnerBilderProjektNummer.getSelectedItem().toString().trim();

                    List<String> punktNummern = new ArrayList<>();
                    MapDBDataSource dataSource = new MapDBDataSource(context);
                    Cursor cursor;
                    dataSource.open();
                    Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");

                    try {
                        if (projekt.equals(konstanten.getLeeresProjektString()))
                        {
                            cursor = dataSource.getDatenbank().rawQuery(
                                    "SELECT punktNummer FROM MapDB WHERE projektNummer IS NULL", null);
                        }
                        else {
                            cursor = dataSource.getDatenbank().rawQuery(
                                    "SELECT punktNummer FROM MapDB WHERE projektNummer=?", new String[]
                                            {projekt});
                        }
                        if(cursor.getCount() > 0)
                        {
                            cursor.moveToFirst();
                            do {
                                punktNummern.add(cursor.getString(cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_NUMMER)));
                            }while (cursor.moveToNext());
                        }
                        else
                        {
                            Toast.makeText(context,"Kein Punkte vorhanden. Bitte ein neues Projekt auswählen.",
                                    Toast.LENGTH_LONG).show();
                        }
                        cursor.close();
                    }
                    catch(Exception e)
                    {
                        Log.e("DATENBANKERROR",e.getMessage().toString());
                    }
                    dataSource.close();
                    Log.d(LOG_TAG, "Die Datenquelle wurde geschlossen.");

                    spinnerBilderPunktNummer.setAdapter(null);

                    if (punktNummern.size() > 0) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                android.R.layout.simple_spinner_item,
                                punktNummern);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerBilderPunktNummer.setAdapter(adapter);
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


        spinnerImportKarteProjektNummer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String projekt = spinnerImportKarteProjektNummer.getSelectedItem().toString().trim();

                    if (projekt.equals(konstanten.getLeeresProjektString())) {

                        textViewImportKartePunktNummer.setVisibility(TextView.VISIBLE);
                        spinnerImportKartePunktNummer.setVisibility(Spinner.VISIBLE);

                        List<String> punktNummern = new ArrayList<>();
                        MapDBDataSource dataSource = new MapDBDataSource(context);
                        Cursor cursor;
                        dataSource.open();
                        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");

                        try {
                            if (projekt.equals(konstanten.getLeeresProjektString())) {
                                cursor = dataSource.getDatenbank().rawQuery(
                                        "SELECT punktNummer FROM MapDB WHERE projektNummer IS NULL", null);
                            } else {
                                cursor = dataSource.getDatenbank().rawQuery(
                                        "SELECT punktNummer FROM MapDB WHERE projektNummer=?", new String[]
                                                {projekt});
                            }
                            if (cursor.getCount() > 0) {
                                cursor.moveToFirst();
                                do {
                                    punktNummern.add(cursor.getString(cursor.getColumnIndex(MapDBHelper.COLUMN_PUNKT_NUMMER)));
                                } while (cursor.moveToNext());
                            } else {
                                Toast.makeText(context, "Kein Punkte vorhanden. Bitte ein neues Projekt auswählen.",
                                        Toast.LENGTH_LONG).show();
                            }
                            cursor.close();
                        } catch (Exception e) {
                            Log.e("DATENBANKERROR", e.getMessage().toString());
                        }
                        dataSource.close();
                        Log.d(LOG_TAG, "Die Datenquelle wurde geschlossen.");

                        spinnerImportKartePunktNummer.setAdapter(null);

                        if (punktNummern.size() > 0) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                    android.R.layout.simple_spinner_item,
                                    punktNummern);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerImportKartePunktNummer.setAdapter(adapter);
                        }
                    }
                    else
                    {
                        textViewImportKartePunktNummer.setVisibility(TextView.GONE);
                        spinnerImportKartePunktNummer.setVisibility(Spinner.GONE);
                    }
                }
                catch(Exception e)
                {
                    String ex;
                    ex = e.getMessage().toString()
                            + "/// " + e.getCause().toString();
                    Log.e("ERROR", ex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinnerDateiHinzufuegenFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinnerDateiHinzufuegenFormat.getSelectedItem().toString().equals("PKT"))
                {
                    dateiSuchenKoordinatensystem.setVisibility(LinearLayout.VISIBLE);
                }
                else
                {
                    dateiSuchenKoordinatensystem.setVisibility(LinearLayout.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }

    /*
    Methode um den Spinner fuer die Projekte zum Datei Hinzufuegen zu fuellen
     */
    private void spinnerProjekteFuellen()
    {
        List<String> projekte = new ArrayList<>();
        MapDBDataSource dataSource = new MapDBDataSource(context);
        dataSource.open();
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");

        try {
            Cursor cursor = dataSource.getDatenbank().rawQuery(
                    "SELECT DISTINCT(projektNummer) FROM MapDB",null);
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                do {
                    String projekt = cursor.getString(cursor.getColumnIndex(MapDBHelper.COLUMN_PROJEKT_NUMMER));
                    if(projekt != null && !projekt.equals("")) {
                        projekte.add(projekt);
                    }
                }while (cursor.moveToNext());
            }
            else
            {
                Toast.makeText(context,"Kein Projekt vorhanden. Bitte ein neues Projekt anlegen.",
                        Toast.LENGTH_LONG).show();
            }
            cursor.close();
        }
        catch(Exception e)
        {
            Log.e("DATENBANKERROR",e.getMessage().toString());
        }
        dataSource.close();
        Log.d(LOG_TAG, "Die Datenquelle wurde geschlossen.");

        if (projekte.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item,
                    projekte);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDateiHinzufuegenProjekte.setAdapter(adapter);
        }
    }

    /*
    Methode um den Spinner fuer die Projekte zum Bild-zu-Punkt-Hinzufuegen zu fuellen
     */
    private void spinnerBilderFuellen()
    {
        List<String> projekte = new ArrayList<>();
        MapDBDataSource dataSource = new MapDBDataSource(context);
        dataSource.open();
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");

        try {
            Cursor cursor = dataSource.getDatenbank().rawQuery(
                    "SELECT DISTINCT(projektNummer) FROM MapDB",null);
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                do {
                    if (cursor.getString(cursor.getColumnIndex(MapDBHelper.COLUMN_PROJEKT_NUMMER)) == null)
                    {
                        projekte.add(konstanten.getLeeresProjektString());
                    }
                    else {
                        projekte.add(cursor.getString(cursor.getColumnIndex(
                                MapDBHelper.COLUMN_PROJEKT_NUMMER)));
                    }
                }while (cursor.moveToNext());
            }
            else
            {
                Toast.makeText(context,"Kein Projekt vorhanden. Bitte ein neues Projekt anlegen.",
                        Toast.LENGTH_LONG).show();
            }
            cursor.close();
        }
        catch(Exception e)
        {
            Log.e("DATENBANKERROR",e.getMessage().toString());
        }
        dataSource.close();
        Log.d(LOG_TAG, "Die Datenquelle wurde geschlossen.");

        spinnerBilderProjektNummer.setAdapter(null);

        if (projekte.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item,
                    projekte);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBilderProjektNummer.setAdapter(adapter);
        }
    }

    /*
    Methode um den Spinner fuer die Projekte zum Import in die Karte zu fuellen
     */
    private void spinnerImportKarteFuellen()
    {
        List<String> projekte = new ArrayList<>();
        MapDBDataSource dataSource = new MapDBDataSource(context);
        dataSource.open();
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");

        try {
            Cursor cursor = dataSource.getDatenbank().rawQuery(
                    "SELECT DISTINCT(projektNummer) FROM MapDB",null);
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                do {
                    if (cursor.getString(cursor.getColumnIndex(MapDBHelper.COLUMN_PROJEKT_NUMMER)) == null)
                    {
                        projekte.add(konstanten.getLeeresProjektString());
                    }
                    else {
                        projekte.add(cursor.getString(cursor.getColumnIndex(
                                MapDBHelper.COLUMN_PROJEKT_NUMMER)));
                    }
                }while (cursor.moveToNext());
            }
            else
            {
                Toast.makeText(context,"Kein Projekt vorhanden. Bitte ein neues Projekt anlegen.",
                        Toast.LENGTH_LONG).show();
            }
            cursor.close();
        }
        catch(Exception e)
        {
            Log.e("DATENBANKERROR",e.getMessage().toString());
        }
        dataSource.close();
        Log.d(LOG_TAG, "Die Datenquelle wurde geschlossen.");

        spinnerImportKarteProjektNummer.setAdapter(null);

        if (projekte.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item,
                    projekte);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerImportKarteProjektNummer.setAdapter(adapter);
        }
    }

    /**
     * Handler fuer das Anfrageergebnis eines Activity-Aufrufs mit startActivityForResult
     * @param requestCode Integer-Wert des Anfragecodes
     * @param resultCode  Integer-Wert des Ergebniscodes
     * @param data  Uebergebene Intent-Daten
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            //Standort von der GetLocationActivity Klasse bekommen

            //Laengen-,Breitenstrings in Bundle aus den Intent-Daten
            Bundle b = data.getExtras();
            double activityLatitude = b.getDouble("latitude");
            double activityLongitude = b.getDouble("longitude");
            Location activitiyStandort = new Location("");
            activitiyStandort.setLatitude(activityLatitude);
            activitiyStandort.setLongitude(activityLongitude);

            //Variable den Standort uebergeben
            standortFragment = activitiyStandort;

            //Aufruf der Methode zum Import der Punkte in die Kartenansicht
            allePunkteInKarteEinladen(umkreis);
        }
    }


    /**
     * Methode um alle Punkte in die Karte einzuladen.
     * Wird ein Radius > 0 angegeben, so werden nur Punkte in diesem Radius
     * um den aktuellen Standort in die Karte eingeladen/in der Karte angezeigt.
     * @param radius (in Metern)
     */
    private void allePunkteInKarteEinladen(final double radius) {
        final List<GeodaetischerPunkt> geodaetischerPunktListe;
        umkreisPunktListe = new ArrayList<>();
        Location standort = new Location("");
        finalInKartenImportPunkte = new ArrayList<>();
        boolean latlonIsNull = false;
        boolean punktExists = false;
        MainActivity activity = (MainActivity) getActivity();

        //Bei Umkreisimport
        if (radius > 0) {
            //Standort zuweisen
            standort = getStandortFragment();

            //Punkte, die bereits in die Karte eingeladen wurden, entfernen
            activity.setKartenPunktListe(null);
        }

        //Ueberpruefen ob Punkt bereits vorhanden
        List<GeodaetischerPunkt> kartenPunkte = activity.getKartenPunktListe();

        if (kartenPunkte != null && kartenPunkte.size() > 0) {
            finalInKartenImportPunkte = kartenPunkte;
        }

        //Datenbank bereitstellen
        MapDBDataSource dataSource = new MapDBDataSource(context);
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        geodaetischerPunktListe = dataSource.getAllMapDB();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();

        for (GeodaetischerPunkt punkt : geodaetischerPunktListe) {
            if (punkt.getLongitude() == 0 || punkt.getLatitude() == 0) {
                latlonIsNull = true;
            }

            if (radius > 0) {
                if (standort != null) {
                    //Punkte im Standortumkreis ermitteln
                    Location punktLocation = new Location("");
                    punktLocation.setLongitude(punkt.getLongitude());
                    punktLocation.setLatitude(punkt.getLatitude());

                    double entfernung = standort.distanceTo(punktLocation);

                    if (entfernung <= radius) {
                        umkreisPunktListe.add(punkt);
                    }
                }
            } else {
                //Alle Punkte einladen
                //Dabei ueberpruefen ob bereits in der Karte vorhanden
                if (kartenPunkte != null && kartenPunkte.size() > 0) {
                    for (GeodaetischerPunkt kartenPunkt : kartenPunkte) {
                        if (kartenPunkt.getProjektNummer().equals(punkt.getProjektNummer())
                                && kartenPunkt.getPunktNummer().equals(punkt.getPunktNummer())) {
                            punktExists = true;
                        }
                    }
                    if (!punktExists) {
                        finalInKartenImportPunkte.add(punkt);
                    }
                    punktExists = false;
                } else {
                    finalInKartenImportPunkte = geodaetischerPunktListe;
                }
            }
        }
        if (finalInKartenImportPunkte.size() > 0 || umkreisPunktListe.size() > 0) {
            if (latlonIsNull) {

                //Quelle: http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android
                //Beitrag von: Steve Haley
                //Letzte Sichtung: 01.07.2016 8:10 Uhr
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                MainActivity activity = (MainActivity) getActivity();
                                if (radius > 0) {
                                    activity.setKartenPunktListe(umkreisPunktListe);
                                } else {
                                    activity.setKartenPunktListe(finalInKartenImportPunkte);
                                }
                                Toast.makeText(context, "Die Punkte wurde in die Karte importiert!", Toast.LENGTH_LONG).show();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Längen- oder Breitengrad = 0 festgestellt!" +
                        "\nMöchten Sie dennoch alle Punkte einladen?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            } else {
                if (radius > 0) {
                    activity.setKartenPunktListe(umkreisPunktListe);
                } else {
                    activity.setKartenPunktListe(finalInKartenImportPunkte);
                }

                Toast.makeText(context, "Die Punkte wurde in die Karte importiert!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Es wurden keine Punkte in die Karte importiert!", Toast.LENGTH_LONG).show();
        }
    }


}
