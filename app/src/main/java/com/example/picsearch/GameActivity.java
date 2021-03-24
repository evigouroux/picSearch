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
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
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
    Switch autoModeSwitch;
    Switch soundModeSwitch;
    Button foundButton;

    private SoundPool soundPool;
    private int heatSound1;

    boolean soundMode = false;
    boolean autoMode = false;

    LocationManager locationManager;
    Location currentLocation = null;
    double distance = 0;
    Handler handler = new Handler();
    Runnable runnable;
    Location target = new Location("");

    float range = 0;

    //Nombre de miliseconde entre chaque refresh
    int delay = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        rangeSlider = findViewById(R.id.rangeSlider);
        autoModeSwitch = findViewById(R.id.autoModeSwitch);
        soundModeSwitch = findViewById(R.id.soundModeSwitch);
        foundButton = findViewById(R.id.foundButton);

        // Target location (TEST)
        target.setLatitude(45.0469);
        target.setLongitude(3.8650);

        // Initializing distance to target as well as starting position
        getLocation();
        checkDistance();

        // Soundpool creation
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        heatSound1 = soundPool.load(this, R.raw.dea_ai_m, 1);

        // Event listener
        autoModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoMode = isChecked;
                rangeSlider.setEnabled(!autoMode);
            }
        });

        soundModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                soundMode = isChecked;
                soundPool.play(heatSound1, 1, 1, 0, 0, 1);

            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {
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
                checkDistance();

            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void checkDistance() {

        if (currentLocation != null) {
            distance = currentLocation.distanceTo(target);
            range = rangeSlider.getValue();

            if (distance < range) {

                // 400 miliseconds vibration if the user is within range of the target
                Vibrator v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
                v.vibrate(400);
                if (autoMode && range >= 5 && distance >= 5) {

                    range = (float)distance - 5;
                    rangeSlider.setValue(range);

                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        currentLocation = location;

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




