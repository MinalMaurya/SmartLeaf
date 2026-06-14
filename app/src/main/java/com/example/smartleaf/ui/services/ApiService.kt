package com.example.smartleaf.ui.services

import com.example.smartleaf.ui.models.*
import retrofit2.http.*

interface ApiService {
    @POST("login.php")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("signup.php")
    suspend fun signup(@Body body: SignupRequest): AuthResponse

    @GET("get_scans.php")
    suspend fun listScans(): ScansResponse

    @POST("save_scan.php")
    suspend fun saveScan(@Body body: ScanRequest): SaveScanResponse
}