package com.example.gpsstudy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.Console;
import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tvLat, tvLong, tvAddress;
    Button btnStart;
    Switch sw;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //catch each view
        tvLat = (TextView) findViewById(R.id.tvRealLat);
        tvLong = (TextView) findViewById(R.id.tvRealLong);
        btnStart = (Button) findViewById(R.id.btnStart);
        tvAddress = (TextView)findViewById(R.id.tvRealAddress);
        sw = (Switch) findViewById(R.id.sw1);

        //set all properties of LocationRequest
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000); // 30 seconds
        locationRequest.setFastestInterval(5000); /// 5 seconds

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        btnStart.setOnClickListener(this);
        sw.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //after permission granted
        switch (requestCode) {
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();//run update gps again because last time there wasnt permission

                } else {
                    Toast.makeText(this, "No Permission granted!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //permission is granted already
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //we got permission. put the values of location into UI
                    updateUIValues(location);
                }
            });
        } else {
            //no permission granted yet, so i request for it now
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        //update all the text view with the new location.
        Log.d("check","updated");
        tvLat.setText(String.valueOf(new DecimalFormat("##.###").format(location.getLatitude())));
        tvLong.setText(String.valueOf(new DecimalFormat("##.###").format(location.getLongitude())));
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tvAddress.setText(addresses.get(0).getAddressLine(0));

        }
        catch(Exception e){
            tvAddress.setText("Not available right now.");
        }

    }


    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            updateGPS();
        } else if (v == sw) {
            if (sw.isChecked()) {
                //turn on updative
                startUpdatingLocation();
            } else {
                //turn off updative
                stopUpdatingLocation();
            }

        }
    }

    private void startUpdatingLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (fusedLocationProviderClient != null) {

                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
                updateGPS();
            }
        }
       else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }

        }
    }
    private void stopUpdatingLocation() {
        if (fusedLocationProviderClient!=null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }
}
