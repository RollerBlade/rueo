package com.example.android.rueo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

        // //having fun with HTML in TextView:
        //String test = "<h2>Title</h2><br><p>Description here</p>";
        //curDictionaryEntry.setText(Html.fromHtml(test));

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
            loadingIndicator.setVisibility(View.INVISIBLE);
            if (s != null && !s.equals(""))
            {
                //TODO: find out wtf is wrong:
                Document doc = Jsoup.parse(s);
                Element div = doc.select("div.search_result").first();
                s = div.outerHtml();
                curDictionaryEntry.setText(Html.fromHtml(s));
            }
            else
            {
                curDictionaryEntry.setText(R.string.network_error);
            }
        }
    }
}
