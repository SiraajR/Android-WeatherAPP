package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void data(View v){
        String cityName = ((TextView) findViewById(R.id.editTextText)).getText().toString();
        Intent i = new Intent(this , DataPage.class);
        i.putExtra("City Name" , cityName);
        startActivity(i);

    }
}
