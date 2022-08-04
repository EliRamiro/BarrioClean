package com.aply.barrioclean.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aply.barrioclean.R;
import com.aply.barrioclean.utils.Credentials;
import com.aply.barrioclean.utils.Preference;
import com.aply.barrioclean.utils.Utils;
import com.aply.barrioclean.utils.ViewDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    @BindView(R.id.et_recojo)
    TextView et_recojo;
    @BindView(R.id.btn_menu)
    Button btn_menu;
    @BindView(R.id.linear_btn_siguiente)
    LinearLayout linear_btn_siguiente;

    Context ctx;
    Credentials cred;
    Utils utils;
    ViewDialog viewDialog;
    int located = 0;
    LatLng recojo;

    BitmapDescriptor icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        ctx = this;
        cred = new Credentials(ctx);
        viewDialog = new ViewDialog(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        utils = new Utils(ctx);
        utils.initializeFirebase(ctx);
        icon = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);

        // Initialize the SDK
        Places.initialize(ctx, ctx.getResources().getString(R.string.google_maps_key));

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        et_recojo.setOnClickListener(this);
        btn_menu.setOnClickListener(this);
        linear_btn_siguiente.setOnClickListener(this);
        viewDialog.showDialog();
        if (cred.getData(Preference.ON_SERVICE).equals("1")) {
            viewDialog.hideDialog(0);
            ctx.startActivity(new Intent(ctx, WaitingRoomActivity.class));
            MapsActivity.this.finish();
        } else if (cred.getData(Preference.ON_SERVICE).equals("2")) {
            viewDialog.hideDialog(0);
            ctx.startActivity(new Intent(ctx, ServiceMapsActivity.class));
            MapsActivity.this.finish();
        }
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString().replace("\n", "");
                Log.e("location_address", strReturnedAddress.toString());
            } else {
                Log.e("location_address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("location_address", "Canont get Address!");
        }
        return strAdd;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(this::onLocationChanged);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                mMap.clear();
                LatLng my_location = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                String my_address = getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude);
                et_recojo.setText(my_address);
                cred.saveData(Preference.DIRECCION, my_address);
                cred.saveData(Preference.USER_LATITUD, String.valueOf(marker.getPosition().latitude));
                cred.saveData(Preference.USER_LONGITUD, String.valueOf(marker.getPosition().longitude));
                mMap.addMarker(new MarkerOptions()
                        .position(my_location)
                        .title(my_address)
                        .icon(icon)
                        .draggable(true))
                        .setDraggable(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(my_location));
                mMap.setMinZoomPreference(12);
                et_recojo.setText(my_address);
                recojo = my_location;
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null && located == 0) {
            mMap.clear();
            LatLng my_location = new LatLng(location.getLatitude(), location.getLongitude());
            String my_address = getCompleteAddressString(location.getLatitude(), location.getLongitude());
            et_recojo.setText(my_address);
            cred.saveData(Preference.DIRECCION, my_address);
            cred.saveData(Preference.USER_LATITUD, String.valueOf(location.getLatitude()));
            cred.saveData(Preference.USER_LONGITUD, String.valueOf(location.getLongitude()));
            mMap.addMarker(new MarkerOptions()
                    .position(my_location)
                    .title(my_address)
                    .icon(icon)
                    .draggable(true))
                    .setDraggable(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(my_location));
            mMap.setMinZoomPreference(12);
            mMap.setOnMapClickListener(latLng -> {
                mMap.clear();
                LatLng my_location1 = new LatLng(latLng.latitude, latLng.longitude);
                String my_address1 = getCompleteAddressString(latLng.latitude, latLng.longitude);
                cred.saveData(Preference.DIRECCION, my_address1);
                cred.saveData(Preference.USER_LATITUD, String.valueOf(latLng.latitude));
                cred.saveData(Preference.USER_LONGITUD, String.valueOf(latLng.longitude));
                mMap.addMarker(new MarkerOptions()
                        .position(my_location1)
                        .title(my_address1)
                        .icon(icon)
                        .draggable(true)).setDraggable(true);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(my_location1));
                mMap.setMinZoomPreference(12);
                located = 1;
                et_recojo.setText(my_address1);
                recojo = my_location1;
            });
            located = 1;
            et_recojo.setText(my_address);
            recojo = my_location;
            viewDialog.hideDialog(0);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.et_recojo:
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))
                        .setCountries(Arrays.asList("PE"))
                        .build(ctx);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                break;
            case R.id.btn_menu:
                startActivity(new Intent(ctx, MenuActivity.class));
                MapsActivity.this.finish();
                break;
            case R.id.linear_btn_siguiente:
                startActivity(new Intent(ctx, CotizaActivity.class));
                MapsActivity.this.finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("TAG", "Place: " + place.getAddress());
                et_recojo.setText(place.getAddress());
                mMap.clear();
                recojo = place.getLatLng();
                mMap.addMarker(new MarkerOptions()
                        .draggable(true)
                        .position(recojo)
                        .title(place.getAddress())
                        .icon(icon));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(recojo));
                cred.saveData(Preference.DIRECCION, place.getAddress());
                cred.saveData(Preference.USER_LATITUD, String.valueOf(place.getLatLng().latitude));
                cred.saveData(Preference.USER_LONGITUD, String.valueOf(place.getLatLng().longitude));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("TAG", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}