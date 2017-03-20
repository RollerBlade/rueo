package com.example.android.rueo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.rueo.network.NetworkUtils;

import java.io.EOFException;
import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
{
    EditText curWord;
    TextView curDictionaryEntry;
    Button startSearch;
    ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curWord = (EditText) findViewById(R.id.curWord);
        curDictionaryEntry = (TextView) findViewById(R.id.curDictionaryEntry);
        startSearch = (Button) findViewById(R.id.startSearch);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        Toast.makeText(this, curDictionaryEntry.getText(), Toast.LENGTH_LONG).show();

    }


    public void startSearchOnButtonClick(View view)
    {
        String searchInput = curWord.getText().toString();
        URL eoURLfull = NetworkUtils.buildUrl(searchInput);
        new httpRetrieveTask().execute(eoURLfull);
    }

    public class httpRetrieveTask extends AsyncTask<URL, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls)
        {
            URL eoUrl = urls[0];
            String httpListing = null;
            try
            {
                httpListing = NetworkUtils.getResponseFromHttpUrl(eoUrl);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return httpListing;
        }

        @Override
        protected void onPostExecute(String s)
        {
            loadingIndicator.setVisibility(View.VISIBLE);
            if (s != null && !s.equals(""))
            {
                curDictionaryEntry.setText(s);
            }
            else
            {
                curDictionaryEntry.setText(R.string.network_error);
            }
        }
    }
}
