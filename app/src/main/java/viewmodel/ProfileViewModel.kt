package viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ✅ Define the data model
data class UserProfile(
    var username: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var phone: String = "",
    var email: String = ""
)

class ProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userData = MutableStateFlow(UserProfile())
    val userData: StateFlow<UserProfile> = _userData

    // ✅ Load user profile from Firestore
    fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("ProfileViewModel", "Fetched data: ${document.data}")
                _userData.value = UserProfile(
                    username = document.getString("username") ?: "",
                    firstName = document.getString("firstName") ?: "",
                    lastName = document.getString("lastName") ?: "",
                    phone = document.getString("phone") ?: "",
                    email = document.getString("email") ?: ""
                )
            }
            .addOnFailureListener {
                Log.e("ProfileViewModel", "Error fetching user data", it)
            }
    }

    // ✅ Update one field locally
    fun updateField(field: String, value: String) {
        val updated = _userData.value.copy().apply {
            when (field) {
                "username" -> username = value
                "firstName" -> firstName = value
                "lastName" -> lastName = value
                "phone" -> phone = value
                "email" -> email = value
            }
        }
        _userData.value = updated
    }

    // ✅ Save updated profile to Firestore
    fun saveUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userMap = mapOf(
            "username" to _userData.value.username,
            "firstName" to _userData.value.firstName,
            "lastName" to _userData.value.lastName,
            "phone" to _userData.value.phone,
            "email" to _userData.value.email
        )
        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("ProfileViewModel", "User profile saved successfully")
            }
            .addOnFailureListener {
                Log.e("ProfileViewModel", "Failed to save user profile", it)
            }
    }
}