package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import android.location.Location;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.text.DecimalFormat;

//import fuer Directions
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

import org.json.JSONObject;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //######################################################################################
    //Variablendeklaration:
    private static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Konstanten konstanten;

    View v;

    Polyline line, directLine;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    Location myLocation = new Location("");
    Location lastLocation = new Location("");
    LatLng myPos = new LatLng(0, 0);

    boolean firstLocationChanged = true;

    MapView mMapView;

    WeakHashMap<Marker,String> markerHash = new WeakHashMap<Marker,String>();
    List<Marker> markerListe = new ArrayList<Marker>();
    String markerId = "";
    boolean markerClicked = false;

    private static final String LOG_TAG = MapFragment.class.getSimpleName();

    private double routenDistanz;
    private String standOrtGenauigkeit = "";

    TextView textViewOben;
    TextView textViewDistanzen;

    LayoutInflater mapLayoutInflater;

    Context context;

    //Liste der Kartenpunkte fuer die Marker
    List<GeodaetischerPunkt> punkteListe;

    LinearLayout punkteSucheLinearLayout;

    ImageButton buttonPunkteSucheLayoutAnzeigen;
    Button buttonPunkteSuche;

    Spinner spinnerPunkteSuche;
//######################################################################################



    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        // inflat and return the layout
        v = inflater.inflate(R.layout.mapfragment, container, false);

        context = getActivity();

        mapLayoutInflater = inflater;

        punkteSucheLinearLayout = (LinearLayout) v.findViewById(R.id.MapPunktSucheLinearLayout);
        punkteSucheLinearLayout.setVisibility(LinearLayout.GONE);

        buttonPunkteSucheLayoutAnzeigen = (ImageButton) v.findViewById(R.id.buttonMapPunktSucheShowLayout);
        buttonPunkteSucheLayoutAnzeigen.setImageResource(android.R.drawable.arrow_down_float);
        buttonPunkteSuche = (Button) v.findViewById(R.id.buttonMapPunktSuche);

        spinnerPunkteSuche = (Spinner) v.findViewById(R.id.spinnerMapPunktSuche);

        try{
            konstanten = new Konstanten(getActivity());
            textViewOben = (TextView) v.findViewById(R.id.mapfragmentTextViewOben);
            textViewDistanzen = (TextView) v.findViewById(R.id.textViewMapFragmentDistanzen);
            textViewDistanzen.setVisibility(TextView.GONE);
            mMapView = (com.google.android.gms.maps.MapView) v.findViewById(R.id.mapfragment);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);
            //Quelle: https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient.Builder
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            initalRequestLocationPermission();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        //Button-Klick soll das Layout anzeigen fuer die Punktesuche
        buttonPunkteSucheLayoutAnzeigen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (punkteSucheLinearLayout.getVisibility() == LinearLayout.GONE) {
                    buttonPunkteSucheLayoutAnzeigen.setImageResource(android.R.drawable.arrow_up_float);
                    if (punkteListe != null && punkteListe.size() > 0) {
                        punkteSucheLinearLayout.setVisibility(LinearLayout.VISIBLE);
                    } else {
                        punkteSucheLinearLayout.setVisibility(LinearLayout.GONE);
                        buttonPunkteSucheLayoutAnzeigen.setImageResource(android.R.drawable.arrow_down_float);
                        Toast.makeText(context, "Es wurden keine Punkte gefunden.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    buttonPunkteSucheLayoutAnzeigen.setImageResource(android.R.drawable.arrow_down_float);
                    punkteSucheLinearLayout.setVisibility(LinearLayout.GONE);
                }
            }
        });

        //Button-Cklick um auf den jeweiligen Punkt zu springen
        buttonPunkteSuche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Projektabfrage fuer die Punktsuche

                String pNum = spinnerPunkteSuche.getSelectedItem().toString();
                for (Marker m:markerListe
                        ) {
                    if(m.getTitle().equals(pNum))
                    {
                        LatLng markerLAtLong = new LatLng(
                                m.getPosition().latitude,m.getPosition().longitude);
                        moveCameraToLatLong(markerLAtLong);
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(konstanten.getIntervalLocationRequest());
            //Methode fuer Location-Berechtigung vom User
            holeLocationMitBerechtigung();

            //Zoom auf den Standpunkt
            myLocation = locationMitBerechtigung();
            if(myLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        myLocation.getLatitude(), myLocation.getLongitude()), 16));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Connection Failed", "Location services connection Fehlgeschlagen: " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            //Polylinien entfernen wenn markerClicked false ist
            if(!markerClicked)
            {
                lineEntfernen(0);
            }
            //Beim ersten Standortunterschied CameraZoom auf Standort
            //dient dem Zoom beim Startup (Workaround)
            if (firstLocationChanged == true) {
                firstLocationChanged = false;
                lastLocation = location;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        location.getLatitude(),location.getLongitude()),16));
            }
            //CameraZoom bei ausreichendem Abstand
            if (location.distanceTo(lastLocation) > konstanten.getAbstandMapCameraZoom())
            {
                lastLocation = location;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        location.getLatitude(),location.getLongitude()),16));
            }

            //Standortpositionsgenauigkeit erfassen
            standOrtGenauigkeit = String.valueOf(location.getAccuracy());
            if(!standOrtGenauigkeit.equals(""))
            {
                textViewOben.setText("Positionsgenauigkeit: "+ standOrtGenauigkeit +"m");
            }
            else
            {
                textViewOben.setText("");
            }

            //Polylinien neu zeichnen wenn sich der Standort aendert
            if(markerClicked == true) {
                // aktueller Standort
                myPos = new LatLng(location.getLatitude(), location.getLongitude());
                //Markerposition
                int markerIndex = 0;

                for (Marker m : markerListe) {
                    if (Objects.equals(m.getId(),markerId)) {
                        Marker slctMarker = m;
                        LatLng markerPos = new LatLng(slctMarker.getPosition().latitude, slctMarker.getPosition().longitude);

                        //Directions:
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(myPos, markerPos);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);

                        //einfache Polyline:
                        PolylineOptions pOptions = new PolylineOptions()
                                .width(2).color(Color.BLUE).geodesic(true);
                        pOptions.add(myPos);
                        pOptions.add(markerPos);
                        lineEntfernen(1);
                        line = mMap.addPolyline(pOptions);

                        //Distanzen anzeigen lassen
                        Location markerLocation = new Location("");
                        markerLocation.setLatitude(markerPos.latitude);
                        markerLocation.setLongitude(markerPos.longitude);
                        double direkteDistanz = location.distanceTo(markerLocation);
                        textViewDistanzen.setText("Entfernungen: [Route: " +
                                String.format("%.3f",routenDistanz) + "m; " +
                                "Luftlinie: "
                                + String.format("%.3f",direkteDistanz) + " m]");
                        textViewDistanzen.setVisibility(TextView.VISIBLE);


                        break;
                    }
                    markerIndex++;
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        try {
            mMap = googleMap;

            /*
            Adapter fuer eigenes Marker-Info-Fenster bereitstellen
             */
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    //View aus eigenem InfoFensterLayout
                    return createCustomInfoWindow();
                }
            });

            //Methode fuer diverse Einstellungen der Map
            setUpMap();

            mMap.setOnMarkerDragListener(new OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Log.d("MarkerDragEnd", "Marker entfernt");
                    lineEntfernen(0);
                    marker.remove();
                    markerId = marker.getId();
                    int markerRemoveIndex = 0;
                    for (Marker m : markerListe) {
                        if (Objects.equals(m.getId(), markerId)) {
                            break;
                        }
                        markerRemoveIndex++;
                    }
                    markerListe.remove(markerRemoveIndex);
                }
            });

            /*
            Beim normalen Klicken auf das Infofenster soll das Infofenster geschlossen werden.
            Dient der besseren Sichtbarkeit der Routen auf der Karte.
             */
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    marker.hideInfoWindow();
                }
            });

            /*
            Beim langen Klicken auf das Infofenster soll die Punktuebersicht mit dem entsprechenden
            Punkt aufgerufen werden.
             */
            mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                @Override
                public void onInfoWindowLongClick(Marker marker) {
                    List<GeodaetischerPunkt> geodaetischerPunktListe;
                    GeodaetischerPunkt geodaetischerPunkt;
                    String punktNummer, projektNummer;
                    MapDBDataSource dataSource = new MapDBDataSource(context);

                    for (Marker m : markerListe) {
                        if (Objects.equals(m.getId(), markerId)) {
                            Marker slctMarker = m;
                            punktNummer = slctMarker.getTitle();
                            projektNummer = slctMarker.getSnippet();

                            Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                            dataSource.open();

                            geodaetischerPunktListe = dataSource.getAllMapDBforPunkt(projektNummer, punktNummer);

                            Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                            dataSource.close();

                            if (geodaetischerPunktListe.size() > 0) {
                                geodaetischerPunkt = geodaetischerPunktListe.get(0);
                                MainActivity activity = (MainActivity) getActivity();
                                activity.punktInPunktUebersichtDarstellen();
                            }
                            else
                            {
                                //TODO: Wenn der Punkt nicht vorhanden ist, der Datenbank hinzufuegen
                            }
                        }
                    }
                }
            });

            // Klicklistener fuer einfaches klicken auf Map
            // entfernt die Polylinie
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    lineEntfernen(0);
                    markerClicked = false;

                    //Selektierter Marker in der MainAcitivity auf null setzen
                    MainActivity activity = (MainActivity) getActivity();
                    activity.setSelectedMarkerPunkt(null);

                    //Entfernung zum Marker nicht mehr anzeigen
                    textViewDistanzen.setVisibility(TextView.GONE);
                }
            });

            // bei langem Klicken auf die Map wird ein Marker erzeugt
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                //@Override
                public void onMapLongClick(LatLng point) {
                    System.out.println("Auf Map lange geklickt!");
                    Marker mrk = mMap.addMarker(new MarkerOptions().position(point)
                            .title("Marker")
                            .snippet("Snippet")
                            .draggable(true));
                    markerListe.add(mrk);
                }

            });

            // Klicklistener fuer  klicken auf einen Marker
            // Infofenster wird geoeffnet
            // Polylinie von Marker zum Standort wird gezeichnet
            mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    markerClicked = true;
                    markerId = marker.getId();
                    double lat, lon;
                    DecimalFormat format = new DecimalFormat("#.000000");
                    lat = marker.getPosition().latitude;
                    lon = marker.getPosition().longitude;
                    LatLng markerPos = new LatLng(lat, lon);
                    Location markerLoc = new Location("");
                    markerLoc.setLatitude(lat);
                    markerLoc.setLongitude(lon);

                    MapDBDataSource dataSource = new MapDBDataSource(context);

                    Log.d("MapFragment", "Die Datenquelle wird geöffnet.");
                    dataSource.open();

                    //PunktNummer steht im Markertitel, Projektnummer im Markersnippet
                    List<GeodaetischerPunkt> markerPunkt = dataSource.getAllMapDBforPunkt(
                            marker.getSnippet(),marker.getTitle());

                    Log.d("MapFragment", "Die Datenquelle wird geschlossen.");
                    dataSource.close();

                    //Marker als selektierter Marker in MainAcitivity setzen
                    MainActivity activity = (MainActivity) getActivity();
                    if(markerPunkt.size()>0) {
                        activity.setSelectedMarkerPunkt(markerPunkt.get(0));
                    }
                    else
                    {
                        activity.setSelectedMarkerPunkt(null);
                    }

                    // locationMitBerechtigung() Methode um letzte Position zu bekommen mit
                    // entsprechender Berechtigungsabfrage
                    //uebergibt Location
                    myLocation = locationMitBerechtigung();
                    myPos = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                    //Polylinien:
                    //Linien entfernen bevor neue Linien gezeichnet werden
                    lineEntfernen(0);

                    //Directions-Polylinie:
                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(myPos, markerPos);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);

                    //einfache Polyline:
                    PolylineOptions pOptions = new PolylineOptions()
                            .width(2).color(Color.BLUE).geodesic(true);
                    pOptions.add(myPos);
                    pOptions.add(markerPos);
                    line = mMap.addPolyline(pOptions);


                    // Mapzoom mit Marker am unteren Bildschirmrand, sodass das Infofenster voll
                    // angezeigt wird
                    /*
                    Quelle: http://stackoverflow.com/questions/16764002/how-to-center-the-camera-so-that-marker-is-at-the-bottom-of-screen-google-map
                     */
                    MapView mapContainer = (MapView) v.findViewById(R.id.mapfragment);
                    int container_height = mapContainer.getHeight();

                    Projection projection = mMap.getProjection();

                    LatLng markerLatLng = new LatLng(marker.getPosition().latitude,
                            marker.getPosition().longitude);
                    Point markerScreenPosition = projection.toScreenLocation(markerLatLng);
                    Point pointHalfScreenAbove = new Point(markerScreenPosition.x,
                            markerScreenPosition.y - (container_height / 3));

                    LatLng aboveMarkerLatLng = projection
                            .fromScreenLocation(pointHalfScreenAbove);

                    marker.showInfoWindow();

                    CameraUpdate center = CameraUpdateFactory.newLatLng(aboveMarkerLatLng);
                    mMap.animateCamera(center);

                    return true;
                }
            });

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Quelle: https://github.com/noahpatterson/nanodegree_playServices_location/blob/master/app/src/main/java/com/example/noahpatterson/playservicesapp/MainActivity.java
    public void holeLocationMitBerechtigung() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("PERMISSON", "in shouldShowRationale");
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    //Quelle: https://github.com/noahpatterson/nanodegree_playServices_location/blob/master/app/src/main/java/com/example/noahpatterson/playservicesapp/MainActivity.java
    public Location locationMitBerechtigung() {
        Location loc = new Location("");
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("PERMISSON", "in shouldShowRationale");
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }

            } else {

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        } else {
            loc = LocationServices.FusedLocationApi.getLastLocation
                    (mGoogleApiClient);
        }
        return loc;
    }


    //Quelle: https://github.com/noahpatterson/nanodegree_playServices_location/blob/master/app/src/main/java/com/example/noahpatterson/playservicesapp/MainActivity.java
    public void initalRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("PERMISSON", "in shouldShowRationale");
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    //Quelle: https://github.com/noahpatterson/nanodegree_playServices_location/blob/master/app/src/main/java/com/example/noahpatterson/playservicesapp/MainActivity.java
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Entfernt eine Polylinie von einem Marker zum Standort
     * Switch:
     * 0: einfache Polylinie + Directions-Polylinie entfernen
     * 1: einfache Polylinie entfernen
     * 2: Directions-Polylinie entfernen
     */
    private void lineEntfernen(int s) {
        switch(s)
        {
            case 0:
                //beide Linien entfernen
                if (line != null) {
                    line.remove();
                }
                if (directLine != null) {
                    directLine.remove();
                }
                break;
            case 1:
                //Luftlinie entfernen
                if (line != null) {
                    line.remove();
                }
                break;
            case 2:
                //Directions-Linie entfernen
                if (directLine != null) {
                    directLine.remove();
                }
                break;
            default:
                System.out.println("keine Auswahl uebergeben");
        }
    }

    /*
    * Methode wird aufgerufen durch onMapReady
    * Dient der Grundeinstellungen der Karte
    */
    private void setUpMap() {
        try {
            // eigene Position wird angezeigt
            mMap.setMyLocationEnabled(true);

            // Kartentyp auf normal gesetzt
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            //Koordinaten die fuer den Mapimportgesetzt wurden laden
            //als Marker setzen
            MainActivity activity = (MainActivity) getActivity();

            punkteListe = activity.getKartenPunktListe();

            List<String> punktNummern = new ArrayList<>();

            //Marker reset
            mMap.clear();
            markerListe.clear();

            if (punkteListe.size() > 0) {
                for (GeodaetischerPunkt punkt:punkteListe
                        ) {

                    //Marker setzen fuer Punkte die in die Karte eingeladen wurden
                    if(punkt.getLatitude() != 0 || punkt.getLongitude() != 0)
                    {
                        Marker mrk = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(punkt.getLatitude(), punkt.getLongitude()))
                                .title(punkt.getPunktNummer())
                                .snippet(punkt.getProjektNummer())
                                .draggable(true)
                        );
                        markerListe.add(mrk);
                        punktNummern.add(punkt.getPunktNummer());
                    }

                    //Spinner fuer die Punktesuche fuellen
                    spinnerPunkteSuche.setAdapter(null);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                            android.R.layout.simple_spinner_item,
                            punktNummern);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPunkteSuche.setAdapter(adapter);
                }

            }

        } catch (SecurityException sec) {
            System.out.println(sec.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Quelle: http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
     */
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        //key anbinden
        parameters += "&key=" + getResources().getString(R.string.google_server_key);

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }
/**
 * Quelle: http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
 */
    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while dl url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    /**
     * Quelle: http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
     */
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            if(data!= null && !data.toLowerCase().contains(("ZERO_RESULTS").toLowerCase())) {
                return data;
            }
            else
            {
                return null;
            }
        }
        /**
         * Quelle: http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
         */
        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result!= null && !result.toLowerCase().contains(("ZERO_RESULTS").toLowerCase())) {
                ParserTask parserTask = new ParserTask();
                // Invokes the thread for parsing the JSON data
                parserTask.execute(result);
            }
        }
    }

/**
 * Quelle: http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
 */
    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }
        /**
         * Quelle: http://wptrafficanalyzer.in/blog/drawing-driving-route-directions-between-two-locations-using-google-directions-in-google-map-android-api-v2/
         */
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            double distanz = 0;

            try {

                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        if (j > 0) {
                            Location dieserPunkt = new Location("");
                            dieserPunkt.setLatitude(lat);
                            dieserPunkt.setLongitude(lng);

                            Location letzterPunkt = new Location("");
                            letzterPunkt.setLatitude(points.get(j - 1).latitude);
                            letzterPunkt.setLongitude(points.get(j - 1).longitude);

                            distanz += dieserPunkt.distanceTo(letzterPunkt);
                        }

                        points.add(position);
                    }

                    routenDistanz = distanz;

                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points)
                            .width(5).color(Color.RED).geodesic(true);
                }

                // Drawing polyline in the Google Map for the i-th route
                lineEntfernen(2);
                directLine = mMap.addPolyline(lineOptions);
            }
            catch (Exception e)
            {
                Log.e("MAP_DIRECTIONS",e.getMessage());
            }
        }
    }


    /**
     * Methode um auf eine bestimmte Position zu zoomen.
     * @param latLong
     */
    private void moveCameraToLatLong(LatLng latLong)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong,20));
    }

    private View createCustomInfoWindow()
    {
        View infoFensterView = mapLayoutInflater.inflate(R.layout.marker_info_fenster,null);

        List<GeodaetischerPunkt> geodaetischerPunktListe;
        List<GeodaetischerPunkt> punkteBilderListe = new ArrayList<>();
        GeodaetischerPunkt geodaetischerPunkt;
        GeodaetischerPunkt bilderPunkt;
        String punktNummer, projektNummer;
        MapDBDataSource dataSource = new MapDBDataSource(context);

        TextView header = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterHeader);
        TextView tvProjektNummer = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterProjektNummer);
        TextView tvRechtsWert = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterRechtsWert);
        TextView tvHochWert = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterHochWert);
        TextView tvHoehe = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterHoehe);
        TextView tvLatitude = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterLatitude);
        TextView tvLongitude = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterLongitude);
        TextView tvPunktArt = (TextView) infoFensterView.findViewById(R.id.textViewMarkerInfoFensterPunktArt);
        LinearLayout bilderLinearLayout = (LinearLayout) infoFensterView.findViewById(R.id.markerInfoFensterBilderLinearLayout);
        MainActivity activity = (MainActivity)getActivity();

        for (Marker m : markerListe) {
            if (Objects.equals(m.getId(), markerId)) {
                Marker slctMarker = m;
                punktNummer = slctMarker.getTitle();
                projektNummer = slctMarker.getSnippet();

                Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                dataSource.open();

                geodaetischerPunktListe = dataSource.getAllMapDBforPunkt(projektNummer,punktNummer);

                if(geodaetischerPunktListe.size()>0) {
                    punkteBilderListe = dataSource.getAllBilderDBforPunkt(projektNummer, punktNummer);
                }

                Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
                dataSource.close();

                if(geodaetischerPunktListe.size()>0) {
                    geodaetischerPunkt = geodaetischerPunktListe.get(0);

                    if(punkteBilderListe.size()>0)
                    {
                        bilderPunkt = punkteBilderListe.get(0);

                        geodaetischerPunkt.setBildListe(bilderPunkt.getBildListe());
                    }


                    //Daten in das Infofester setzen
                    header.setText(geodaetischerPunkt.getPunktNummer());
                    tvProjektNummer.setText("Projektnummer: " + geodaetischerPunkt.getProjektNummer());
                    tvRechtsWert.setText("Rechtswert: " + geodaetischerPunkt.getRechts());
                    tvHochWert.setText("Hochwert: " + geodaetischerPunkt.getHoch());
                    tvHoehe.setText("Höhe: " + geodaetischerPunkt.getHoehe());
                    tvLatitude.setText("Breite: " + geodaetischerPunkt.getLatitude());
                    tvLongitude.setText("Länge: " + geodaetischerPunkt.getLongitude());
                    tvPunktArt.setText("Punktart: " + geodaetischerPunkt.getPunktArt());

                    //Wenn bilder vorhanden sind sollen diese als ImageView
                    //in das dafuer vorgesehene LinearLayout hinzugefuegt werden
                    if(geodaetischerPunkt.getBildListe() != null) {
                        if (geodaetischerPunkt.getBildListe().size() > 0) {
                            bilderLinearLayout.removeAllViews();

                            LinearLayout.LayoutParams parameter = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            parameter.gravity= Gravity.CENTER_HORIZONTAL;
                            ImageView imageView = new ImageView(context);
                            imageView.setLayoutParams(parameter);
                            imageView.setImageBitmap(
                                    activity.scaleBitmapThumbnail(
                                            geodaetischerPunkt.getBildListe().get(0)
                                    ));
                            bilderLinearLayout.addView(imageView);
                        }
                    }
                }
                else
                {
                    //Marker wurde in der Datenbank als Punkt nicht gefunden

                    //Daten in das Infofester setzen
                    header.setText("Punkt ist noch nicht in der Datenbank.");
                    tvProjektNummer.setVisibility(TextView.GONE);
                    tvRechtsWert.setText("Rechtswert: ");
                    tvHochWert.setText("Hochwert: ");
                    tvHoehe.setVisibility(TextView.GONE);
                    tvLatitude.setText("Breite: " + slctMarker.getPosition().latitude);
                    tvLongitude.setText("Länge: " + slctMarker.getPosition().longitude);
                    tvPunktArt.setVisibility(TextView.GONE);

                    View viewMarker1 = (View) infoFensterView.findViewById(R.id.viewMarker1);
                    viewMarker1.setVisibility(View.GONE);
                    View viewMarker2 = (View) infoFensterView.findViewById(R.id.viewMarker2);
                    viewMarker2.setVisibility(View.GONE);
                    View viewMarker3 = (View) infoFensterView.findViewById(R.id.viewMarker3);
                    viewMarker3.setVisibility(View.GONE);
                }

                break;
            }
        }
        return infoFensterView;
    }
}