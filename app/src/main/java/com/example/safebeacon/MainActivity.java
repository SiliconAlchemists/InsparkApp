package com.example.safebeacon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;


public class MainActivity extends AppCompatActivity {



//    public void openNav(View view){
//        Intent i = new Intent(getApplicationContext(),Nav.class);
//        startActivity(i);
//    }

    public void openLookout(View view){
        Intent i = new Intent(getApplicationContext(),Lookout.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
