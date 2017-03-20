package com.example.android.rueo.network;

import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils
{
    final static String RUEO_BASE_URL = "http://rueo.ru/sercxo/";

    /*
    Составитель корректных URL с блекджеком и кириллицей
    На входе - искомое слово, на выходе - URL
    */
    public static URL buildUrl(String searchQuery) {
        Uri builtUri = Uri.parse(RUEO_BASE_URL).buildUpon().appendPath(searchQuery).build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /*
    Метод возвращает содержимое http запроса по URL
    */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
