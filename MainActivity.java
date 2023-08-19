package com.example.weather;



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {
    String cityName;
    String timeZone;
    Handler handler = new Handler();
    int updateTimeMillis = 1000 * 60; // 1 minute interval (in milliseconds)
    TimeCalc timeCalc;
    SharedPreferences favCityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = getIntent();
        favCityName = getSharedPreferences("FavoriteCities", MODE_PRIVATE);
        String favoriteCity1 = Title(favCityName.getString("FavouriteCity1", ""));
        String favoriteCity2 = Title(favCityName.getString("FavouriteCity2", ""));
        String favoriteCity3 = Title(favCityName.getString("FavouriteCity3", ""));
        if(!favoriteCity1.isEmpty()){
            fetchWeatherData(favoriteCity1 ,1);
        }
        if(!favoriteCity2.isEmpty()){
            fetchWeatherData(favoriteCity2,2);
        }
        if(!favoriteCity3.isEmpty()){
            fetchWeatherData(favoriteCity3,3);
        }
    }


    public void data(View v) {
        String cityName = ((TextView) findViewById(R.id.editTextText)).getText().toString().toLowerCase();
        if(cityName.isEmpty()){
            Toast.makeText(this , "Enter a city name" , Toast.LENGTH_LONG).show();
        }else{
            Intent i = new Intent(this, DataPage.class);
            i.putExtra("City Name", cityName);
            startActivity(i);
        }

    }

    private void fetchWeatherData(String cityName, int sequelNum) {
        String apiKey = "c7cd4713068a4e949ea182633232407";
        String baseUrl = "http://api.weatherapi.com/v1";
        String url = baseUrl + "/current.json?key=" + apiKey + "&q=" + cityName;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Weather", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONObject currentWeatherObject = jsonResponse.getJSONObject("current");
                            JSONObject timezone = jsonResponse.getJSONObject("location");

                            String idCity = "CityName"+sequelNum;
                            int resourceID= getResources().getIdentifier(idCity, "id", getPackageName());
                            ((TextView) findViewById(resourceID)).setText(cityName);

                            // Extract the weather information
                            double temperature = currentWeatherObject.getDouble("temp_c");
                            String Idtemp = "Temp"+sequelNum;
                            int tempId = getResources().getIdentifier(Idtemp, "id", getPackageName());
                            ((TextView) findViewById(tempId)).setText(getString(R.string.temperature_format, temperature));

                            String iconUrl = "https:" + currentWeatherObject.getJSONObject("condition").getString("icon");
                            String IdImage = "Image"+ sequelNum;
                            int ImageId = getResources().getIdentifier(IdImage, "id", getPackageName());
                            ImageView iconImageView = findViewById(ImageId);

                            Picasso.get().load(iconUrl).into(iconImageView);
                            iconImageView.requestLayout();

                            timeZone = timezone.getString("localtime");
                            String timeOnly = timeZone.substring(timeZone.indexOf(" ") + 1);
                            timeCalc = new TimeCalc(timeOnly);
                            String formattedTime = timeCalc.getCurrentTime();
                            String IdTime = "Time"+ sequelNum;
                            int TimeId = getResources().getIdentifier(IdTime,"id", getPackageName());
                            ((TextView) findViewById(TimeId)).setText(formattedTime);

                            // Schedule the time update using a Handler
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Increment the time and update the timeTextView
                                    timeCalc.incrementTime();
                                    String formattedTime = timeCalc.getCurrentTime();
                                    ((TextView) findViewById(TimeId)).setText(formattedTime);

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

                        Log.d("Dum", "Error");
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
    private void startDataPage(String cityName) {
        Intent intent = new Intent(this, DataPage.class);
        intent.putExtra("City Name", cityName);
        startActivity(intent);
    }
    public void onFirstSectionClick(View view) {
        String city = Title(favCityName.getString("FavouriteCity1", ""));
        startDataPage(city);
    }

    public void onSecondSectionClick(View view) {
        String city = Title(favCityName.getString("FavouriteCity2", ""));
        startDataPage(city);
    }

    public void onThirdSectionClick(View view) {
        String city = Title(favCityName.getString("FavouriteCity3", ""));
        startDataPage(city);
    }

}

