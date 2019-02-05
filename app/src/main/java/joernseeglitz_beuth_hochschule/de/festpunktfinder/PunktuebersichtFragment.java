package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

//TODO: Sortierungsabfrage
//TODO: Filter (Projektnummer,...)
//TODO: Datenbankeintragbearbeitungsfunktion

public class PunktuebersichtFragment  extends Fragment {
    private static final String LOG_TAG = PunktuebersichtFragment.class.getSimpleName();
    private MapDBDataSource dataSource;
    View view;
    LinearLayout linearLayoutOptionen;
    CheckBox checkBoxOptionen;
    Button buttonOptionenAllePunkteAnzeigen, getButtonOptionenAuswahlPunkteAnzeigen;
    Button buttonOptionenKartenPunkte;
    Spinner spinnerProjekte;
    TableLayout rowsTable;
    Context context;
    Konstanten konstanten;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.punktuebersichtfragment, container, false);
        context = getActivity();
        konstanten = new Konstanten(context);
        rowsTable = (TableLayout) view.findViewById(R.id.tabellePUeber);
        linearLayoutOptionen = (LinearLayout) view.findViewById(R.id.punktUeberLinearLayoutOptionen);
        linearLayoutOptionen.setVisibility(LinearLayout.GONE);
        checkBoxOptionen = (CheckBox) view.findViewById(R.id.checkBoxPunktUeberOptionen);
        buttonOptionenAllePunkteAnzeigen = (Button) view.findViewById(R.id.buttonPunktUeberOptionAlleAnzeigen);
        getButtonOptionenAuswahlPunkteAnzeigen = (Button) view.findViewById(R.id.buttonPunktUeberOptionAuswahlAnzeigen);
        buttonOptionenKartenPunkte = (Button) view.findViewById(R.id.buttonPunktUeberOptionKartenPunkte);
        spinnerProjekte = (Spinner) view.findViewById(R.id.spinnerPunktUeberProjekte);

        //Spinner mit Projekten aus der Datenbank fuellen
        spinnerOptionenAlleProjekteFuellen();

        dataSource = new MapDBDataSource(getActivity());

        final MainActivity activity = (MainActivity) getActivity();

        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        if(activity.getSelectedMarkerPunkt() != null)
        {
            //Ist ein Marker in der Karte selektiert, soll nur dieser Punkt in der Punktueberscht
            //angezeigt werden
            punktAusKarteInTabelleLaden(activity.getSelectedMarkerPunkt().getPunktNummer(),
                    activity.getSelectedMarkerPunkt().getProjektNummer());
        }
        else {
            //Ist kein Marker in der Karte selektiert sollen beim initiellen Aufruf
            //alle Punkte in der Tabelle dargestellt werden.
            allePunkteInTabelleLaden();
        }

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();

        /*
        CheckBox-Listener
        Macht das Layout der Auswahloptionen sichtbar oder laesst es wieder verschwinden
         */
        checkBoxOptionen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(checkBoxOptionen.isChecked())
                {
                    linearLayoutOptionen.setVisibility(LinearLayout.VISIBLE);
                }
                else
                {
                    linearLayoutOptionen.setVisibility(LinearLayout.GONE);
                }
            }
        });


        /*
        Button-Click-Listener fuer das Anzeigen aller, in der Datenbank, vorhandenen Punkte
         */
        buttonOptionenAllePunkteAnzeigen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowsTable.removeAllViews();
                Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                dataSource.open();
                //GeodaetischerPunkt Eintraege auflisten
                allePunkteInTabelleLaden();
                Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                dataSource.close();
            }
        });

        /*
        Button-Click-Listener fuer das Anzeigen aller, in der Datenbank, vorhandenen Punkte
        die zu einem bestimmten oder zu keinem Projekt gehören
         */
        getButtonOptionenAuswahlPunkteAnzeigen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowsTable.removeAllViews();
                Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                dataSource.open();
                //GeodaetischerPunkt Eintraege auflisten entsprechend der Projektwahl
                if(spinnerProjekte.getSelectedItem() != null) {
                    if(!spinnerProjekte.getSelectedItem().toString().trim()
                            .equals(konstanten.getLeeresProjektString()))
                    {
                        allePunkteInTabelleLadenFuerEinProjekt(
                                spinnerProjekte.getSelectedItem().toString().trim());
                    }
                    else
                    {
                        allePunkteInTabelleLadenFuerEinProjekt("");
                    }

                }
                else
                {
                    Toast.makeText(context,"Es konnte kein Projekt gefunden werden!",Toast.LENGTH_SHORT).show();
                }

                Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                dataSource.close();
            }
        });

        /**
         * Button-Click-Listener fuer den Kartenansichtpunkte-anzeigen-Button
         * Zeigt alle Punkte, die in die Kartenansicht eingeladen wurden
         * in der Tabelle an.
         */
        buttonOptionenKartenPunkte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                dataSource.open();

                if(activity.getKartenPunktListe() != null && activity.getKartenPunktListe().size()>0) {
                    //Bisherige Tabelle leeren
                    rowsTable.removeAllViews();

                    //Kartenansichtpunkte aus der MainActivity holen
                    //und in der Methode zum Anzeigen in der Tabelle uebergeben
                    allePunkteInTabelleLadenFuerPunkteListe(activity.getKartenPunktListe());
                }
                else
                {
                    Toast.makeText(context,"Es wurden noch keine Punkte in die Kartenansicht geladen!"
                            ,Toast.LENGTH_LONG).show();
                }

                Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                dataSource.close();
            }
        });

        return view;
    }

    private Object isNull(Object obj)
    {
        if(obj == null)
        {
            return "";
        }
        else
        {
            return obj;
        }
    }


    //Quelle: http://www.programmierenlernenhq.de/daten-in-sqlite-datenbank-schreiben-und-lesen-in-android/
    private void allePunkteInTabelleLaden()
    {
        List<GeodaetischerPunkt> geodaetischerPunktListe = dataSource.getAllMapDB();

        if (geodaetischerPunktListe.size()>0) {

            //Tablle fuellen:
            //Quelle: http://stackoverflow.com/questions/13391810/display-table-with-sqllite-database-values-in-android

            int rows;
            //Indexoberschranke wegen Tabellenkopfzeile hochgesetzt
            for (rows = 0; rows < geodaetischerPunktListe.size() + 1; rows++) {
                TableRow trTwo = new TableRow(getActivity());
                trTwo.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                //Liste der jeweiligen Datenbankeintraege in einer Zeile
                List<Object> mapDBElemente = new ArrayList<Object>();
                if (rows == 0) {
                    mapDBElemente.add(" ProjektNummer ");
                    mapDBElemente.add(" PunktNummer ");
                    mapDBElemente.add(" Rechts ");
                    mapDBElemente.add(" Hoch ");
                    mapDBElemente.add(" Hoehe ");
                    mapDBElemente.add(" Longitude ");
                    mapDBElemente.add(" Latitude ");
                    mapDBElemente.add(" PunktArt ");
                    mapDBElemente.add(" Anmerkung ");
                    mapDBElemente.add(" BildListe ");
                } else {
                    //Index -1 da der erste Index fuer den Tabellenkopf verwendet wird
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getProjektNummer()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktNummer()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getRechts()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoch()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoehe()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLongitude()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLatitude()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktArt()));
                    //Anmerkung alle 50 Zeichen -> Zeilenumbruch zur besseren Darstellbarkeit
                    String anmerkung = geodaetischerPunktListe.get(rows - 1).getAnmerkung();
                    if(anmerkung != null && !anmerkung.equals("")) {
                        StringBuffer tmp_anmerkung = new StringBuffer(anmerkung);
                        int trenn = 50;
                        if (anmerkung.length() >= trenn) {
                            trenn++;
                            for (int i = 0; i <= (anmerkung.length() / (trenn - 1)); i++) {
                                tmp_anmerkung.insert(i * trenn, '\n');
                            }
                            anmerkung = tmp_anmerkung.toString();
                        }
                    }
                    mapDBElemente.add(isNull(anmerkung));
                }

                for (int coloumn = 0; coloumn < mapDBElemente.size(); coloumn++) {
                    //Bildliste wird anders bearbeitet
                    //Texte in Textview einfuegen und in die Tabellenspalte einfuegen
                    TextView txtTwo = new TextView(getActivity());
                    txtTwo.setText(mapDBElemente.get(coloumn).toString());
//                txtTwo.setLayoutParams(new LayoutParams(
//                        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//                txtTwo.setTextColor(Color.BLACK);
                    txtTwo.setGravity(Gravity.CENTER);
//                txtTwo.setBackgroundResource(R.drawable.table_border_new);
                    //Tabellenkopfeinstellungen:
                    if (rows == 0) {
                        txtTwo.setTextColor(Color.BLACK);
                        txtTwo.setBackgroundColor(Color.LTGRAY);
                    } else {
                        //Unterschiedlicher hintergrund nach gerade oder ungerader Index
                        if(coloumn%2 == 0) {
                            txtTwo.setBackgroundColor(Color.DKGRAY);
                        }
                        else
                        {
                            txtTwo.setBackgroundColor(Color.GRAY);
                        }
                    }
                    txtTwo.setPadding(3, 3, 3, 3);
                    trTwo.addView(txtTwo);// Binding Text To Row
                }

                //Bilder in die Tabelle einfuegen
                if (rows != 0) {
                    String proj = geodaetischerPunktListe.get(rows - 1).getProjektNummer();
                    if (proj == null) {
                        proj = "";
                    }
                    String punktNum = geodaetischerPunktListe.get(rows - 1).getPunktNummer();

                    Cursor cursorPunktID;
                    if (proj.equals("")) {

                        cursorPunktID = dataSource.getDatenbank().rawQuery("SELECT ID FROM MapDB WHERE punktNummer =? AND " +
                                        "(projektNummer IS NULL OR projektNummer = '')",
                                new String[]{punktNum});
                    } else {
                        cursorPunktID = dataSource.getDatenbank().rawQuery(
                                "SELECT ID FROM MapDB WHERE punktNummer =? AND projektNummer =?",
                                new String[]{punktNum, proj});
                    }
                    cursorPunktID.moveToFirst();
                    int pIDIndex = cursorPunktID.getColumnIndex(MapDBHelper.COLUMN_ID);
                    int pID = cursorPunktID.getInt(pIDIndex);

                    //Holt alle Eintraege mit der bestimmten Punkt-ID
                    Cursor cursor = dataSource.getDatenbank().query(
                            MapDBHelper.TABLE_BILDER, dataSource.getColumns_bilderDB(),
                            MapDBHelper.COLUMN_PUNKT_ID + "=?",
                            new String[]{String.valueOf(pID)}, null, null, null);
                    cursor.moveToFirst();
                    List<Bitmap> sqlBilder = new ArrayList<Bitmap>();
                    MainActivity activity = (MainActivity) getActivity();
                    //Liste der Bilder aus der Datenbank zusammentragen
                    while (!cursor.isAfterLast()) {
                        int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);
                        byte[] bild = cursor.getBlob(idBild);
                        sqlBilder.add(
                                BitmapFactory.decodeByteArray(bild, 0, bild.length));
                        cursor.moveToNext();
                    }
                    cursor.close();

                    for (Bitmap bild : sqlBilder) {
                        final ImageView imView = new ImageView(getActivity());
                        imView.setImageBitmap(activity.scaleBitmapThumbnail(bild));
                        final ImageView imViewDBGroesse = new ImageView(getActivity());
                        imViewDBGroesse.setImageBitmap(bild);

                        imView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadPhoto(imViewDBGroesse,
                                        konstanten.getBilderMaxWidth(),
                                        konstanten.getBilderMaxHeight());

                            }
                        });

                        trTwo.addView(imView);
                    }
                }
                rowsTable.addView(trTwo, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
            }
        }
        else
        {
            Toast.makeText(context,"Es sind noch keine Punkte importiert worden.",Toast.LENGTH_SHORT).show();
        }
    }

    //Quelle: http://www.programmierenlernenhq.de/daten-in-sqlite-datenbank-schreiben-und-lesen-in-android/
    private void allePunkteInTabelleLadenFuerEinProjekt(String projektNummer)
    {
        List<GeodaetischerPunkt> geodaetischerPunktListe = dataSource.getAllMapDBforProject(projektNummer);

        //Tablle fuellen:
        //Quelle: http://stackoverflow.com/questions/13391810/display-table-with-sqllite-database-values-in-android

        int rows;
        //Indexoberschranke wegen Tabellenkopfzeile hochgesetzt
        for (rows=0; rows< geodaetischerPunktListe.size()+1; rows++) {
            TableRow trTwo = new TableRow(getActivity());
            trTwo.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            //Liste der jeweiligen Datenbankeintraege in einer Zeile
            List<Object> mapDBElemente = new ArrayList<Object>();
            if (rows == 0) {
                mapDBElemente.add(" ProjektNummer ");
                mapDBElemente.add(" PunktNummer ");
                mapDBElemente.add(" Rechts ");
                mapDBElemente.add(" Hoch ");
                mapDBElemente.add(" Hoehe ");
                mapDBElemente.add(" Longitude ");
                mapDBElemente.add(" Latitude ");
                mapDBElemente.add(" PunktArt ");
                mapDBElemente.add(" Anmerkung ");
                mapDBElemente.add(" BildListe ");
            } else {
                //Index -1 da der erste Index fuer den Tabellenkopf verwendet wird
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getProjektNummer()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktNummer()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getRechts()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoch()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoehe()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLongitude()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLatitude()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktArt()));
                //Anmerkung alle 50 Zeichen -> Zeilenumbruch zur besseren Darstellbarkeit
                String anmerkung = geodaetischerPunktListe.get(rows - 1).getAnmerkung();
                if(anmerkung != null && !anmerkung.equals("")) {
                    StringBuffer tmp_anmerkung = new StringBuffer(anmerkung);
                    int trenn = 50;
                    if (anmerkung.length() >= trenn) {
                        trenn++;
                        for (int i = 0; i <= (anmerkung.length() / (trenn - 1)); i++) {
                            tmp_anmerkung.insert(i * trenn, '\n');
                        }
                        anmerkung = tmp_anmerkung.toString();
                    }
                }
                mapDBElemente.add(isNull(anmerkung));
            }

            for (int coloumn = 0; coloumn < mapDBElemente.size(); coloumn++) {
                //Bildliste wird anders bearbeitet
                //Texte in Textview einfuegen und in die Tabellenspalte einfuegen
                TextView txtTwo = new TextView(getActivity());
                txtTwo.setText(mapDBElemente.get(coloumn).toString());
//                txtTwo.setLayoutParams(new LayoutParams(
//                        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//                txtTwo.setTextColor(Color.BLACK);
                txtTwo.setGravity(Gravity.CENTER);
//                txtTwo.setBackgroundResource(R.drawable.table_border_new);
                //Tabellenkopfeinstellungen:
                if (rows == 0) {
                    txtTwo.setTextColor(Color.BLACK);
                    txtTwo.setBackgroundColor(Color.LTGRAY);
                } else {
                    //Unterschiedlicher hintergrund nach gerade oder ungerader Index
                    if(coloumn%2 == 0) {
                        txtTwo.setBackgroundColor(Color.DKGRAY);
                    }
                    else
                    {
                        txtTwo.setBackgroundColor(Color.GRAY);
                    }
                }
                txtTwo.setPadding(3, 3, 3, 3);
                trTwo.addView(txtTwo);// Binding Text To Row
            }

            //Bilder in die Tabelle einfuegen
            if (rows != 0) {
                String proj = geodaetischerPunktListe.get(rows - 1).getProjektNummer();
                if (proj == null)
                {
                    proj = "";
                }
                String punktNum = geodaetischerPunktListe.get(rows - 1).getPunktNummer();

                Cursor cursorPunktID;
                if (proj.equals("")) {

                    cursorPunktID = dataSource.getDatenbank().rawQuery("SELECT ID FROM MapDB WHERE punktNummer =? AND " +
                                    "(projektNummer IS NULL OR projektNummer = '')",
                            new String[]{punktNum});
                } else {
                    cursorPunktID = dataSource.getDatenbank().rawQuery(
                            "SELECT ID FROM MapDB WHERE punktNummer =? AND projektNummer =?",
                            new String[]{punktNum, proj});
                }
                cursorPunktID.moveToFirst();
                int pIDIndex = cursorPunktID.getColumnIndex(MapDBHelper.COLUMN_ID);
                int pID = cursorPunktID.getInt(pIDIndex);

                //Holt alle Eintraege mit der bestimmten Punkt-ID
                Cursor cursor = dataSource.getDatenbank().query(
                        MapDBHelper.TABLE_BILDER, dataSource.getColumns_bilderDB(),
                        MapDBHelper.COLUMN_PUNKT_ID + "=?",
                        new String[]{String.valueOf(pID)}, null, null, null);
                cursor.moveToFirst();
                List<Bitmap> sqlBilder = new ArrayList<Bitmap>();
                MainActivity activity = (MainActivity)getActivity();
                //Liste der Bilder aus der Datenbank zusammentragen
                while (!cursor.isAfterLast()) {
                    int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);
                    byte[] bild = cursor.getBlob(idBild);
                    sqlBilder.add(
                            BitmapFactory.decodeByteArray(bild, 0, bild.length));
                    cursor.moveToNext();
                }
                cursor.close();

                for (Bitmap bild : sqlBilder) {
                    final ImageView imView = new ImageView(getActivity());
                    imView.setImageBitmap(activity.scaleBitmapThumbnail(bild));
                    final ImageView imViewDBGroesse = new ImageView(getActivity());
                    imViewDBGroesse.setImageBitmap(bild);

                    imView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadPhoto(imViewDBGroesse,
                                    konstanten.getBilderMaxWidth(),
                                    konstanten.getBilderMaxHeight());

                        }
                    });

                    trTwo.addView(imView);
                }
            }
            rowsTable.addView(trTwo, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    //Quelle: http://www.programmierenlernenhq.de/daten-in-sqlite-datenbank-schreiben-und-lesen-in-android/
    public void punktAusKarteInTabelleLaden(String punktNummer, String projektNummer)
    {
        MapDBDataSource dataSource = new MapDBDataSource(getActivity());
        dataSource.open();
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");

        List<GeodaetischerPunkt> geodaetischerPunktListe = dataSource.getAllMapDBforPunkt(projektNummer,punktNummer);


        //Tablle fuellen:
        //Quelle: http://stackoverflow.com/questions/13391810/display-table-with-sqllite-database-values-in-android

        int rows;
        //Indexoberschranke wegen Tabellenkopfzeile hochgesetzt
        for (rows=0; rows< geodaetischerPunktListe.size()+1; rows++) {
            TableRow trTwo = new TableRow(getActivity());
            trTwo.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            //Liste der jeweiligen Datenbankeintraege in einer Zeile
            List<Object> mapDBElemente = new ArrayList<Object>();
            if (rows == 0) {
                mapDBElemente.add(" ProjektNummer ");
                mapDBElemente.add(" PunktNummer ");
                mapDBElemente.add(" Rechts ");
                mapDBElemente.add(" Hoch ");
                mapDBElemente.add(" Hoehe ");
                mapDBElemente.add(" Longitude ");
                mapDBElemente.add(" Latitude ");
                mapDBElemente.add(" PunktArt ");
                mapDBElemente.add(" Anmerkung ");
                mapDBElemente.add(" BildListe ");
            } else {
                //Index -1 da der erste Index fuer den Tabellenkopf verwendet wird
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getProjektNummer()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktNummer()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getRechts()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoch()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoehe()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLongitude()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLatitude()));
                mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktArt()));
                //Anmerkung alle 50 Zeichen -> Zeilenumbruch zur besseren Darstellbarkeit
                String anmerkung = geodaetischerPunktListe.get(rows - 1).getAnmerkung();
                if(anmerkung != null && !anmerkung.equals("")) {
                    StringBuffer tmp_anmerkung = new StringBuffer(anmerkung);
                    int trenn = 50;
                    if (anmerkung.length() >= trenn) {
                        trenn++;
                        for (int i = 0; i <= (anmerkung.length() / (trenn - 1)); i++) {
                            tmp_anmerkung.insert(i * trenn, '\n');
                        }
                        anmerkung = tmp_anmerkung.toString();
                    }
                }
                mapDBElemente.add(isNull(anmerkung));
            }

            for (int coloumn = 0; coloumn < mapDBElemente.size(); coloumn++) {
                //Bildliste wird anders bearbeitet
                //Texte in Textview einfuegen und in die Tabellenspalte einfuegen
                TextView txtTwo = new TextView(getActivity());
                txtTwo.setText(mapDBElemente.get(coloumn).toString());
//                txtTwo.setLayoutParams(new LayoutParams(
//                        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//                txtTwo.setTextColor(Color.BLACK);
                txtTwo.setGravity(Gravity.CENTER);
//                txtTwo.setBackgroundResource(R.drawable.table_border_new);
                //Tabellenkopfeinstellungen:
                if (rows == 0) {
                    txtTwo.setTextColor(Color.BLACK);
                    txtTwo.setBackgroundColor(Color.LTGRAY);
                } else {
                    //Unterschiedlicher hintergrund nach gerade oder ungerader Index
                    if(coloumn%2 == 0) {
                        txtTwo.setBackgroundColor(Color.DKGRAY);
                    }
                    else
                    {
                        txtTwo.setBackgroundColor(Color.GRAY);
                    }
                }
                txtTwo.setPadding(3, 3, 3, 3);
                trTwo.addView(txtTwo);// Binding Text To Row
            }

            //Bilder in die Tabelle einfuegen
            if (rows != 0) {
                String proj = geodaetischerPunktListe.get(rows - 1).getProjektNummer();
                if (proj == null)
                {
                    proj = "";
                }
                String punktNum = geodaetischerPunktListe.get(rows - 1).getPunktNummer();

                Cursor cursorPunktID;
                if (proj.equals("")) {

                    cursorPunktID = dataSource.getDatenbank().rawQuery("SELECT ID FROM MapDB WHERE punktNummer =? AND " +
                                    "(projektNummer IS NULL OR projektNummer = '')",
                            new String[]{punktNum});
                } else {
                    cursorPunktID = dataSource.getDatenbank().rawQuery(
                            "SELECT ID FROM MapDB WHERE punktNummer =? AND projektNummer =?",
                            new String[]{punktNum, proj});
                }
                cursorPunktID.moveToFirst();
                int pIDIndex = cursorPunktID.getColumnIndex(MapDBHelper.COLUMN_ID);
                int pID = cursorPunktID.getInt(pIDIndex);

                //Holt alle Eintraege mit der bestimmten Punkt-ID
                Cursor cursor = dataSource.getDatenbank().query(
                        MapDBHelper.TABLE_BILDER, dataSource.getColumns_bilderDB(),
                        MapDBHelper.COLUMN_PUNKT_ID + "=?",
                        new String[]{String.valueOf(pID)}, null, null, null);
                cursor.moveToFirst();
                List<Bitmap> sqlBilder = new ArrayList<Bitmap>();
                MainActivity activity = (MainActivity)getActivity();
                //Liste der Bilder aus der Datenbank zusammentragen
                while (!cursor.isAfterLast()) {
                    int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);
                    byte[] bild = cursor.getBlob(idBild);
                    sqlBilder.add(
                            BitmapFactory.decodeByteArray(bild, 0, bild.length));
                    cursor.moveToNext();
                }
                cursor.close();

                for (Bitmap bild : sqlBilder) {
                    final ImageView imView = new ImageView(getActivity());
                    imView.setImageBitmap(activity.scaleBitmapThumbnail(bild));
                    final ImageView imViewDBGroesse = new ImageView(getActivity());
                    imViewDBGroesse.setImageBitmap(bild);

                    imView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadPhoto(imViewDBGroesse,
                                    konstanten.getBilderMaxWidth(),
                                    konstanten.getBilderMaxHeight());

                        }
                    });

                    trTwo.addView(imView);
                }
            }
            rowsTable.addView(trTwo, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }

        dataSource.close();
        Log.d(LOG_TAG, "Die Datenquelle wurde geschlossen.");
    }

    //Quelle: http://www.programmierenlernenhq.de/daten-in-sqlite-datenbank-schreiben-und-lesen-in-android/
    private void allePunkteInTabelleLadenFuerPunkteListe(List<GeodaetischerPunkt> punkteListe)
    {
        List<GeodaetischerPunkt> geodaetischerPunktListe = punkteListe;
        if (geodaetischerPunktListe.size()>0) {

            //Tablle fuellen:
            //Quelle: http://stackoverflow.com/questions/13391810/display-table-with-sqllite-database-values-in-android

            int rows;
            //Indexoberschranke wegen Tabellenkopfzeile hochgesetzt
            for (rows = 0; rows < geodaetischerPunktListe.size() + 1; rows++) {
                TableRow trTwo = new TableRow(getActivity());
                trTwo.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                //Liste der jeweiligen Datenbankeintraege in einer Zeile
                List<Object> mapDBElemente = new ArrayList<Object>();
                if (rows == 0) {
                    mapDBElemente.add(" ProjektNummer ");
                    mapDBElemente.add(" PunktNummer ");
                    mapDBElemente.add(" Rechts ");
                    mapDBElemente.add(" Hoch ");
                    mapDBElemente.add(" Hoehe ");
                    mapDBElemente.add(" Longitude ");
                    mapDBElemente.add(" Latitude ");
                    mapDBElemente.add(" PunktArt ");
                    mapDBElemente.add(" Anmerkung ");
                    mapDBElemente.add(" BildListe ");
                } else {
                    //Index -1 da der erste Index fuer den Tabellenkopf verwendet wird
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getProjektNummer()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktNummer()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getRechts()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoch()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getHoehe()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLongitude()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getLatitude()));
                    mapDBElemente.add(isNull(geodaetischerPunktListe.get(rows - 1).getPunktArt()));
                    //Anmerkung alle 50 Zeichen -> Zeilenumbruch zur besseren Darstellbarkeit
                    String anmerkung = geodaetischerPunktListe.get(rows - 1).getAnmerkung();
                    if(anmerkung != null && !anmerkung.equals("")) {
                        StringBuffer tmp_anmerkung = new StringBuffer(anmerkung);
                        int trenn = 50;
                        if (anmerkung.length() >= trenn) {
                            trenn++;
                            for (int i = 0; i <= (anmerkung.length() / (trenn - 1)); i++) {
                                tmp_anmerkung.insert(i * trenn, '\n');
                            }
                            anmerkung = tmp_anmerkung.toString();
                        }
                    }
                    mapDBElemente.add(isNull(anmerkung));
                }

                for (int coloumn = 0; coloumn < mapDBElemente.size(); coloumn++) {
                    //Bildliste wird anders bearbeitet
                    //Texte in Textview einfuegen und in die Tabellenspalte einfuegen
                    TextView txtTwo = new TextView(getActivity());
                    txtTwo.setText(mapDBElemente.get(coloumn).toString());
//                txtTwo.setLayoutParams(new LayoutParams(
//                        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//                txtTwo.setTextColor(Color.BLACK);
                    txtTwo.setGravity(Gravity.CENTER);
//                txtTwo.setBackgroundResource(R.drawable.table_border_new);
                    //Tabellenkopfeinstellungen:
                    if (rows == 0) {
                        txtTwo.setTextColor(Color.BLACK);
                        txtTwo.setBackgroundColor(Color.LTGRAY);
                    } else {
                        //Unterschiedlicher hintergrund nach gerade oder ungerader Index
                        if(coloumn%2 == 0) {
                            txtTwo.setBackgroundColor(Color.DKGRAY);
                        }
                        else
                        {
                            txtTwo.setBackgroundColor(Color.GRAY);
                        }
                    }
                    txtTwo.setPadding(3, 3, 3, 3);
                    trTwo.addView(txtTwo);// Binding Text To Row
                }

                //Bilder in die Tabelle einfuegen
                if (rows != 0) {
                    String proj = geodaetischerPunktListe.get(rows - 1).getProjektNummer();
                    if (proj == null) {
                        proj = "";
                    }
                    String punktNum = geodaetischerPunktListe.get(rows - 1).getPunktNummer();

                    Cursor cursorPunktID;
                    if (proj.equals("")) {

                        cursorPunktID = dataSource.getDatenbank().rawQuery("SELECT ID FROM MapDB WHERE punktNummer =? AND " +
                                        "(projektNummer IS NULL OR projektNummer = '')",
                                new String[]{punktNum});
                    } else {
                        cursorPunktID = dataSource.getDatenbank().rawQuery(
                                "SELECT ID FROM MapDB WHERE punktNummer =? AND projektNummer =?",
                                new String[]{punktNum, proj});
                    }
                    cursorPunktID.moveToFirst();
                    int pIDIndex = cursorPunktID.getColumnIndex(MapDBHelper.COLUMN_ID);
                    int pID = cursorPunktID.getInt(pIDIndex);

                    //Holt alle Eintraege mit der bestimmten Punkt-ID
                    Cursor cursor = dataSource.getDatenbank().query(
                            MapDBHelper.TABLE_BILDER, dataSource.getColumns_bilderDB(),
                            MapDBHelper.COLUMN_PUNKT_ID + "=?",
                            new String[]{String.valueOf(pID)}, null, null, null);
                    cursor.moveToFirst();
                    List<Bitmap> sqlBilder = new ArrayList<Bitmap>();
                    MainActivity activity = (MainActivity) getActivity();
                    //Liste der Bilder aus der Datenbank zusammentragen
                    while (!cursor.isAfterLast()) {
                        int idBild = cursor.getColumnIndex(MapDBHelper.COLUMN_BILD);
                        byte[] bild = cursor.getBlob(idBild);
                        sqlBilder.add(
                                BitmapFactory.decodeByteArray(bild, 0, bild.length));
                        cursor.moveToNext();
                    }
                    cursor.close();

                    for (Bitmap bild : sqlBilder) {
                        final ImageView imView = new ImageView(getActivity());
                        imView.setImageBitmap(activity.scaleBitmapThumbnail(bild));
                        final ImageView imViewDBGroesse = new ImageView(getActivity());
                        imViewDBGroesse.setImageBitmap(bild);

                        imView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadPhoto(imViewDBGroesse,
                                        konstanten.getBilderMaxWidth(),
                                        konstanten.getBilderMaxHeight());

                            }
                        });

                        trTwo.addView(imView);
                    }
                }
                rowsTable.addView(trTwo, new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.WRAP_CONTENT,
                        TableLayout.LayoutParams.WRAP_CONTENT));
            }
        }
        else
        {
            Toast.makeText(context,"Es sind noch keine Punkte importiert worden.",Toast.LENGTH_SHORT).show();
        }
    }


    /*
    Methode um den Spinner fuer die Projekte, die in der Punktuebersicht stehen sollen, zu fuellen
    Informationen werden aus der Datenbank geladen
     */
    private void spinnerOptionenAlleProjekteFuellen()
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

        spinnerProjekte.setAdapter(null);

        if (projekte.size() > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item,
                    projekte);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProjekte.setAdapter(adapter);
        }
    }


    /**
     * Quelle: http://stackoverflow.com/questions/6044793/popupwindow-with-image
     * @param imageView
     * @param width
     * @param height
     */
    private void loadPhoto(ImageView imageView, int width, int height) {

        ImageView tempImageView = imageView;


        AlertDialog.Builder imageDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.custom_fullimage_dialog,
                (ViewGroup) view.findViewById(R.id.layout_root));
        //ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
        TouchImageView image = (TouchImageView) layout.findViewById(R.id.fullimage);
        image.setImageDrawable(tempImageView.getDrawable());
        imageDialog.setView(layout);
        imageDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });


        imageDialog.create();
        imageDialog.show();
    }

}
