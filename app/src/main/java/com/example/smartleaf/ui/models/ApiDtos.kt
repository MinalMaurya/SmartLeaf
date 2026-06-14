package com.example.smartleaf.ui.models

data class SignupRequest(val email:String, val name:String, val phone:String?=null, val preferred_lang:String="en", val password:String)
data class AuthResponse(val ok:Boolean, val token:String?, val farmer_id:Long?)
data class LoginRequest(val email:String, val password:String)
data class LoginResponse(val ok:Boolean, val token:String?, val farmer:Farmer?)
data class Farmer(val farmer_id:Long, val email:String, val name:String, val phone:String?, val preferred_lang:String)
data class ProfileResponse(val ok:Boolean, val profile:Farmer)
data class UpdateProfileRequest(val name:String?=null, val phone:String?=null, val preferred_lang:String?=null)
data class ScanRequest(
    val image_uri: String, val model_name: String, val model_version: String,
    val predicted_code: String, val confidence: Double,
    val generated_desc: String?, val generated_symptoms: String? =null, val generated_remedies: String? =null
)
data class ScanEntry(
    val scan_id:Long, val image_uri:String, val model_name:String, val model_version:String,
    val predicted_code:String, val confidence:Double,
    val generated_desc:String, val generated_symptoms:String?, val generated_remedies:String?,
    val created_at:String
)
data class ScansResponse(val ok:Boolean, val scans:List<ScanEntry>)
data class SaveScanResponse(val ok:Boolean, val scan_id:Long)