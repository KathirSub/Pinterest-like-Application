package com.android.assignment2B;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class InternetOperations {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static String SERVER_URL = "https://pixabay.com/api/";
    public static String API_KEY = "13665803-562bbd6db6230116df784dc4b";

    public static String get(String url) throws IOException {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
