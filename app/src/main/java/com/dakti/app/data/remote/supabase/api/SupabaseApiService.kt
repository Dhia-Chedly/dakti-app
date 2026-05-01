package com.dakti.app.data.remote.supabase.api

import com.dakti.app.data.remote.supabase.model.SupabaseAuthSignInRequest
import com.dakti.app.data.remote.supabase.model.SupabaseAuthSignUpRequest
import com.dakti.app.data.remote.supabase.model.SupabaseAuthSignUpResponse
import com.dakti.app.data.remote.supabase.model.SupabaseAuthUserResponse
import com.dakti.app.data.remote.supabase.model.SupabaseRefreshTokenRequest
import com.dakti.app.data.remote.supabase.model.SupabaseSessionResponse
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface SupabaseApiService {

    @POST("auth/v1/token?grant_type=password")
    suspend fun signInWithPassword(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthSignInRequest
    ): SupabaseSessionResponse

    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthSignUpRequest
    ): SupabaseAuthSignUpResponse

    @GET("auth/v1/user")
    suspend fun getCurrentUser(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String
    ): SupabaseAuthUserResponse

    @POST("auth/v1/token?grant_type=refresh_token")
    suspend fun refreshSession(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseRefreshTokenRequest
    ): SupabaseSessionResponse

    @GET("rest/v1/{table}")
    suspend fun selectRows(
        @Path("table", encoded = true) table: String,
        @HeaderMap headers: Map<String, String>,
        @QueryMap(encoded = true) query: Map<String, String>
    ): JsonArray

    @POST("rest/v1/{table}")
    suspend fun insertRows(
        @Path("table", encoded = true) table: String,
        @HeaderMap headers: Map<String, String>,
        @Body payload: JsonElement
    ): JsonArray

    @PATCH("rest/v1/{table}")
    suspend fun updateRows(
        @Path("table", encoded = true) table: String,
        @HeaderMap headers: Map<String, String>,
        @QueryMap(encoded = true) filters: Map<String, String>,
        @Body payload: JsonObject
    ): JsonArray

    @POST("functions/v1/{functionName}")
    suspend fun invokeFunction(
        @Path("functionName", encoded = true) functionName: String,
        @HeaderMap headers: Map<String, String>,
        @Body payload: JsonObject
    ): JsonObject
}
