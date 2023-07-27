package com.example.weather;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class DataPage extends AppCompatActivity {
    String city;
    String timeZone;
    Handler handler = new Handler();
    int updateTimeMillis = 1000 * 60; // 1 minute interval (in milliseconds)
    TimeCalc timeCalc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_page);
        Intent i = getIntent();
        String cityName = i.getStringExtra("City Name")+"'s Weather";
        city = i.getStringExtra("City Name");
        ((TextView) findViewById(R.id.textView)).setText(cityName);

        ((TextView) findViewById(R.id.textView2)).setText("Want to learn more about "+i.getStringExtra("City Name")+"?");
        fetchWeatherData(city);
        final TextView timeTextView = findViewById(R.id.timeTextView);

        // Create a TimeCalc instance with the initial time from the API response




    }
    public void launchMain(View v){
        ((Button)findViewById(R.id.button2)).setBackgroundColor(Color.WHITE);
        Intent page = new Intent(this , MainActivity.class);
        startActivity(page);
    }

    public void launchWiki(View v){
        Button wiki = ((Button) findViewById(R.id.button3));
        wiki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent wikiPedia =  new Intent(Intent.ACTION_VIEW);
                wikiPedia.setData(Uri.parse("https://en.wikipedia.org/wiki/"+ city));
                startActivity(wikiPedia);

            }
        });
    }
    private void fetchWeatherData(String cityName) {
        String apiKey ="c7cd4713068a4e949ea182633232407";
        String baseUrl = "http://api.weatherapi.com/v1";
        String url = baseUrl + "/current.json?key=" + apiKey + "&q=" + cityName;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Weather Response", response);

                        try {

                            JSONObject jsonResponse = new JSONObject(response);
                            JSONObject currentWeatherObject = jsonResponse.getJSONObject("current");
                            JSONObject timezone = jsonResponse.getJSONObject("location");


                            // Extract the weather information

                            double temperature = currentWeatherObject.getDouble("temp_c");
                            ((TextView) findViewById(R.id.temperatureTextView)).setText(getString(R.string.temperature_format, temperature));

                            String iconUrl = "https:"+currentWeatherObject.getJSONObject("condition").getString("icon");
                            ImageView iconImageView = findViewById(R.id.imageView);
                            Picasso.get().load(iconUrl).into(iconImageView);

                            iconImageView.requestLayout();
                            timeZone = timezone.getString("localtime");
                            timeZone = timezone.getString("localtime");
                            // Extract only the time portion from the "localtime" string
                            String timeOnly = timeZone.substring(timeZone.indexOf(" ") + 1);

                            // Create a TimeCalc instance with the extracted time
                            timeCalc = new TimeCalc(timeOnly);

                            // Update the timeTextView with the initial time
                            String formattedTime = timeCalc.getCurrentTime();
                            ((TextView) findViewById(R.id.timeTextView)).setText(formattedTime);

                            // Schedule the time update using a Handler
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Increment the time and update the timeTextView
                                    timeCalc.incrementTime();
                                    String formattedTime = timeCalc.getCurrentTime();
                                    ((TextView) findViewById(R.id.timeTextView)).setText(formattedTime);

                                    // Schedule the next update after updateTimeMillis milliseconds
                                    handler.postDelayed(this, updateTimeMillis);
                                }
                            }, updateTimeMillis);





                        } catch (JSONException e) {
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d("Dumb", "Error");
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String errorMessage = new String(error.networkResponse.data);
                            Log.e("API Error", "Status Code: " + statusCode);
                            Log.e("API Error", "Error Message: " + errorMessage);
                        } else {
                            Log.e("API Error", "Unknown Error");
                        }

                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);




    }


}
class  TimeCalc {
    private int hour;
    private int minute;

    public TimeCalc(String timeString) {
        // Parse the input timeString into hours and minutes

        if (timeString.length() == 5) {
            hour = Integer.parseInt(timeString.substring(0,2));
            minute = Integer.parseInt(timeString.substring(3,5));
        }else if(timeString.length() ==4){
            hour = Integer.parseInt(timeString.substring(0,1));
            minute = Integer.parseInt(timeString.substring(2,4));
        }
        else {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }
    }

    public String getCurrentTime() {
        if(hour < 12){
            return String.format(Locale.getDefault(), "%02d:%02d am", hour, minute);
        }
        return String.format(Locale.getDefault(), "%02d:%02d pm", hour, minute);
    }

    public void incrementTime() {
        // Increment the minute part and handle overflow
        minute++;
        if (minute >= 60) {
            minute = 0;
            incrementHour();
        }
    }

    private void incrementHour() {
        // Increment the hour part and handle overflow
        hour++;
        if (hour >= 24) {
            hour = 0; // Reset to 0 when it becomes 24 (midnight)
        }
    }
}

