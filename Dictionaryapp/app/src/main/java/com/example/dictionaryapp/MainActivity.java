package com.example.dictionaryapp;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {

    TextView textView;
    TextView resusltTxt;
    SearchView searchView;
    private static  final String  url0 = "http://api.wordnik.com:80/v4/word.json/";
    private static final String url1 = "/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key=54d58ad1400c9bc39d59b4c5dae05829cb6d4086bcfe2c886";
    int duration = Toast.LENGTH_SHORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.textView);
        resusltTxt = (TextView) findViewById(R.id.meaning);

        // Get the SearchView and set the searchable configuration
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) findViewById(R.id.searchView);
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        //searchView.setOnSearchClickListener();
        EditText txtSearch = ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint("Search a word");
        txtSearch.setHintTextColor(Color.LTGRAY);
        txtSearch.setTextColor(Color.WHITE);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(txtSearch, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                textView.setText(query);
                GetWordMeanTask getWordMeanTask = new GetWordMeanTask();
                getWordMeanTask.execute(query.trim().toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }


    public class GetWordMeanTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = GetWordMeanTask.class.getSimpleName();

        private String getWordMeaningFromJson(String JsonStr)
                throws JSONException {

            final String WORD_MEANING = "text";
            String resultStr = "No word found.";
            JSONArray jsonArray = new JSONArray(JsonStr);
            if(jsonArray.length() > 0) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                resultStr = jsonObject.getString(WORD_MEANING);
            }

            Log.v(LOG_TAG, "Word meaning: " + resultStr);

            return resultStr;

        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;

            try {

                final String BASE_URL1 = "http://api.wordnik.com:80/v4/word.json/";
                final String BASE_URL2 = "/definitions?limit=200&includeRelated=true&useCanonical=false&includeTags=false&api_key=54d58ad1400c9bc39d59b4c5dae05829cb6d4086bcfe2c886";
                final String BASE_URL = BASE_URL1 + params[0] + BASE_URL2;

                URL url = new URL(BASE_URL);

                Log.v(LOG_TAG, "URi"+ BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                JsonStr = buffer.toString();

                Log.v(LOG_TAG,"JSON String: "+ JsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getWordMeaningFromJson(JsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            if(string != null){
                resusltTxt.setText(string);
            }

        }
    }

}
