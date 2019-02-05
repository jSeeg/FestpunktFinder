package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


//TODO: Bestimmte Projekte aus der Karte löschen

public class EinstellungsFragment extends Fragment {

    Button buttonDatenbankZuruecksetzen;
    Button buttonKartenPunkteEntfernen;
    Button buttonExterneDBUebernehmen;
    Button buttonMapUebernehmen;

    EditText editTextPositionsInterval;
    EditText editTextMapZoomEntfernung;

    EditText editTextExternDBUser;
    EditText editTextExternDBPasswort;
    EditText editTextExternDBDatenbank;
    EditText editTextExternDBServer;
    EditText editTextExternDBUrl;

    Context context;

    Konstanten konstanten;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.einstellungsfragment, container, false);

        context = getActivity();

        MainActivity activity = (MainActivity) getActivity();

        konstanten = activity.getConfigExternDB();

        buttonDatenbankZuruecksetzen = (Button) view.findViewById(R.id.buttonEinstellungenDatenbankZuruecksetzen);
        buttonKartenPunkteEntfernen = (Button) view.findViewById(R.id.buttonEinstellungenKartenPunkteEntfernen);
        buttonExterneDBUebernehmen = (Button) view.findViewById(R.id.buttonEinstellungenExterneDBUebernehmen);
        buttonMapUebernehmen = (Button) view.findViewById(R.id.buttonEinstellungenMapUebernehmen);

        editTextPositionsInterval = (EditText) view.findViewById(R.id.editTextEinstellungenMapPoSitionsInterval);
        editTextPositionsInterval.setText(String.valueOf(konstanten.getIntervalLocationRequest()));
        editTextMapZoomEntfernung = (EditText) view.findViewById(R.id.editTextEinstellungenMapZoomEntfernug);
        editTextMapZoomEntfernung.setText(String.valueOf(konstanten.getAbstandMapCameraZoom()));

        editTextExternDBUser = (EditText) view.findViewById(R.id.editTextEinstellungenExternDBUser);
        editTextExternDBUser.setText(konstanten.getExternDBUser());
        editTextExternDBPasswort = (EditText) view.findViewById(R.id.editTextEinstellungenExternDBPasswort);
        editTextExternDBPasswort.setText(konstanten.getExternDBPasswort());
        editTextExternDBDatenbank = (EditText) view.findViewById(R.id.editTextEinstellungenExternDBDB);
        editTextExternDBDatenbank.setText(konstanten.getExternDBDatenbank());
        editTextExternDBServer = (EditText) view.findViewById(R.id.editTextEinstellungenExternDBServer);
        editTextExternDBServer.setText(konstanten.getExternDBServer());
        editTextExternDBUrl = (EditText) view.findViewById(R.id.editTextEinstellungenExternDBUrl);
        editTextExternDBUrl.setText(konstanten.getExternDBUrl());

        /*
        Listener fuer den Datenbank-zuruecksetze-Button
        Ruft eine Dialogabfrage auf zur Sicherheitsabfrage ob die Datenkbank zurueck gesetzt werden soll
         */
        buttonDatenbankZuruecksetzen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                //Datenbank löschen
                                context.deleteDatabase("mapDB.db");
                                Toast.makeText(context,"Die Datenbank wurde zurückgesetzt!",Toast.LENGTH_LONG).show();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Möchten Sie die Datenbank wirklich zurücksetzen?")
                        .setPositiveButton("Ja", dialogClickListener)
                        .setNegativeButton("Nein", dialogClickListener).show();

            }
        });

        /*
        Listener fuer den Karten-PUnkte-entfernen-Button
        Ruft eine Dialogabfrage auf zur Sicherheitsabfrage ob die Punkte in der Karte entfernt werden sollen
         */
        buttonKartenPunkteEntfernen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked

                                MainActivity activity = (MainActivity) getActivity();
                                activity.setKartenPunktListe(null);
                                Toast.makeText(context,"Die Punkte wurden aus der Karte entfernt!",Toast.LENGTH_LONG).show();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Möchten Sie wirklich alle dargestellten Punkte aus der Karte entfernen?")
                        .setPositiveButton("Ja", dialogClickListener)
                        .setNegativeButton("Nein", dialogClickListener).show();
            }
        });

        buttonExterneDBUebernehmen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextExternDBUser.getText().toString().trim().equals("")  &&
                        !editTextExternDBDatenbank.getText().toString().trim().equals("") &&
                        !editTextExternDBServer.getText().toString().trim().equals("") &&
                        !editTextExternDBUrl.getText().toString().trim().equals(""))
                {

                    String user = editTextExternDBUser.getText().toString().trim();
                    String pw = editTextExternDBPasswort.getText().toString().trim();
                    String db = editTextExternDBDatenbank.getText().toString().trim();
                    String server = editTextExternDBServer.getText().toString().trim();
                    String url = editTextExternDBUrl.getText().toString().trim();

                    MainActivity activity = (MainActivity) getActivity();
                    activity.setConfigExternDB(user,pw,db,server,url);
                    Toast.makeText(context,"Konfigurationsdaten gespeichert.",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Log.w("EINSTELLUNGEN","FEHLERR: Nicht alle Extern-DB angaben getätigt.");
                    Toast.makeText(context,"Bitte alle Eingaben tätigen!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Button Click Listener
         * Uebernimmt die Config-Einstellungen fuer das Positionsabfrageintervall
         * und Map-Zoom-Entfernung
         */
        buttonMapUebernehmen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences configs = context.getSharedPreferences("ConfigFile",0);
                SharedPreferences.Editor editor = configs.edit();

                editor.putString("mapPosInterval",String.valueOf(editTextPositionsInterval.getText()));
                editor.putString("mapZoomEntfernung",String.valueOf(editTextMapZoomEntfernung.getText()));

                editor.commit();

                Toast.makeText(context,"Einstellungen übernommen.",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
