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
    final static private String ERROR_NO_SUCH_WORD_FOUND = "Подходящей словарной статьи не найдено.";

    EditText curWord;
    ProgressBar loadingIndicator;
    LinearLayout outputField;
    boolean searchBarEditDetectorEnabled = true;
    boolean curWordShouldBeErased = false;
    View.OnKeyListener enterDetector = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                startSearch();
            }
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && (keyCode == KeyEvent.KEYCODE_DEL)) {
                curWordShouldBeErased = false;
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
                if (curWordShouldBeErased)
                {
                    curWordShouldBeErased = false;
                    searchBarEditDetectorEnabled = false;
                    curWord.setText(s.toString().substring(start, start+count));
                    curWord.setSelection(curWord.getText().length());
                    searchBarEditDetectorEnabled = true;
                }
                getAjax();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    View.OnClickListener ajaxSuggestionClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            TextView cur = (TextView) v;
            searchBarEditDetectorEnabled = false;
            curWord.setText(cur.getText());
            curWord.setSelection(curWord.getText().length());
            searchBarEditDetectorEnabled = true;
            startSearch();
        }
        public void test (){}
    };
    TextView.OnClickListener CurWordOnclickFlagDisabler = new TextView.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            curWordShouldBeErased = false;
        }
    };
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        curWord = (EditText) findViewById(R.id.curWord);
        outputField = (LinearLayout) findViewById(R.id.outputField);
        //обработчик нажатий Энтер:
            curWord.setOnKeyListener(enterDetector);
        //обработчик ввода текста:
            curWord.addTextChangedListener(searchBarEditDetector);
        //обработчик тапов в поле ввода
            curWord.setOnClickListener(CurWordOnclickFlagDisabler);

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
            outputField.removeAllViews();
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
            TextView searchOutput = new TextView(MainActivity.this);
            searchOutput.setPadding(20,20,20,20);
            outputField.addView(searchOutput);
            if (s != null && !s.equals(""))
            {
                s = httpParser(s);
                searchOutput.setText(Html.fromHtml(s));
            }
            else
            {
                searchOutput.setText(R.string.network_error);
            }
            curWordShouldBeErased = true;
        }
    }

    //в соседнем треде получаем аякс-ответ, парсим, выводим кликабельные TextView
    private class ajaxRetrieveTask extends AsyncTask <URL, Void, String> {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            outputField.removeAllViews();
            loadingIndicator.setVisibility(View.VISIBLE);
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
            if (s != null && !s.equals(""))
            {
                ArrayList<String> ajaxAnswer = ajaxParser(s);
                for (int i=0; i < ajaxAnswer.size(); i++)
                {
                    TextView suggestion = new TextView(MainActivity.this);
                    suggestion.setText(ajaxAnswer.get(i));
                    suggestion.setOnClickListener(ajaxSuggestionClicked);
                    suggestion.setTextSize(20);
                    suggestion.setPadding(20,0,0,0);

                    outputField.addView(suggestion);
                }
            }
            else
            {
                TextView error = new TextView(MainActivity.this);
                error.setText(R.string.network_error);
                outputField.addView(error);
            }
        }
        //TODO продумать случай, когда статьи не найдено и предлагают похожее слово (напр. "er")
         //TODO (1) поправить автостирание при новом вводе
            //запилить флаг
            //обработка бэкспейс
            //обработка ввода буквы
            //обработка выделения слова в серчбаре(onclick по серчбару)
            //обработка флага при запуске серч
        //TODO (2) тыкание в слово в статье (два случая - ссылка и просто слово)
        //TODO (3) добавить обработку случая, когда поиск не нашел статьи: вывалить аякс, если аякс - 0, вываодит что-то.
                //добавить заголовок к аякс - выводу
                //добавить заголовок к серч методу
        //TODO (4) поправить цвет шрифта и фона в серчбаре
        //TODO (5) продумать работу аякс при нулевом выводе (мб отбросить три последних буквы слова из серчбар)
    }
}