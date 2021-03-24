package com.example.picsearch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity implements LocationListener {

    ImageView targetImageView;
    ImageView sourceImageView;
    Slider rangeSlider;
    Switch autoModeSwitch;
    Switch soundModeSwitch;
    Button foundButton;
    Button importButton;

    private SoundPool soundPool;
    private int heatSound0, heatSound1, heatSound2, heatSound3;

    boolean soundMode = false;
    boolean autoMode = false;

    LocationManager locationManager;
    Location currentLocation = null;
    double distance = 0;
    double previousDistance = 0;
    Handler handler = new Handler();
    Runnable runnable;
    Location target = new Location("");

    float range = 0;

    //Nombre de miliseconde entre chaque refresh
    int delay = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targetImageView = findViewById(R.id.target);
        sourceImageView = findViewById(R.id.source);
        foundButton = findViewById(R.id.button);
        importButton = findViewById(R.id.importButton);
        autoModeSwitch = findViewById(R.id.autoModeSwitch);
        soundModeSwitch = findViewById(R.id.soundModeSwitch);


        // Soundpool creation
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();

        heatSound0 = soundPool.load(this, R.raw.cold, 1);
        heatSound1 = soundPool.load(this, R.raw.neutral, 1);
        heatSound2 = soundPool.load(this, R.raw.hot, 1);
        heatSound3 = soundPool.load(this, R.raw.veryhot, 1);

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
            }
        });

        //request for camera runtime permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);

        }


        rangeSlider = findViewById(R.id.rangeSlider);

        // Target location (TEST)
        target.setLatitude(45.7556805);
        target.setLongitude(3.1073695);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, 100);

        }

        foundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivityForResult(intent, 200);
            }
        });


    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            sourceImageView.setImageBitmap(bitmap);

//            @SuppressLint("MissingPermission") Location curr = ((LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            float distance = curr.distanceTo(target);
//            Log.d("LOL", curr + " - " + distance + " - " + target);
            if (distance < 5) {
                Toast.makeText(this, "Bravo vous avez trouvé hahahaha", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 200) {
            try {
                InputStream stream = getContentResolver().openInputStream(data.getData());
                ExifInterface exif = new ExifInterface(stream);

                double[] latLong = exif.getLatLong();
                Log.d("Main", Arrays.toString(latLong));
                target.setLatitude(latLong[0]);
                target.setLongitude(latLong[1]);
                targetImageView.setImageURI(data.getData());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            previousDistance = distance;
            distance = currentLocation.distanceTo(target);
            range = rangeSlider.getValue();

            if (distance < range) {

                // 400 miliseconds vibration if the user is within range of the target
                if (!soundMode) {
                    Vibrator v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
                    v.vibrate(400);
                    soundPool.play(heatSound2, 1, 1, 0, 0, 1);
                }
                else {
                    if (distance <= previousDistance) {
                        soundPool.play(heatSound2, 1, 1, 0, 0, 1);
                    }
                    else {
                        soundPool.play(heatSound0, 1, 1, 0, 0, 1);
                    }
                }

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