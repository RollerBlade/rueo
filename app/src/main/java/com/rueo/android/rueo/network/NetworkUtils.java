package com.rueo.android.rueo.network;

import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils
{
    final static private String RUEO_HTTP_URL = "http://rueo.ru/sercxo/";
    final static private String AJAX_PARAM = "ajax";
    final static private String AJAX_PARAM_TERM = "term";

    /*
    Составитель корректных URL с блекджеком и кириллицей
    На входе - искомое слово и тип желаемого УРЛ (хттп или аякс)
    На выходе - готовый URL
    */
    public static URL buildUrl(String word, String type) {
        Uri builtUri = null;
        switch (type)
        {
            case "http":
                builtUri = Uri.parse(RUEO_HTTP_URL).buildUpon().appendPath(word).build();
                break;
            case "ajax":
                builtUri = Uri.parse(RUEO_HTTP_URL).buildUpon()
                        .appendQueryParameter(AJAX_PARAM, "")
                        .appendQueryParameter(AJAX_PARAM_TERM, word).build();
                break;
        }
        URL url = null;
        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return url;
    }

    /*
    Метод возвращает содержимое http запроса по URL
    */
    public static String getResponseFromHttpUrl(URL url) throws IOException
    {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try
        {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput)
            {
                return scanner.next();
            }
            else
            {
                return null;
            }
        }
        finally
        {
            urlConnection.disconnect();
        }
    }
}
