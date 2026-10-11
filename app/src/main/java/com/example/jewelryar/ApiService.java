package com.example.jewelryar;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> login(@Field("email") String email,
                              @Field("password") String password);

    @FormUrlEncoded
    @POST("register.php")
    Call<LoginResponse> register(@Field("name") String name,
                                 @Field("email") String email,
                                 @Field("password") String password);

    @GET("products.php")
    Call<ProductsResponse> getProducts();
    @FormUrlEncoded
    @POST("reset_password.php")
    Call<LoginResponse> resetPassword(@Field("email") String email,
                                      @Field("password") String password);
}