package com.tryfit.common.rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by alexeyreznik on 03/07/2017.
 */

public interface TryFitWebService {

    String BASE_URL = "https://demo.try.fit/api/";

    @Headers("Content-Type: application/json")
    @POST("scan/{clientId}")
    Call<String> sendFootData(@Path("clientId") String clientId, @Body String body);

    @POST("login")
    @FormUrlEncoded
    Call <LoginResponse> login(@Field("login") String login, @Field("password") String password);

    @POST("signup")
    @FormUrlEncoded
    Call <LoginResponse> signUp(@Field("name") String name, @Field("surname") String surname,
                                @Field("email") String email, @Field("password") String password);

    @GET("clients/model/{clientId}")
    Call <ResponseBody> getModels(@Path("clientId") String clientId);

    @POST("graphql")
    Call<ResponseBody> loginInPlugin(@Body GraphQLRequest body);

    @POST("graphql")
    Call<ClientInfoResponse> getClientInfo(@Body GraphQLRequest body);

    @POST("graphql")
    Call<ClientFittingResponse> getClientFittings(@Body GraphQLRequest body);

    @POST("graphql")
    Call<GroupsResponse> getGroups(@Body GraphQLRequest body);
}
