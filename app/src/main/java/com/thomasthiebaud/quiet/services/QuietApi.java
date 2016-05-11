package com.thomasthiebaud.quiet.services;

import com.thomasthiebaud.quiet.model.Message;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by thomasthiebaud on 4/30/16.
 */
public interface QuietApi {
    @PUT("/api/user/signin")
    Call<Message> signIn(@Body HashMap<String, Object> body);

    @GET("/api/phone/{phoneNumber}")
    Call<Message> checkPhoneNumber(@Header("idtoken") String idToken, @Path("phoneNumber") String phoneNumber);

    @PUT("/api/phone")
    Call<Message> reportPhoneNumber(@Body HashMap<String, Object> body);
}
