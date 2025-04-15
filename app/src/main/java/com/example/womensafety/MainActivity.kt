package com.example.womensafety

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.womensafety.ui.theme.WomenSafetyTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel


// Import for your viewmodels and data classes
import com.example.womensafety.data.EmergencyContact
import com.example.womensafety.data.UserProfile
import viewmodel.ContactsViewModel
import viewmodel.ProfileViewModel as ProfileViewModel1

// Import for compose saveable state
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.firestore.FieldValue


class MainActivity : ComponentActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()

        setContent {
            WomenSafetyTheme {
                AppContent(firestore, auth)
            }
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

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.isNotBlank() && phoneNumber.all { it.isDigit() } && phoneNumber.length >= 10
    }
}

@Composable
fun AppContent(firestore: FirebaseFirestore, auth: FirebaseAuth) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Check Firebase Auth for login status
    val startDestination = if (auth.currentUser != null) {
        // Check if emergency contacts are set in Firestore
        checkEmergencyContactsAndNavigate(navController, firestore, auth)
        "dashboard" // Default destination while checking contacts
    } else {
        "login"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(navController, startDestination = startDestination) {

                composable("liveLocation") {
                    LiveLocationScreen(navController)
                }
                composable("login") {
                    LoginScreen(navController = navController, firestore = firestore, auth = auth)
                }
                composable("register") {
                    RegistrationScreen(navController = navController, firestore = firestore, auth = auth)
                }
                composable("emergency_contacts") {
                    EmergencyContactScreen(navController = navController, firestore = firestore, auth = auth)
                }
                composable("sos") {
                    SOSScreen(navController)
                }
                composable("dashboard") {
                    DashboardScreen(navController, firestore, auth)
                }
                composable("update_profile") {
                    val profileViewModel: ProfileViewModel1 = viewModel()
                    ProfileScreen(viewModel = profileViewModel)
                }

                composable("edit_contacts") {
                    val contactsViewModel: ContactsViewModel = viewModel()
                    ContactsScreen(viewModel = contactsViewModel)
                }

                composable("forgot_password") {
                    ForgotPasswordScreen(navController = navController, auth = auth)
                }
                composable("contacts") {
                    // Manually create the ViewModel for ContactsScreen
                    val contactsViewModel: ContactsViewModel = viewModel()
                    ContactsScreen(viewModel = contactsViewModel)
                }
                composable("profile") {
                    // Manually create the ViewModel for ProfileScreen
                    val profileViewModel: ProfileViewModel1 = viewModel()
                    ProfileScreen(viewModel = profileViewModel)
                }

            }


            // Show bottom navigation only for authenticated pages
            if (currentRoute in listOf("dashboard", "sos", "edit_contacts", "update_profile")) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute ?: "",
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLocationScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Location") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This is the Live Location screen.",
                style = MaterialTheme.typography.titleLarge

            )
        }
    }
}


@Composable
fun UpdateProfileScreen(navController: NavHostController, firestore: FirebaseFirestore, auth: FirebaseAuth) {

}


@Composable
fun EditContactsScreen(navController: NavHostController, firestore: FirebaseFirestore, auth: FirebaseAuth) {

}

private fun checkEmergencyContactsAndNavigate(navController: NavController, firestore: FirebaseFirestore, auth: FirebaseAuth) {
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

@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentRoute: String,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = {
                if (currentRoute != "dashboard") {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            },
            icon = {
                Icon(
                    if (currentRoute == "dashboard") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home") }
        )

        NavigationBarItem(
            selected = currentRoute == "sos",
            onClick = {
                if (currentRoute != "sos") {
                    navController.navigate("sos")
                }
            },
            icon = {
                Icon(
                    if (currentRoute == "sos") Icons.Filled.Warning else Icons.Outlined.Warning,
                    contentDescription = "SOS",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            label = { Text("SOS", color = MaterialTheme.colorScheme.error) }
        )

        NavigationBarItem(
            selected = currentRoute == "edit_contacts",
            onClick = {
                if (currentRoute != "edit_contacts") {
                    navController.navigate("edit_contacts")
                }
            },
            icon = {
                Icon(
                    if (currentRoute == "edit_contacts") Icons.Filled.Contacts else Icons.Outlined.Contacts,
                    contentDescription = "Contacts"
                )

            },
            label = { Text("Contacts") }
        )

        NavigationBarItem(
            selected = currentRoute == "update_profile",
            onClick = {
                if (currentRoute != "update_profile") {
                    navController.navigate("update_profile")
                }
            },
            icon = {
                Icon(
                    if (currentRoute == "update_profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Athena - Safety App") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        //hehe
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App logo or icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    "Sign in to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                var email by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )


                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                )

                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = password, // Ensure 'password' is properly managed in your state
                    onValueChange = { password = it }, // Update password value
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )







                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { navController.navigate("forgot_password") }) {
                        Text("Forgot Password?", color = MaterialTheme.colorScheme.primary)
                    }
                }


                Spacer(modifier = Modifier.height(24.dp))

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign In", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Don't have an account?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { navController.navigate("register") }) {
                        Text("Sign Up")
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Join Athena",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    "Create your account to stay safe",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "First Name"
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Last Name"
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Username"
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (firstName.isNotBlank() && lastName.isNotBlank() && username.isNotBlank() &&
                            email.isNotBlank() && password.isNotBlank()
                        ) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Create Account", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Already have an account?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("Sign In")
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

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

    // Load existing contacts from Firestore
    LaunchedEffect(Unit) {
        userId?.let { uid ->
            firestore.collection("users").document(uid)
                .collection("emergencyContacts")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val name = document.getString("name") ?: ""
                        val phone = document.getString("phoneNumber") ?: ""
                        if (name.isNotEmpty() && phone.isNotEmpty()) {
                            contactsList.add(Pair(name, phone))
                        }
                    }
                }
        }
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Emergency Contacts") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text("Your Emergency Contacts", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(8.dp))

            if (contactsList.isEmpty()) {
                Text("No contacts added yet", color = Color.Gray)
            } else {
                LazyColumn {
                    items(contactsList) { (name, phone) ->
                        ContactItem(name, phone, contactsList, firestore, userId)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add contact from phone
            Button(
                onClick = { contactPickerLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Contact from Phone")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Manual contact entry
            var contactName by remember { mutableStateOf("") }
            var contactPhone by remember { mutableStateOf("") }

            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Contact Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contactPhone,
                onValueChange = { contactPhone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (contactName.isNotBlank() && contactPhone.isNotBlank()) {
                        contactsList.add(Pair(contactName, contactPhone))
                        saveEmergencyContact(userId, contactName, contactPhone, firestore)
                        contactName = ""
                        contactPhone = ""
                    } else {
                        errorMessage = "Please enter both name and phone number"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Contact")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (contactsList.size >= 2) {
                        navController.navigate("dashboard")
                    } else {
                        errorMessage = "Please add at least two emergency contacts"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red)
            }
        }
    }
}

// Save Contact to Firestore
fun saveEmergencyContact(userId: String?, name: String, phone: String, firestore: FirebaseFirestore) {
    userId?.let { uid ->
        val contact = hashMapOf(
            "name" to name,
            "phoneNumber" to phone,
            "createdAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(uid)
            .collection("emergencyContacts")
            .add(contact)
    }
}

// Fetch Contact Details
fun getContactDetails(context: Context, contactUri: Uri): Pair<String, String>? {
    var name = ""
    var phoneNumber = ""

    context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            if (nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
    }

    val contactId = contactUri.lastPathSegment

    val phoneCursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
        arrayOf(contactId),
        null
    )

    phoneCursor?.use { cursor ->
        if (cursor.moveToFirst()) {
            val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (phoneIndex >= 0) {
                phoneNumber = cursor.getString(phoneIndex).replace("\\s".toRegex(), "")
            }
        }
    }

    return if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
        Pair(name, phoneNumber)
    } else {
        null
    }
}

// Contact Item UI
@Composable
fun ContactItem(name: String, phone: String, contactsList: MutableList<Pair<String, String>>, firestore: FirebaseFirestore, userId: String?) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, fontWeight = FontWeight.Bold)
                Text(phone, color = Color.Gray)
            }
            IconButton(onClick = {
                contactsList.remove(Pair(name, phone))
                userId?.let { uid ->
                    firestore.collection("users").document(uid)
                        .collection("emergencyContacts")
                        .whereEqualTo("phoneNumber", phone)
                        .get()
                        .addOnSuccessListener { docs ->
                            for (doc in docs) doc.reference.delete()
                        }
                }
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Contact", tint = Color.Red)
            }
        }
    }
}






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    auth: FirebaseAuth
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Reset Password",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Forgot Your Password?",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Enter your email address and we'll send you instructions to reset your password",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            isLoading = true
                            auth.sendPasswordResetEmail(email)
                                .addOnSuccessListener {
                                    isLoading = false
                                    successMessage = "Password reset email sent to $email"
                                    errorMessage = ""
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                    errorMessage = "Error: ${it.message}"
                                    successMessage = ""
                                }
                        } else {
                            errorMessage = "Please enter your email address"
                            successMessage = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send Reset Link", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate("login") }
                ) {
                    Text("Back to Login")
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                if (successMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = successMessage,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SOSScreen(navController: NavController) {
    val context = LocalContext.current
    var isSendingSOS by remember { mutableStateOf(false) }
    var countdownValue by remember { mutableStateOf(5) }

    // Animation for the pulsating effect on the SOS button
    val infiniteTransition = rememberInfiniteTransition()
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSendingSOS) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Countdown effect when SOS is being sent
    LaunchedEffect(isSendingSOS) {
        if (isSendingSOS) {
            while (countdownValue > 0) {
                delay(1000)
                countdownValue--
            }
            // When countdown finishes, simulate sending SOS
            // In a real app, this would trigger location sharing, SMS sending, etc.
            isSendingSOS = false
            countdownValue = 5
            // Show a toast to indicate SOS has been sent
            Log.d("SOSScreen", "Emergency alert sent to contacts")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isSendingSOS)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.background
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            if (isSendingSOS) {
                Text(
                    "SENDING EMERGENCY ALERT IN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "$countdownValue",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { isSendingSOS = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "CANCEL",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    "PRESS AND HOLD FOR EMERGENCY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Large SOS button with pulsating effect
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(scale.value)
                        .shadow(elevation = 10.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .border(4.dp, MaterialTheme.colorScheme.errorContainer, CircleShape)
                        .clickable {
                            // On click trigger SOS
                            isSendingSOS = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SOS",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onError
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Emergency help will be sent to your trusted contacts with your location",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Emergency call buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "EMERGENCY NUMBERS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Police button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:911")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Call Police"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("POLICE (911)")
                        }

                        // Women's Helpline button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:1800")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Call,
                                contentDescription = "Call Women's Helpline"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WOMEN'S HELPLINE (1-800)")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val userId = auth.currentUser?.uid
    var userName by remember { mutableStateOf("User") }
    var safetyTips by remember { mutableStateOf(listOf<String>()) }
    var emergencyContacts by remember { mutableStateOf(listOf<Pair<String, String>>()) }

    // Load user data when screen opens
    LaunchedEffect(Unit) {
        userId?.let { uid ->
            // Get user profile
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName") ?: ""
                    userName = firstName
                }

            // Get emergency contacts
            firestore.collection("users").document(uid)
                .collection("emergencyContacts")
                .get()
                .addOnSuccessListener { documents ->
                    val contacts = mutableListOf<Pair<String, String>>()
                    for (document in documents) {
                        val name = document.getString("name") ?: ""
                        val phone = document.getString("phoneNumber") ?: ""
                        if (name.isNotEmpty() && phone.isNotEmpty()) {
                            contacts.add(Pair(name, phone))
                        }
                    }
                    emergencyContacts = contacts
                }
        }

        // Sample safety tips
        safetyTips = listOf(
            "Share your location with trusted contacts when traveling alone",
            "Stay alert and aware of your surroundings at all times",
            "Avoid sharing too much personal information on social media",
            "Trust your instincts - if something feels wrong, it probably is",
            "Learn basic self-defense techniques for emergency situations",
            "Keep your phone charged and accessible at all times"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Athena") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                "Welcome, $userName",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Your safety is our priority",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Safety Shield",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Actions Section
                Text(
                    "QUICK ACTIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // SOS Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .clickable { navController.navigate("sos") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "SOS",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "SOS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Live Location Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .clickable { navController.navigate("liveLocation") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Share Location",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Share Location",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Second row of quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Fake Call Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .clickable { navController.navigate("fakeCall") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Fake Call",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Fake Call",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // Safety Tips Button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .clickable { navController.navigate("safetyTips") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Safety Tips",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Safety Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Emergency Contacts Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "EMERGENCY CONTACTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = { navController.navigate("manageContacts") },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Manage")
                    }
                }

                // List of emergency contacts
                if (emergencyContacts.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = "Add Contact",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "No emergency contacts added yet",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { navController.navigate("manageContacts") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Contacts")
                            }
                        }
                    }
                } else {
                    emergencyContacts.forEach { (name, phone) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        name.first().toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp)
                                ) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        phone,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                @Composable
                                fun CallButton(phone: String) {
                                    val context = LocalContext.current // Get context here

                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:$phone")
                                            }
                                            context.startActivity(intent) // Use context safely
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Call"
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.Call, // Explicitly specify imageVector
                                    contentDescription = "Call $name",
                                    tint = MaterialTheme.colorScheme.primary
                                )


                                Spacer(modifier = Modifier.height(24.dp))

                                // Safety Tips Section
                                Text(
                                    "SAFETY TIPS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )


                                @Composable
                                fun PreviewSafetyTipsRow() {
                                    Column {
                                        val sampleTips = listOf(
                                            "Always share your live location with a trusted contact.",
                                            "Avoid walking alone in isolated areas at night.",
                                            "Keep emergency contacts easily accessible."
                                        )

                                    }
                                    // Carousel of safety tips
                                    @Composable
                                    fun SafetyTipsRow(safetyTips: List<String>) {
                                        LazyRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            items(safetyTips) { tip ->
                                                Card(
                                                    modifier = Modifier
                                                        .width(280.dp)
                                                        .height(160.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.inverseSurface
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(16.dp),
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Lightbulb,
                                                            contentDescription = "Tip",
                                                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                                                            modifier = Modifier.size(32.dp)
                                                        )

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        Text(
                                                            text = tip,
                                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                                fontSize = 16.sp
                                                            ),
                                                            color = MaterialTheme.colorScheme.inverseOnSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }


                                    // Bottom Actions
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text(
                                                "ADDITIONAL SETTINGS",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Profile Settings
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { navController.navigate("profile") }
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = "Profile",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Text(
                                                    "Your Profile",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                Spacer(modifier = Modifier.weight(1f))

                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = "Go to Profile",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }



                                            // App Settings
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { navController.navigate("settings") }
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Settings,
                                                    contentDescription = "Settings",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Text(
                                                    "App Settings",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )

                                                Spacer(modifier = Modifier.weight(1f))

                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    contentDescription = "Go to Settings",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                }

                                // Main SOS Button
                                Box(

                                    modifier = Modifier.fillMaxSize() // Ensure it takes full screen space
                                ) {
                                    FloatingActionButton(
                                        onClick = { navController.navigate("sos") },
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd) // Now align() will work!
                                            .padding(24.dp)
                                            .size(64.dp),
                                        shape = CircleShape,
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "SOS",
                                            tint = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }


                                @Composable
                                fun LiveLocationScreen(navController: NavController) {
                                    var isLocationSharing by remember { mutableStateOf(false) }
                                    var selectedContacts by remember { mutableStateOf(listOf<String>()) }
                                    var sharingDuration by remember { mutableStateOf(30) } // Default 30 minutes
                                    var remainingTime by remember { mutableStateOf(0) }

                                    // Timer effect when location is being shared
                                    LaunchedEffect(isLocationSharing) {
                                        if (isLocationSharing) {
                                            remainingTime =
                                                sharingDuration * 60 // Convert to seconds
                                            while (remainingTime > 0) {
                                                delay(1000)
                                                remainingTime--
                                            }
                                            // When timer finishes, stop sharing
                                            isLocationSharing = false
                                        }
                                    }

                                    Scaffold(
                                        topBar = {
                                            TopAppBar(
                                                title = { Text("Live Location Sharing") },
                                                colors = TopAppBarDefaults.topAppBarColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                                                ),
                                                navigationIcon = {
                                                    IconButton(onClick = { navController.popBackStack() }) {
                                                        Icon(
                                                            Icons.Default.ArrowBack,
                                                            contentDescription = "Back",
                                                            tint = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    ) { paddingValues ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(paddingValues)
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Spacer(modifier = Modifier.height(16.dp))

                                            if (isLocationSharing) {
                                                // Location sharing active view
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    // This would be a map in a real implementation
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            "LIVE LOCATION ACTIVE",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        // Pulsating location indicator
                                                        Box(
                                                            modifier = Modifier
                                                                .size(64.dp)
                                                                .padding(8.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    MaterialTheme.colorScheme.primary.copy(
                                                                        alpha = 0.3f
                                                                    )
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(32.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.primary)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // Time remaining
                                                val hours = remainingTime / 3600
                                                val minutes = (remainingTime % 3600) / 60
                                                val seconds = remainingTime % 60

                                                Text(
                                                    "Time Remaining",
                                                    style = MaterialTheme.typography.titleMedium
                                                )

                                                Text(
                                                    String.format(
                                                        "%02d:%02d:%02d",
                                                        hours,
                                                        minutes,
                                                        seconds
                                                    ),
                                                    style = MaterialTheme.typography.displaySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // Contacts receiving location
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(16.dp)
                                                    ) {
                                                        Text(
                                                            "SHARING WITH",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        selectedContacts.forEach { contact ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Person,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.primary
                                                                )

                                                                Spacer(modifier = Modifier.width(8.dp))

                                                                Text(
                                                                    contact,
                                                                    style = MaterialTheme.typography.bodyLarge
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.weight(1f))

                                                // Stop sharing button
                                                Button(
                                                    onClick = { isLocationSharing = false },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        "STOP SHARING",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                // Location sharing setup view
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    // This would be a map preview in a real implementation
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Icon(
                                                            Icons.Default.LocationOn,
                                                            contentDescription = "Location",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(48.dp)
                                                        )

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Text(
                                                            "Your Current Location",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // Duration selection
                                                Text(
                                                    "SHARING DURATION",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    listOf(30, 60, 120, 240).forEach { duration ->
                                                        Button(
                                                            onClick = {
                                                                sharingDuration = duration
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                                .padding(horizontal = 4.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = if (sharingDuration == duration)
                                                                    MaterialTheme.colorScheme.primary
                                                                else
                                                                    MaterialTheme.colorScheme.surface
                                                            ),
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                if (sharingDuration == duration)
                                                                    MaterialTheme.colorScheme.primary
                                                                else
                                                                    MaterialTheme.colorScheme.outline
                                                            )
                                                        ) {
                                                            Text(
                                                                "${duration / 60}h",
                                                                color = if (sharingDuration == duration)
                                                                    MaterialTheme.colorScheme.onPrimary
                                                                else
                                                                    MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // Contact selection
                                                Text(
                                                    "SELECT CONTACTS",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp)
                                                )

                                                // Sample contacts
                                                val contacts =
                                                    listOf("Mom", "Dad", "Sister", "Best Friend")

                                                LazyColumn(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .border(
                                                            1.dp,
                                                            MaterialTheme.colorScheme.outline,
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .padding(8.dp)
                                                ) {
                                                    items(contacts) { contact ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Checkbox(
                                                                checked = selectedContacts.contains(
                                                                    contact
                                                                ),
                                                                onCheckedChange = { checked ->
                                                                    selectedContacts =
                                                                        if (checked) {
                                                                            selectedContacts + contact
                                                                        } else {
                                                                            selectedContacts - contact
                                                                        }
                                                                }
                                                            )

                                                            Text(
                                                                contact,
                                                                style = MaterialTheme.typography.bodyLarge
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.weight(1f))

                                                // Start sharing button
                                                Button(
                                                    onClick = {
                                                        if (selectedContacts.isNotEmpty()) {
                                                            isLocationSharing = true
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    enabled = selectedContacts.isNotEmpty()
                                                ) {
                                                    Text(
                                                        "START SHARING",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsScreen(viewModel: ContactsViewModel = viewModel()) {
    // Observe the contacts list from the ViewModel
    val contacts by viewModel.contacts.collectAsState()

    // Load contacts on first launch
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Emergency Contacts", style = MaterialTheme.typography.titleLarge)



        Spacer(modifier = Modifier.height(16.dp))

        // Add Contact Button
        Button(
            onClick = { viewModel.addContact() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Contact")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display contacts in a list
        LazyColumn {
            items(contacts) { contact ->
                ContactItem(contact = contact, viewModel = viewModel)
            }
        }
    }
}



@Composable
fun ContactItem(contact: EmergencyContact, viewModel: ContactsViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var updatedName by remember { mutableStateOf(contact.name) }
    var updatedPhoneNumber by remember { mutableStateOf(contact.phoneNumber) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        if (isEditing) {
            OutlinedTextField(
                value = updatedName,
                onValueChange = { updatedName = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = updatedPhoneNumber,
                onValueChange = { updatedPhoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.updateContact(
                        contact.copy(name = updatedName, phoneNumber = updatedPhoneNumber)
                    )
                    isEditing = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        } else {
            Text("Name: ${contact.name}", style = MaterialTheme.typography.bodyLarge)
            Text("Phone: ${contact.phoneNumber}", style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { isEditing = true }) {
                    Text("Edit")
                }
                TextButton(onClick = { viewModel.deleteContact(contact) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}




@Composable
fun ProfileScreen(viewModel: ProfileViewModel1 = viewModel()) {
    val userData by viewModel.userData.collectAsState()

    // Load once when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Use rememberSaveable to persist during recompositions
    var username by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    // Update UI fields once when data loads
    LaunchedEffect(userData) {
        username = userData.username
        firstName = userData.firstName
        lastName = userData.lastName
        phone = userData.phone
        email = userData.email
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Edit Profile", style = MaterialTheme.typography.titleLarge)


        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                viewModel.updateField("username", it)
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                viewModel.updateField("firstName", it)
            },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                viewModel.updateField("lastName", it)
            },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                viewModel.updateField("phone", it)
            },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.updateField("email", it)
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveUserProfile()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}


