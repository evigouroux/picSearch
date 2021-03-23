package com.example.picsearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.widget.Toast;
import android.os.Vibrator;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import org.w3c.dom.Text;

public class GameActivity extends AppCompatActivity implements LocationListener {

    Slider rangeSlider;

    LocationManager locationManager;
    Handler handler = new Handler();
    Runnable runnable;
    Location target = new Location("");

    float range = 0;

    //Nombre de miliseconde entre chaque refresh
    int delay = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        rangeSlider = findViewById(R.id.rangeSlider);

        // Target location (TEST)
        target.setLatitude(37.3175);
        target.setLongitude(-121.9109);

        if(ContextCompat.checkSelfPermission(GameActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(GameActivity.this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);

        }

    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                getLocation();
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, GameActivity.this);
        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        //Distance between the user and its target x2 = cible, x1 = user
        double distance =  Math.round(location.distanceTo(target)) ;
        range = rangeSlider.getValue();

        if (distance < range) {

            // 400 miliseconds vibration if the user is within range of the target
            Vibrator v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
            v.vibrate(400);
        }

        Toast.makeText(this, "" + distance + " / " + range, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}




