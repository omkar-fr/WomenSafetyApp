package com.example.womensafety

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.womensafety.ui.theme.WomenSafetyTheme

class MainActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("WomenSafetyApp", Context.MODE_PRIVATE)

        requestPermissions()

        setContent {
            WomenSafetyTheme {
                val navController = rememberNavController()

                val startDestination = if (isUserLoggedIn()) {
                    if (isEmergencyContactSet()) "home" else "emergency_contacts"
                } else "login"

                NavHost(navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            sharedPreferences = sharedPreferences
                        )
                    }
                    composable("register") {
                        RegistrationScreen(
                            navController = navController,
                            sharedPreferences = sharedPreferences
                        )
                    }
                    composable("emergency_contacts") {
                        EmergencyContactScreen(
                            navController = navController,
                            sharedPreferences = sharedPreferences
                        )
                    }
                    composable("home") {
                        HomeScreen(navController)
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
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

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.contains("email") && sharedPreferences.contains("password")
    }

    private fun isEmergencyContactSet(): Boolean {
        return !sharedPreferences.getString("emergencyContact1", "").isNullOrBlank() &&
                !sharedPreferences.getString("emergencyContact2", "").isNullOrBlank()
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    sharedPreferences: SharedPreferences
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
                if (email.isNotBlank() && password.isNotBlank()) {
                    val savedEmail = sharedPreferences.getString("email", "")
                    val savedPassword = sharedPreferences.getString("password", "")
                    if (email == savedEmail && password == savedPassword) {
                        navController.navigate("home")
                    } else {
                        errorMessage = "Invalid email or password"
                    }
                } else {
                    errorMessage = "Please fill in all fields"
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

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
    sharedPreferences: SharedPreferences
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    sharedPreferences.edit().apply {
                        putString("email", email)
                        putString("password", password)
                    }.apply()
                    navController.navigate("login")
                } else {
                    errorMessage = "Please fill in all fields"
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Register")
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
    sharedPreferences: SharedPreferences
) {
    var contact1 by remember { mutableStateOf("") }
    var contact2 by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
                    sharedPreferences.edit().apply {
                        putString("emergencyContact1", contact1)
                        putString("emergencyContact2", contact2)
                    }.apply()
                    navController.navigate("home")
                } else {
                    errorMessage = "Please enter valid phone numbers"
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Save Contacts")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    return phoneNumber.isNotBlank() && phoneNumber.all { it.isDigit() }
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
        Text("Safety Dashboard", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val emergencyIntent = Intent(context, EmergencyService::class.java)
                context.startService(emergencyIntent)
                emergencyTriggered = true
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