package com.example.android.rueo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.android.rueo.network.NetworkUtils;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import static com.example.android.rueo.network.InputParser.ajaxParser;
import static com.example.android.rueo.network.InputParser.httpParser;

public class MainActivity extends AppCompatActivity
{
    EditText curWord;
    TextView curDictionaryEntry;
    ProgressBar loadingIndicator;
    ScrollView ajaxField;
    LinearLayout ajaxSuggestions;
    boolean searchBarEditDetectorEnabled = true;
    View.OnKeyListener enterDetector = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                startSearch();
            }
            return false;
        }
        private void test (){}
    };
    TextWatcher searchBarEditDetector = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length()>0 && searchBarEditDetectorEnabled)
            {
                getAjax();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    View.OnFocusChangeListener searchBarFocusJumper = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus)
        {
            if (hasFocus)
            {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                switch (v.getId())
                {
                    case R.id.curDictionaryEntry:
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        break;
                    case R.id.curWord:
                        EditText view = (EditText) v;
                        view.setText("");
                        imm.showSoftInput(v, 0);
                        break;
                }
            }
        }
        public void test (){}
    };
    View.OnClickListener ajaxSuggestionClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            TextView cur = (TextView) v;
            searchBarEditDetectorEnabled = false;
            curWord.setText(cur.getText());
            searchBarEditDetectorEnabled = true;
            startSearch();
        }
        public void test (){}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        curDictionaryEntry = (TextView) findViewById(R.id.curDictionaryEntry);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        curWord = (EditText) findViewById(R.id.curWord);
        ajaxField = (ScrollView) findViewById(R.id.ajaxOutputField);
        ajaxSuggestions = (LinearLayout) findViewById(R.id.ajaxSuggestions);
        //обработчик нажатий Энтер:
            curWord.setOnKeyListener(enterDetector);
        //обработчик ввода текста:
            curWord.addTextChangedListener(searchBarEditDetector);
        //обработчики прыжков фокуса со строки поиска (в т.ч. очистка поля поиска при его выделении)
            curWord.setOnFocusChangeListener(searchBarFocusJumper);
            curDictionaryEntry.setOnFocusChangeListener(searchBarFocusJumper);
    }

    //инициирует поиск словарной статьи по содержимому серчбара
    private void startSearch() {
        String searchInput = curWord.getText().toString();
        URL eoURLfull = NetworkUtils.buildUrl(searchInput, "http");
        new httpRetrieveTask().execute(eoURLfull);
    }

    //инициирует аякс-запрос на сайт
    private void getAjax() {
        String searchInput = curWord.getText().toString();
        URL testURL = NetworkUtils.buildUrl(searchInput, "ajax");
        new ajaxRetrieveTask().execute(testURL);
    }

    //в соседнем треде получаем словарную статью, кромсаем ее и выводим
    private class httpRetrieveTask extends AsyncTask <URL, Void, String> {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
            curDictionaryEntry.setText("");
            curDictionaryEntry.setVisibility(View.VISIBLE);
            curDictionaryEntry.requestFocus();
            ajaxField.setVisibility(View.INVISIBLE);
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
                s = httpParser(s);
                //TODO разобраться, почему deprecated
                curDictionaryEntry.setText(Html.fromHtml(s));
            }
            else
            {
                curDictionaryEntry.setText(R.string.network_error);
            }
        }
    }

    //в соседнем треде получаем аякс-ответ, парсим, выводим кликабельные TextView
    private class ajaxRetrieveTask extends AsyncTask <URL, Void, String> {
        @Override
        protected void onPreExecute()
        {
            curDictionaryEntry.setVisibility(View.INVISIBLE);
            loadingIndicator.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... urls)
        {
            URL eoUrl = urls[0];
            String JSONListing = null;
            try
            {
                JSONListing = NetworkUtils.getResponseFromHttpUrl(eoUrl);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return JSONListing;
        }

        @Override
        protected void onPostExecute(String s)
        {
            loadingIndicator.setVisibility(View.INVISIBLE);
            ajaxField.setVisibility(View.VISIBLE);
            if (s != null && !s.equals(""))
            {
                ArrayList<String> ajaxAnswer = ajaxParser(s);
                ajaxSuggestions.removeAllViews();
                for (int i=0; i < ajaxAnswer.size(); i++)
                {
                    TextView suggestion = new TextView(MainActivity.this);
                    suggestion.setText(ajaxAnswer.get(i));
                    suggestion.setOnClickListener(ajaxSuggestionClicked);
                    suggestion.setTextSize(20);
                    suggestion.setPadding(20,0,0,0);
                    ajaxSuggestions.addView(suggestion);
                }
            }
            else
            {
                curDictionaryEntry.setText(R.string.network_error);
            }
        }
        //TODO продумать случай, когда статьи не найдено и предлагают похожее слово (напр. "er")
    }
}
