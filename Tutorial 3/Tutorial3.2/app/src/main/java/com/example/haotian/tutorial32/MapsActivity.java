package com.example.haotian.tutorial32;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.opencsv.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "MapsActivity";
    public static final int THUMBNAIL = 1;
    protected String root;
    String filePath;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Button picButton; //takes user to camera
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private Bitmap imageBitmap;
    private List<String[]> entries;
    private Hashtable<Marker, Long> markerToTimestamp = new Hashtable<>();
    private File file;
    private FileReader mFileReader;
    private MapsActivity thisActivity = this;
    private com.google.android.gms.location.LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        root = Environment.getExternalStorageDirectory().toString();
        filePath = root + "/DCIM/markers.csv";
        picButton = (Button) findViewById(R.id.photobutton);
        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        //read the file to initialize
        try {
            mFileReader = new FileReader(filePath);
        } catch (java.io.FileNotFoundException e) {
            try {
                String[] firstRow = {"TimeStamp", "Latitude", "Longitude", "(Title)", "(Snippet)"};
                FileWriter mFileWriter = new FileWriter(filePath);
                CSVWriter mCSVWriter = new CSVWriter(mFileWriter);
                mCSVWriter.writeNext(firstRow);
                mCSVWriter.close();
                mFileWriter.close();
            } catch (Exception e2) {
                e.printStackTrace();
            }
        }
        //now let's read again to make sure that we got the file read
        try {
            mFileReader = new FileReader(filePath);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        }
        CSVReader mCSVReader = new CSVReader(mFileReader);

        //read CSV into entries list
        try {
            entries = mCSVReader.readAll();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        populateMarkers();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    // Creates LocationRequest object per the Android developer instructions
    // TODO: Potentially change parameters
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Starts location updates (based on method from Android dev page)
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mLocationListener);
    }

    // Stops location updates (based on method from Android dev page
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
    }

    // Next three methods needed to build Google API Client
    @Override
    public void onConnected(Bundle connectionHint) {
        System.out.println("CONNECTION SUCCEEDED");
        createLocationRequest();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        System.out.println("CONNECTION FAILED");
    }


    // Method to create picture-taking intent based on method given in instructions.
    private void dispatchTakePictureIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePicture, THUMBNAIL);
    }

    // Method to setup GoogleApiClient given from the Android Developer page
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                TitleSnippetDialogFragment d = TitleSnippetDialogFragment.createFragment(marker,thisActivity);
                d.show(getSupportFragmentManager(), "title_snippet");
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTitle().equals("") && marker.getSnippet().equals("")) {
                    TitleSnippetDialogFragment d = TitleSnippetDialogFragment.createFragment(marker,thisActivity);
                    d.show(getSupportFragmentManager(), "title_snippet");
                } else {
                    marker.showInfoWindow();
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == THUMBNAIL && resultCode == RESULT_OK) {
            //Gather basic data
            Long timeStamp = System.currentTimeMillis();
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            FileOutputStream out = null;

            //Saving the thumbnail
            try {
                out = new FileOutputStream(root + "/DCIM" + "/bitmap" + timeStamp + ".png");
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Add the marker
            LatLng position = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            Marker newMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Untitled")
                    .snippet("No Description")
                    .icon(BitmapDescriptorFactory.fromBitmap(imageBitmap)));

            //Save the marker
            try {
                addToCSV(position, ""+timeStamp);
                markerToTimestamp.put(newMarker, timeStamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addToCSV(LatLng position, String timeStamp) throws java.io.FileNotFoundException, java.io.IOException {
        //to be optimized, everything is taken out here from the old file, modified and rewritten. It is only efficient for small amounts of rows here
        //If we have time we could optimize more here, but for our application it's enough
        String[] newRow = {timeStamp, "" + position.latitude, "" + position.longitude, "Untitled", "No Description"};
        FileWriter mFileWriter = new FileWriter(filePath);
        CSVWriter mCSVWriter = new CSVWriter(mFileWriter);
        entries.add(newRow);
        mCSVWriter.writeAll(entries);
        mCSVWriter.close();
    }

    public void updateTitleAndSnippet(Marker marker, String newTitle, String newSnippet) throws java.io.IOException {
        FileWriter mFileWriter = new FileWriter(filePath);
        CSVWriter mCSVWriter = new CSVWriter(mFileWriter);
        String[] row = rowOf(markerToTimestamp.get(marker));
        row[3] = newTitle;
        row[4] = newSnippet;
        mCSVWriter.writeAll(entries);
        mCSVWriter.close();
    }

    public void removeMarker(Marker marker) throws java.io.IOException {
        FileWriter mFileWriter = new FileWriter(filePath);
        CSVWriter mCSVWriter = new CSVWriter(mFileWriter);
        String[] row = rowOf(markerToTimestamp.get(marker));
        entries.remove(row);
        mCSVWriter.writeAll(entries);
        mCSVWriter.close();
    }

    private void populateMarkers() {
        for (int rowNumber = 1; rowNumber < entries.size(); rowNumber++) {
            //(The first row is ignored)
            String[] row = entries.get(rowNumber);
            //convert a raw into 3 variables
            long timeStamp = Long.parseLong(row[0]);
            double latitude = Double.parseDouble(row[1]);
            double longitude = Double.parseDouble(row[2]);
            String title = row[3];
            String snippet = row[4];

            //open the related bitmapFIle
            String bitmapFile = root + "/DCIM" + "/bitmap" + timeStamp + ".png";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap imageBitmap = BitmapFactory.decodeFile(bitmapFile, options);
            //Add the marker
            LatLng position = new LatLng(latitude, longitude);
            Marker newMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.fromBitmap(imageBitmap)));
            markerToTimestamp.put(newMarker, timeStamp);

        }
    }

    String[] rowOf(Long ts) {
        for (int rowNumber = 1; rowNumber < entries.size(); rowNumber++) {
            String[] row = entries.get(rowNumber);
            if (Long.parseLong(row[0]) == ts)
                return row;
        }
        return null;
    }

}
