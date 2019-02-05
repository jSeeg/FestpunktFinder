package joernseeglitz_beuth_hochschule.de.festpunktfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GetLocationActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = "GetLocationAcitivity";

    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private Location standort;

    private double latitude;
    private double longitude;

    Konstanten konstanten;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        konstanten = new Konstanten(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(konstanten.getIntervalLocationRequest());

        //Methode fuer Location-Berechtigung vom User
        holeLocationMitBerechtigung();

        standort = locationMitBerechtigung();

        latitude = standort.getLatitude();
        longitude = standort.getLongitude();

        //sendIntent();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient Verbindung unterbrochen.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient Verbindungsfehler!");
    }

    @Override
    public void onLocationChanged(Location location) {
        standort = location;
        latitude = standort.getLatitude();
        longitude = standort.getLongitude();

        sendIntent();
    }

    /**
     * Methode um die Laengen- und Breitengrade in einem Intent zu uebergeben
     */
    private void sendIntent()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("latitude",latitude);
        returnIntent.putExtra("longitude",longitude);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    //Quelle: https://github.com/noahpatterson/nanodegree_playServices_location/blob/master/app/src/main/java/com/example/noahpatterson/playservicesapp/MainActivity.java
    public void holeLocationMitBerechtigung() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("PERMISSON", "in shouldShowRationale");
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            } else {
                ActivityCompat.requestPermissions(this,
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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("PERMISSON", "in shouldShowRationale");
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            }
        } else {
            loc = LocationServices.FusedLocationApi.getLastLocation
                    (mGoogleApiClient);
        }
        return loc;
    }
}
