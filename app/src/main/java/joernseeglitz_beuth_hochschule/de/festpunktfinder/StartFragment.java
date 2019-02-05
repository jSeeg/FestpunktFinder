package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.app.Fragment;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class StartFragment extends Fragment {

    ImageView imageViewStart;

    TextView textViewDBPunktAnzahl;
    TextView textViewDBBilderAnzahl;
    TextView textViewDBProjekteAnzahl;
    TextView textViewDBGroesse;

    Context context;

    Konstanten konstanten;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.startfragment, container, false);

        context = getActivity();

        konstanten = new Konstanten(context);

        imageViewStart = (ImageView) view.findViewById(R.id.imageViewStart);
        //imageViewStart.setImageResource(R.mipmap.ic_launcher);
        imageViewStart.setImageResource(R.drawable.icon);
        //Minimal- und Maximalgrosse des Views setzen
        imageViewStart.setMinimumHeight(500);
        imageViewStart.setMinimumWidth(500);
        imageViewStart.setMaxHeight(500);
        imageViewStart.setMaxWidth(500);

        MapDBDataSource dataSource = new MapDBDataSource(context);
        Log.d("StartFragment", "Die Datenquelle wird geöffnet.");
        dataSource.open();

        textViewDBPunktAnzahl = (TextView) view.findViewById(R.id.textViewStartDBPunkteAnzahl);
        textViewDBPunktAnzahl.setText("Anzahl der Punkte: " +
                String.valueOf(dataSource.getPunktAnzahl()));

        textViewDBProjekteAnzahl = (TextView) view.findViewById(R.id.textViewStartDBProjekteAnzahl);
        textViewDBProjekteAnzahl.setText("Anzahl der Projekte: "
                + String.valueOf(dataSource.getProjekteAnzahl()));

        textViewDBBilderAnzahl = (TextView) view.findViewById(R.id.textViewStartDBBilderAnzahl);
        textViewDBBilderAnzahl.setText("Anzahl der Bilder: " +
                String.valueOf(dataSource.getBilderAnzahl()));

        textViewDBGroesse = (TextView) view.findViewById(R.id.textViewStartDBGroesse);
        textViewDBGroesse.setText("Größe der Datenbank: " +
                String.format("%.3f",dataSource.getDatenbankGroesse())
                + " MB");

        Log.d("StartFragment", "Die Datenquelle wird geschlossen.");
        dataSource.close();

        return view;
    }


}
