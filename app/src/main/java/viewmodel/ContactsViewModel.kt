package viewmodel

import androidx.lifecycle.ViewModel
import com.example.womensafety.data.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _contacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val contacts: StateFlow<List<EmergencyContact>> = _contacts

    // 🔄 Load contacts from Firestore
    fun loadContacts() {
        val userId = auth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection("emergencyContacts")
                    .get().await()

                val loadedContacts = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    val phone = doc.getString("phoneNumber")
                    if (!name.isNullOrBlank() && !phone.isNullOrBlank()) {
                        EmergencyContact(doc.id, name, phone)
                    } else null
                }

                _contacts.value = loadedContacts
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    // ➕ Add new dummy contact (you can replace with actual UI form)
    fun addContact() {
        val userId = auth.currentUser?.uid ?: return
        val newContact = EmergencyContact(name = "New Contact", phoneNumber = "1234567890")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("users").document(userId)
                    .collection("emergencyContacts")
                    .add(mapOf(
                        "name" to newContact.name,
                        "phoneNumber" to newContact.phoneNumber
                    )).await()
                loadContacts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // ✏️ Update contact
    fun updateContact(contact: EmergencyContact) {
        val userId = auth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("users").document(userId)
                    .collection("emergencyContacts")
                    .document(contact.id)
                    .set(mapOf(
                        "name" to contact.name,
                        "phoneNumber" to contact.phoneNumber
                    )).await()
                loadContacts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // ❌ Delete contact
    fun deleteContact(contact: EmergencyContact) {
        val userId = auth.currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("users").document(userId)
                    .collection("emergencyContacts")
                    .document(contact.id)
                    .delete().await()
                loadContacts()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
