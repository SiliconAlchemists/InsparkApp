package com.example.safebeacon;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class Lookout extends AppCompatActivity implements SensorEventListener {

    SensorManager sensorManager;
    Sensor sensor;

    WebSocket ws;

    Location currLocation;

    FusedLocationProviderClient fusedLocationClient;


    float deltaX;
    float deltaY;
    float deltaZ;

    public OkHttpClient client;

    @Override
    public void onSensorChanged(SensorEvent event) {

        deltaX = event.values[0];
        deltaY = event.values[1];
        deltaZ = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(final WebSocket webSocket, Response response) {
            JSONObject rObj = new JSONObject();
            try {
                rObj.put("action", "start");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(rObj.toString());
            Log.d("SocketVal", "start boi");

        }

        @Override
        public void onMessage(final WebSocket webSocket, String text) {

            JSONObject request = null;
            try {
                request = new JSONObject(text);
                if(request.getString("action").equals("gyro")){
                    JSONObject rObj = new JSONObject();
                    try {
                        rObj.put("action", "gyro");
                        rObj.put("x", deltaX);
                        rObj.put("y", deltaY);
                        rObj.put("z", deltaZ);
//                        rObj.put("latitude", currLocation.getLatitude());
//                        rObj.put("longitude", currLocation.getLongitude());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    webSocket.send(rObj.toString());
                } else if(request.getString("action").equals("location")) {
                    JSONObject rObj = new JSONObject();
                    try {
                        rObj.put("action", "location");
                        rObj.put("latitude", currLocation.getLatitude());
                        rObj.put("longitude", currLocation.getLongitude());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    webSocket.send(rObj.toString());
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("SocketVal", "error boi");

            }






        }


        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Toast.makeText(getApplicationContext(), "Conn closed", Toast.LENGTH_SHORT).show();
            Log.d("SocketVal", "here5");

        }

//        @Override
//        public void onMessage(WebSocket webSocket, ByteString bytes) {
//            Toast.makeText(getApplicationContext(), "nigger", Toast.LENGTH_SHORT).show();
//        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
//            output("Closing : " + code + " / " + reason);
            Log.d("SocketVal", "here6");

        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
//            output("Error : " + t.getMessage());
        }
    }

    private void start() {
//        Request request = new Request.Builder().url("ws://192.168.31.239:3006/lookout").build();
        Request request = new Request.Builder().url("wss://salty-brushlands-62683.herokuapp.com/lookout").build();

        Lookout.EchoWebSocketListener listener = new Lookout.EchoWebSocketListener();
        ws = client.newWebSocket(request, listener);
        client.dispatcher().executorService().shutdown();
    }


    public void sendButtonCall(View view) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("action", "feedback");
            obj.put("latitude", currLocation.getLatitude());
            obj.put("longitude", currLocation.getLongitude());
            obj.put("type", view.getTag());
            ws.send(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    WebView browser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lookout);

        browser = findViewById(R.id.webView);
//
////        browser.
//
        browser.setWebViewClient(new WebViewClient());
        browser.getSettings().setJavaScriptEnabled(true);
        browser.loadUrl("file:///android_asset/index.html");
//        browser.loadUrl("https://guarded-crag-75807.herokuapp.com");
//        browser.loadUrl("http://192.168.31.239:5000");
//        deltaX = 2.0f;

        client = new OkHttpClient();
        start();


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);


        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currLocation = location;
                            Toast.makeText(getApplicationContext(), String.valueOf(currLocation.getLatitude()), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}
