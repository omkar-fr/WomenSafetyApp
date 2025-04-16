package com.example.womensafety.data

data class EmergencyContact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String
)



data class UserProfile(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = ""
)

