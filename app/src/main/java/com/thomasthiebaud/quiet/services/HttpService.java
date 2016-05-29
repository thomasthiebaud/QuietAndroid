package com.thomasthiebaud.quiet.services;

import com.thomasthiebaud.quiet.BuildConfig;
import com.thomasthiebaud.quiet.component.UnsafeOkHttpClient;

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
                .baseUrl(BuildConfig.QUIET_SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(UnsafeOkHttpClient.get())
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
