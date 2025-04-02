package com.example.womensafety

import android.Manifest
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.womensafety.ui.theme.WomenSafetyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            WomenSafetyTheme {
                val navController = rememberNavController()

                // Check Firebase Auth for login status instead of SharedPreferences
                val startDestination = if (auth.currentUser != null) {
                    // Check if emergency contacts are set in Firestore
                    checkEmergencyContactsAndNavigate(navController)
                    "dashboard" // Default destination while checking contacts
                } else {
                    "login"
                }

                    NavHost(navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            firestore = firestore,
                            auth = auth
                        )
                    }
                    composable("register") {
                        RegistrationScreen(
                            navController = navController,
                            firestore = firestore,
                            auth = auth
                        )
                    }
                    composable("emergency_contacts") {
                        EmergencyContactScreen(
                            navController = navController,
                            firestore = firestore,
                            auth = auth
                        )
                    }
                    composable("sos") {
                        HomeScreen(navController)
                    }
                    composable("dashboard") {
                        DashboardScreen(navController, firestore, auth)
                    }
                    composable("edit_contacts") {
                        EditContactsScreen(navController, firestore, auth)
                    }
                    composable("update_profile") {
                        UpdateProfileScreen(navController, firestore, auth)
                    }
                        composable("forgot_password") {
                            ForgotPasswordScreen(navController = navController, auth = auth)
                        }
                }
            }
        }
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.isNotBlank() && phoneNumber.all { it.isDigit() } && phoneNumber.length >= 10
    }


    private fun checkEmergencyContactsAndNavigate(navController: NavController) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users").document(currentUser.uid)
            .collection("emergencyContacts")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    navController.navigate("emergency_contacts") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            }
            .addOnFailureListener {
                // Handle error, maybe stay at dashboard with a message
            }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val deniedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (deniedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, deniedPermissions.toTypedArray(), 101)
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = emailOrUsername,
            onValueChange = { emailOrUsername = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (emailOrUsername.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    auth.signInWithEmailAndPassword(emailOrUsername, password)
                        .addOnSuccessListener {
                            isLoading = false
                            val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                            firestore.collection("users").document(userId)
                                .collection("emergencyContacts")
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.size() >= 2) {
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("emergency_contacts") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    errorMessage = "Error checking contacts: ${it.message}"
                                }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = "Login failed: ${it.message}"
                        }
                } else {
                    errorMessage = "Please fill in all fields"
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Adjusted spacing

        TextButton(onClick = { navController.navigate("forgot_password") }) { // Added here
            Text("Forgot Password?")
        }

        Spacer(modifier = Modifier.height(8.dp)) // Adjusted spacing

        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun RegistrationScreen(
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (firstName.isNotBlank() && lastName.isNotBlank() && username.isNotBlank() &&
                    email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true

                    // First check if username is already in use
                    firestore.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                // Username is available, create authentication
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        val userId = auth.currentUser?.uid ?: return@addOnSuccessListener

                                        // Save user profile data
                                        val user = hashMapOf(
                                            "firstName" to firstName,
                                            "lastName" to lastName,
                                            "username" to username,
                                            "email" to email,
                                            "createdAt" to FieldValue.serverTimestamp()
                                        )

                                        firestore.collection("users").document(userId)
                                            .set(user)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                navController.navigate("emergency_contacts") {
                                                    popUpTo("register") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                errorMessage = "Error saving profile: ${e.message}"
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        errorMessage = "Registration failed: ${e.message}"
                                    }
                            } else {
                                isLoading = false
                                errorMessage = "Username already exists"
                            }
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = "Error checking username: ${e.message}"
                        }
                } else {
                    errorMessage = "Please fill in all fields"
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
@Composable
fun EmergencyContactScreen(
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val userId = auth.currentUser?.uid
    val context = LocalContext.current
    val contactsList = remember { mutableStateListOf<Pair<String, String>>() }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Contact Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { contactUri ->
            val contactInfo = getContactDetails(context, contactUri)
            contactInfo?.let { (name, phone) ->
                contactsList.add(Pair(name, phone))
                saveEmergencyContact(userId, name, phone, firestore)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emergency Contacts", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { contactPickerLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Select Contact")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(contactsList) { (name, phone) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(name, fontWeight = FontWeight.Bold)
                            Text(phone, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate("dashboard") },
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = contactsList.isNotEmpty()
        ) {
            Text("Save & Continue")
        }
    }
}

// Function to get contact details
fun getContactDetails(context: Context, contactUri: Uri): Pair<String, String>? {
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            return Pair(name, phone)
        }
    }
    return null
}

// Function to save contact to Firestore
fun saveEmergencyContact(userId: String?, name: String, phone: String, firestore: FirebaseFirestore) {
    userId?.let {
        val contactData = mapOf("name" to name, "phoneNumber" to phone)
        firestore.collection("users").document(userId)
            .collection("emergencyContacts")
            .add(contactData)
            .addOnSuccessListener {
                Log.d("Firestore", "Contact saved successfully")
            }
            .addOnFailureListener {
                Log.e("Firestore", "Failed to save contact: ${it.message}")
            }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    var emergencyTriggered by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("SOS Emergency", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val emergencyIntent = Intent(context, EmergencyService::class.java)
                context.startService(emergencyIntent)
                emergencyTriggered = true

                // Navigate to dashboard after SOS is triggered
                navController.navigate("dashboard") {
                    popUpTo("sos") { inclusive = true }
                }
            },
            modifier = Modifier
                .size(200.dp)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                "SOS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onError
            )
        }

        if (emergencyTriggered) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Emergency SOS Triggered!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun DashboardScreen(
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val safetyTips = listOf(
        "Always share your location with trusted friends when going out.",
        "Keep your phone charged and with you at all times.",
        "Trust your instincts. If something feels wrong, it probably is.",
        "Use well-lit, busy routes when walking, especially at night.",
        "Have emergency numbers on speed dial.",
        "Be aware of your surroundings and avoid distractions like texting while walking.",
        "Try to travel in groups when possible, especially at night.",
        "Let someone know your itinerary and expected arrival time."
    )
    val randomTip = remember { safetyTips[Random.nextInt(safetyTips.size)] }

    // Load user profile data from Firestore
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        firstName = document.getString("firstName") ?: ""
                        lastName = document.getString("lastName") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Welcome message with user's name
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Welcome $firstName $lastName!",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SOS Button
            Button(
                onClick = { navController.navigate("sos") },
                modifier = Modifier
                    .size(150.dp)
                    .padding(8.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    "SOS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onError
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Side by side buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { navController.navigate("edit_contacts") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text("Edit Emergency Contacts")
                }

                Button(
                    onClick = { navController.navigate("update_profile") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Update Profile")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout button
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Safety tips box at the bottom
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Safety Tip:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        randomTip,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun EditContactsScreen(
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    var contact1 by remember { mutableStateOf("") }
    var contact2 by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val userId = auth.currentUser?.uid

    // Load existing contacts when screen opens
    LaunchedEffect(Unit) {
        userId?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("emergencyContacts")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val contactNumber = document.getString("phoneNumber") ?: ""
                        val contactOrder = document.getLong("order")?.toInt() ?: 0

                        if (contactOrder == 1) {
                            contact1 = contactNumber
                        } else if (contactOrder == 2) {
                            contact2 = contactNumber
                        }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    errorMessage = "Failed to load contacts: ${it.message}"
                }
        } ?: run {
            isLoading = false
            errorMessage = "User not logged in"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Edit Emergency Contacts", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = contact1,
                onValueChange = { contact1 = it },
                label = { Text("Emergency Contact 1") },
                modifier = Modifier.fillMaxWidth(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = contact2,
                onValueChange = { contact2 = it },
                label = { Text("Emergency Contact 2") },
                modifier = Modifier.fillMaxWidth(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isValidPhoneNumber(contact1) && isValidPhoneNumber(contact2)) {
                        userId?.let { uid ->
                            isLoading = true

                            // Create a batch to save both contacts at once
                            val batch = firestore.batch()

                            val contactsRef = firestore.collection("users").document(uid)
                                .collection("emergencyContacts")

                            // First clear existing contacts
                            contactsRef.get()
                                .addOnSuccessListener { documents ->
                                    // Delete all existing contacts
                                    for (document in documents) {
                                        batch.delete(document.reference)
                                    }

                                    // Add new contacts
                                    val contact1Doc = hashMapOf(
                                        "phoneNumber" to contact1,
                                        "order" to 1,
                                        "updatedAt" to FieldValue.serverTimestamp()
                                    )

                                    val contact2Doc = hashMapOf(
                                        "phoneNumber" to contact2,
                                        "order" to 2,
                                        "updatedAt" to FieldValue.serverTimestamp()
                                    )

                                    batch.set(contactsRef.document("contact1"), contact1Doc)
                                    batch.set(contactsRef.document("contact2"), contact2Doc)

                                    // Commit the batch
                                    batch.commit()
                                        .addOnSuccessListener {
                                            isLoading = false
                                            navController.navigate("dashboard") {
                                                popUpTo("edit_contacts") { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            errorMessage = "Error saving contacts: ${e.message}"
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = "Error retrieving existing contacts: ${e.message}"
                                }
                        } ?: run {
                            errorMessage = "User not logged in"
                        }
                    } else {
                        errorMessage = "Please enter valid phone numbers"
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Contacts")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    navController.navigate("dashboard") {
                        popUpTo("edit_contacts") { inclusive = true }
                    }
                }
            ) {
                Text("Cancel")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    return phoneNumber.isNotBlank() && phoneNumber.all { it.isDigit() } && phoneNumber.length >= 10
}


@Composable
fun UpdateProfileScreen(
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: return

    // State variables for profile fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch user data when composable loads
    LaunchedEffect(Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    firstName = document.getString("firstName") ?: ""
                    lastName = document.getString("lastName") ?: ""
                    username = document.getString("username") ?: ""
                    email = document.getString("email") ?: ""
                }
                isLoading = false
            }
            .addOnFailureListener {
                errorMessage = "Failed to load profile data"
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Update Profile", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && username.isNotBlank() && email.isNotBlank()) {
                        isLoading = true
                        val updatedUser = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "username" to username,
                            "email" to email
                        )

                        firestore.collection("users").document(userId)
                            .set(updatedUser)
                            .addOnSuccessListener {
                                // Update email in Firebase Auth if it has changed
                                auth.currentUser?.updateEmail(email)?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        navController.navigate("dashboard") {
                                            popUpTo("update_profile") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = "Failed to update email"
                                    }
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Failed to update profile: ${e.message}"
                                isLoading = false
                            }
                    } else {
                        errorMessage = "Please fill in all fields"
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Update Profile")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    navController.navigate("dashboard") {
                        popUpTo("update_profile") { inclusive = true }
                    }
                },
                enabled = !isLoading
            ) {
                Text("Cancel")
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    auth: FirebaseAuth
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reset Password", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    isLoading = true
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            isLoading = false
                            message = "Password reset email sent! Check your inbox."
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            message = "Failed to send reset email: ${e.message}"
                        }
                } else {
                    message = "Please enter your email"
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Send Reset Link")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Back to Login")
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = if (message.startsWith("Failed")) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}