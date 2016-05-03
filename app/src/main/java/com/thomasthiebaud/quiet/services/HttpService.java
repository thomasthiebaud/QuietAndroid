package com.thomasthiebaud.quiet.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by thomasthiebaud on 5/1/16.
 */
public final class HttpService {
    private static HttpService instance = null;
    private QuietApi quietApi;

    private HttpService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://172.17.0.3:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(CustomTrust.getUnsafeOkHttpClient())
                .build();

        quietApi = retrofit.create(QuietApi.class);
    }

    public static HttpService getInstance() {
        if(instance == null)
            instance = new HttpService();
        return instance;
    }

    public QuietApi getQuietApi() {
        return quietApi;
    }
}
