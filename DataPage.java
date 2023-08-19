package com.example.weather;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

    boolean fav = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_page);
        Intent i = getIntent();
        String cityName = i.getStringExtra("City Name")+"'s Weather";
        city = Title(i.getStringExtra("City Name"));
        ((TextView) findViewById(R.id.textView)).setText(cityName);

        ((TextView) findViewById(R.id.textView2)).setText("Want to learn more about "+i.getStringExtra("City Name")+"?");
        fetchWeatherData(city);

    }
    public void onFavoriteButtonClicked(View view) {
        fav = !fav;
        ImageButton favoriteButton = findViewById(R.id.imageButton3);

        SharedPreferences favCityName = getSharedPreferences("FavoriteCities", MODE_PRIVATE);
        SharedPreferences.Editor editor = favCityName.edit();

        if (favCityName.contains("FavouriteCity1") && favCityName.getString("FavouriteCity1", "").equals(city)) {
            editor.remove("FavouriteCity1");
        } else if (favCityName.contains("FavouriteCity2") && favCityName.getString("FavouriteCity2", "").equals(city)) {
            editor.remove("FavouriteCity2");
        } else if (favCityName.contains("FavouriteCity3") && favCityName.getString("FavouriteCity3", "").equals(city)) {
            editor.remove("FavouriteCity3");
        } else {

            if (!favCityName.contains("FavouriteCity1")) {
                editor.putString("FavouriteCity1", city);
            } else if (!favCityName.contains("FavouriteCity2")) {
                editor.putString("FavouriteCity2", city);
            } else if (!favCityName.contains("FavouriteCity3")) {
                editor.putString("FavouriteCity3", city);
            }
        }
        editor.apply();


    }

    public void launchMain(View v){
        ((Button)findViewById(R.id.button2)).setBackgroundColor(Color.WHITE);
        Intent page = new Intent(this , MainActivity.class);
        page.putExtra("bool Value", fav);
        if(fav){
            page.putExtra("favoriteCity", city);


            Log.d("done","City imported");
        }
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
        String apiKey ="apikey";
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
                            // Extract only the time portion from the "localtime" string
                            String timeOnly = timeZone.substring(timeZone.indexOf(" ") + 1);

                            // Create a TimeCalc instance with the extracted time
                            timeCalc = new TimeCalc(timeOnly);
                            String formattedTime = timeCalc.getCurrentTime();
                            ((TextView) findViewById(R.id.timeTextView)).setText(formattedTime);
                            fetchWikipediaContent(cityName);


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
    private void fetchWikipediaContent(String cityName) {
        String apiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&redirects=true&titles=" + cityName;

        StringRequest request = new StringRequest(Request.Method.GET, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONObject pagesObject = jsonResponse.getJSONObject("query").getJSONObject("pages");
                            String pageId = pagesObject.keys().next();
                            String extract = pagesObject.getJSONObject(pageId).getString("extract");
                            extract = android.text.Html.fromHtml(extract).toString();
                            if (extract.length() > 330) {
                                extract = extract.substring(0, 530) + "...";
                            }
                            ((TextView) findViewById(R.id.wikiInfotextView)).setText(extract);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle JSON parsing error
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
    private String Title(String city) {
        if (city.isEmpty()) {
            return city;
        }
        StringBuilder formattedTitle = new StringBuilder();
        String[] words = city.split(" ");
        for (String word : words) {
            if (!word.isEmpty()) {
                formattedTitle.append(Character.toUpperCase(word.charAt(0)));
                formattedTitle.append(word.substring(1).toLowerCase());
                formattedTitle.append(" ");
            }
        }
        return formattedTitle.toString().trim();
    }



}



