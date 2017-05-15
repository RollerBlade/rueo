package com.example.android.rueo;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import static com.example.android.rueo.network.InputParser.ajaxParser;
import static com.example.android.rueo.network.InputParser.httpParser;
import com.example.android.rueo.history.Stack;
import com.example.android.rueo.network.NetworkUtils;

public class MainActivity extends AppCompatActivity
{
    //серчбар
        EditText curWord;
        String curWordStr;
    //поле для вывода статей и подсказок
        LinearLayout outputField;
    //кнопки управления справа от серчбара
        ImageView leftBtn, rightBtn, downBtn, settingsBtn;
    //для истории в правом дровере
        ArrayAdapter<String> historyAdapter;
    //дроверы
        private DrawerLayout mDrawerLayout;
        private ListView rightDrawer;
        private ScrollView leftDrawer;
    long timestamp;
    ProgressBar loadingIndicator;
    httpRetrieveTask hrt = null;
    ajaxRetrieveTask art = null;
    int curWordStrShifter = 0;
    boolean searchBarEditDetectorEnabled = true;
    boolean curWordShouldBeErased = false;
    Stack searchBarStack = new Stack();



    View.OnKeyListener enterDetector = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && (keyCode == KeyEvent.KEYCODE_ENTER))
            {
                startHttpRetrieveTask(curWord.getText().toString());
                searchBarStack.push(curWord.getText().toString());
                listInflator(rightDrawer, searchBarStack.getStringArray());
            }
            return false;
        }
        private void test (){}
    };
    TextWatcher searchBarEditDetector = new TextWatcher()
    {
        String textBefore = null, textAfter = null;
        private int mPreviousLength;
        private boolean mBackSpace;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            textBefore = s.toString();
            mPreviousLength = s.length();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            textAfter = s.toString();
            if (s.length()>0 && searchBarEditDetectorEnabled)
            {
                //костыль обработки бэкспейс
                mBackSpace = mPreviousLength == (s.length()+1);
                if (mBackSpace)
                {
                    isCurWordShouldBeErased(false);
                }
                //очищаем серчбар, если надо
                if (curWordShouldBeErased)
                {
                    isCurWordShouldBeErased(false);
                    searchBarEditDetectorEnabled = false;
                    //curWord.setText(s.toString().substring(start, start+count));
                    //костыль для телефона СС, который не умеет правильно считать =)
                        int i;
                        for (i = 0; i < textBefore.length() && s.charAt(i)==textBefore.charAt(i); i++);
                        curWord.setText(s.subSequence(i,i+1).toString());
                    curWord.setSelection(curWord.getText().length());
                    searchBarEditDetectorEnabled = true;
                }
                startAjaxRetrieveTask(curWord.getText().toString());
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            if (s.length()==0)
            {
                outputField.removeAllViews();
            }
            curWordStr = curWord.getText().toString();
        }
    };
    TextView.OnTouchListener CurWordOnclickFlagDisabler = new TextView.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent a)
        {
            isCurWordShouldBeErased(false);
            return false;
        }
        private void test (){}
    };
    ImageView.OnTouchListener buttonOnClickImgSwapper = new ImageView.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            ImageView img = (ImageView) v;

            switch (img.getId())
            {
                case R.id.settings:
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        img.setImageResource(R.drawable.ic_settings_pressed);
                        mDrawerLayout.openDrawer(leftDrawer);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        img.setImageResource(R.drawable.ic_settings_unpressed);
                    }
                    return true;
                case R.id.arrowLeft:
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        img.setImageResource(R.drawable.ic_down_arrow_pressed);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        img.setImageResource(R.drawable.ic_down_arrow_unpressed);
                        String temp = searchBarStack.getLeft();
                        if (temp != null)
                        {
                            searchBarEditDetectorEnabled = false;
                            curWord.setText(temp);
                            startHttpRetrieveTask(temp);
                            searchBarEditDetectorEnabled = true;
                        }
                    }
                    return true;
                case R.id.arrowRight:
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        img.setImageResource(R.drawable.ic_down_arrow_pressed);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        img.setImageResource(R.drawable.ic_down_arrow_unpressed);
                        String temp = searchBarStack.getRight();
                        if (temp != null)
                        {
                            searchBarEditDetectorEnabled = false;
                            curWord.setText(temp);
                            startHttpRetrieveTask(temp);
                            searchBarEditDetectorEnabled = true;
                        }
                    }
                    return true;
                case R.id.arrowDown:
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        img.setImageResource(R.drawable.ic_history_pressed);
                        mDrawerLayout.openDrawer(rightDrawer);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP)
                    {
                        img.setImageResource(R.drawable.ic_history_unpressed);
                    }
                    return true;
                default:
                    return true;
            }
            //return false;
        }
        private void test (){}
    };
    ListView.OnItemClickListener suggestionClicked = new ListView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            TextView cur = (TextView) view;
            searchBarEditDetectorEnabled = false;
            curWord.setText(cur.getText());
            curWord.setSelection(curWord.getText().length());
            searchBarEditDetectorEnabled = true;
            startHttpRetrieveTask(curWord.getText().toString());
            searchBarStack.push(curWord.getText().toString());
            listInflator(rightDrawer, searchBarStack.getStringArray());
            mDrawerLayout.closeDrawer(rightDrawer);
        }
        public void test (){}
    };

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        timestamp = System.currentTimeMillis()/1000;;
        setContentView(R.layout.activity_main);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        curWord = (EditText) findViewById(R.id.curWord);
        outputField = (LinearLayout) findViewById(R.id.outputField);
        //обработчик нажатий Энтер:
            curWord.setOnKeyListener(enterDetector);
        //обработчик ввода текста:
            curWord.addTextChangedListener(searchBarEditDetector);
        //обработчик тапов в поле ввода
            curWord.setOnTouchListener(CurWordOnclickFlagDisabler);
        //переключатель картинок на кнопках
            leftBtn = (ImageView) findViewById(R.id.arrowLeft);
            rightBtn = (ImageView) findViewById(R.id.arrowRight);
            downBtn = (ImageView) findViewById(R.id.arrowDown);
            settingsBtn = (ImageView) findViewById(R.id.settings);
            leftBtn.setOnTouchListener(buttonOnClickImgSwapper);
            rightBtn.setOnTouchListener(buttonOnClickImgSwapper);
            downBtn.setOnTouchListener(buttonOnClickImgSwapper);
            settingsBtn.setOnTouchListener(buttonOnClickImgSwapper);
        //дроверы
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            leftDrawer = (ScrollView) findViewById(R.id.left_drawer);
            rightDrawer = (ListView) findViewById(R.id.right_drawer);
            historyAdapter = new ArrayAdapter<String>
                    (this, R.layout.drawer_list_item, searchBarStack.getListArray());
            rightDrawer.setAdapter(historyAdapter);
            rightDrawer.setOnItemClickListener(suggestionClicked);
            LayoutInflater inflater = getLayoutInflater();
            View historyHeader = inflater.inflate(R.layout.drawer_header_item, null, false);
            rightDrawer.addHeaderView(historyHeader, null, false);
            View historyFooter = inflater.inflate(R.layout.drawer_footer_item, null, false);
            rightDrawer.addFooterView(historyFooter, null, false);
    }

    public void clearHistory (View v)
    {
        searchBarStack.clear();
        listInflator(rightDrawer, searchBarStack.getStringArray());
    }

    public void closeDrawers (View v)
    {
        mDrawerLayout.closeDrawer(rightDrawer);
        mDrawerLayout.closeDrawer(leftDrawer);

    }

    public void sendEmail (View v)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        switch (v.getId())
        {
            case (R.id.emailToAuthorTV):
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"vortaristo@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "RUEO онлайн словарь");
                if (!curWord.getText().toString().isEmpty())
                {
                    i.putExtra(Intent.EXTRA_TEXT,
                            "Статья: \"" + curWord.getText().toString()+
                            "\"\nСсылка: http://rueo.ru/sercxo/" + curWord.getText().toString());
                }
                break;
            case (R.id.emailToDeveloperTV):
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"k002422@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "RUEO app");
                break;
        }
        try
        {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            String temp = searchBarStack.getLeft();
            if (temp != null)
            {
                searchBarEditDetectorEnabled = false;
                curWord.setText(temp);
                startHttpRetrieveTask(temp);
                searchBarEditDetectorEnabled = true;
            }
            else
            {
                if ((System.currentTimeMillis()/1000 - timestamp) < 2)
                {
                    System.exit(0);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Press back again in 2 sec to exit app", Toast.LENGTH_LONG).show();
                    timestamp = System.currentTimeMillis()/1000;
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean isCurWordShouldBeErased(boolean flag)
    {
        curWordShouldBeErased = flag;
        Log.d("ololo", "flag is " + curWordShouldBeErased);
        return curWordShouldBeErased;
    }

    private void listInflator (ListView myList, String[] array)
    {
        historyAdapter.clear();
        historyAdapter.addAll(searchBarStack.getListArray());
    }

    private void startHttpRetrieveTask (String input)
    {
        curWord.setSelection(curWord.getText().length());
        if (hrt != null)
        {
            hrt.cancel(false);
        }
        if (art != null)
        {
            art.cancel(false);
        }
        hrt = new httpRetrieveTask();
        hrt.execute(input);
    }

    private void startAjaxRetrieveTask (String input)
    {
        if (hrt != null)
        {
            hrt.cancel(false);
        }
        if (art != null)
        {
            art.cancel(false);
        }
        art = new ajaxRetrieveTask();
        art.execute(input);
    }

    //в соседнем треде получаем словарную статью, кромсаем ее и выводим
    private class httpRetrieveTask extends AsyncTask <String, Void, String> {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
            outputField.removeAllViews();
        }

        @Override
        protected String doInBackground(String... urls)
        {
            URL eoUrl = NetworkUtils.buildUrl(urls[0], "http");
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
            ScrollView searchOutputWrapper = new ScrollView(MainActivity.this);
            TextView searchOutput = new TextView(MainActivity.this);
            searchOutput.setPadding(20,20,20,20);
            searchOutputWrapper.addView(searchOutput);
            outputField.addView(searchOutputWrapper);
            if (s != null && !s.equals(""))
            {
                s = httpParser(s);
                searchOutput.setText(Html.fromHtml(s).toString());
            }
            else
            {
                searchOutput.setText(R.string.network_error);
            }
            isCurWordShouldBeErased(true);
        }
    }

    //в соседнем треде получаем аякс-ответ, парсим, выводим кликабельные TextView
    private class ajaxRetrieveTask extends AsyncTask <String, Void, String> {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            outputField.removeAllViews();
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls)
        {
            URL eoUrl = NetworkUtils.buildUrl(urls[0], "ajax");
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
                TextView title = new TextView(MainActivity.this);
                title.setTypeface(null, Typeface.BOLD);
                title.setTextSize(20);
                title.setPadding(20, 0, 0, 0);
                if (ajaxAnswer.size()>0)
                {
                    if (curWordStrShifter>0)
                    {
                        title.setText("Ошибка ввода!\nВозможные совпадения:");
                        //outputField.addView(title);
                    }
                    else
                    {
                        title.setText("Возможные совпадения:");
                        //outputField.addView(title);
                    }
                    if (curWordStrShifter == 0)
                        curWord.setTextColor(getResources().getColor(R.color.textColor));
                    curWordStrShifter = 0;

                }
                else
                {
                    if (curWord.length()>1 && curWord.length() > curWordStrShifter)
                    {
                        curWordStrShifter++;
                        curWord.setTextColor(getResources().getColor(R.color.searchbarTextErrorColor));
                        startAjaxRetrieveTask(curWord.getText().toString()
                                .substring(0, curWord.length() - curWordStrShifter));
                        //title.setText("Ошибка ввода!");
                    }
                    else
                    {
                        title.setText("Совпадений не найдено!");
                        //curWord.setTextColor(getResources().getColor(R.color.searchbarTextErrorColor));
                        //outputField.addView(title);
                    }
                }

                ListView ajaxOutput = new ListView(MainActivity.this);
                ArrayAdapter<String> ajaxAdapter = new ArrayAdapter<String>
                    (MainActivity.this,R.layout.ajax_list_item, ajaxAnswer);
                ajaxOutput.setAdapter(ajaxAdapter);
                ajaxOutput.setOnItemClickListener(suggestionClicked);
                outputField.addView(ajaxOutput);
            }
            else
            {
                TextView error = new TextView(MainActivity.this);
                error.setText(R.string.network_error);
                outputField.addView(error);
            }
        }
    }


    //TODO (1) продумать случай, когда статьи не найдено и предлагают похожее слово (напр. "er")
    //TODO (2) тыкание в слово в статье (два случая - ссылка и просто слово)
    //TODO (3) добавить обработку случая, когда поиск не нашел статьи: вывалить аякс, если аякс - 0, вываодит что-то.
}