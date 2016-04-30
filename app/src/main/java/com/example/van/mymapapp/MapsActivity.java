package com.example.van.mymapapp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
                        LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static final String TAG = "VAN";     //MapsActivity.class.getSimpleName();
    public static final int MY_ACCESS_FINE_LOCATION_REQ_ID = 1;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.i(TAG, "onCreate() " );

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)           //should balance calls to this method with calls to unregisterConnectionCallbacks(ConnectionCallbacks) to avoid leaking resources
                .addOnConnectionFailedListener(this)    //should balance calls to this method with calls to unregisterConnectionFailedListener(OnConnectionFailedListener) to avoid leaking resources
                .addApi(LocationServices.API)
                .build();
        Log.i(TAG, "mGoogleApiClient = " + mGoogleApiClient);

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(900 * 1000)         // 10 secs, in ms
                .setFastestInterval(60 * 1000);  // 1 sec, in ms
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady() " );

        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mGoogleApiClient.connect();
        LatLng myPos = new LatLng(8.4792, 124.6511);
        mMap.addMarker(new MarkerOptions().position(myPos).title("Van"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPos));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

//        mMap.setMapType(3);

        if( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                                        != PackageManager.PERMISSION_GRANTED ){

            if( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ){
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                                                MY_ACCESS_FINE_LOCATION_REQ_ID);

            }
        }
        Log.i(TAG, "minZoom2 = " + mMap.getMinZoomLevel() + " maxZoom = " + mMap.getMaxZoomLevel());
        Log.i(TAG, "isBuildingsEnabled = " + mMap.isBuildingsEnabled() + "# isIndoorEnabled = " + mMap.isIndoorEnabled()
                + "# isMyLocationEnabled = " + mMap.isMyLocationEnabled() + "# isTrafficEnabled = " + mMap.isTrafficEnabled());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() " );

        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    private void setUpMapIfNeeded(){
        Log.i(TAG, "setUpMapIfNeeded() " );
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() " );

        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
        public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected():1");

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location == null){
            Log.i(TAG, "onConnected():2:getLastLocation is null");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }else{
            handleNewLocation(location);
        }
    }
    private void handleNewLocation(Location location){
        Log.i(TAG, "handleNewLocation() " );
        Log.i(TAG, location.toString());
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title("Van"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended()" );

        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed()");
        if(connectionResult.hasResolution()){
            try{
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }catch (IntentSender.SendIntentException e){
                e.printStackTrace();
            }
        }else{
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult():requestCode = " + requestCode + " ;resultCode = " +resultCode);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged()");
        handleNewLocation(location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionResult() " );

        switch(requestCode){
            case MY_ACCESS_FINE_LOCATION_REQ_ID: {
                if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // permission was granted, yay! Do the things you need to do.
//                    mMap.setMyLocationEnabled(true);
                    Log.i(TAG, " isMyLocationEnabled = " + mMap.isMyLocationEnabled());
                }else{
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    //Implements GoogleMap.OnMarkerClickListener
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i(TAG, "onMarkerClick()");
//        dispatchTakePictureIntent();
        return false;
    }

    private void dispatchTakePictureIntent() {
        Log.i(TAG, "dispatchTakePictureIntent()");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        Log.i(TAG, "createImageFile()");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
