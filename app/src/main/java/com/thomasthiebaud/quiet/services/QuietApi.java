package com.thomasthiebaud.quiet.services;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

/**
 * Created by thomasthiebaud on 4/30/16.
 */
public interface QuietApi {
    @PUT("/api/user/signin")
    Call<Message> signIn(@Body HashMap<String, Object> body);
}
