package com.example.forgepswd

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgepswd.ui.theme.ForgePswdTheme
import java.security.MessageDigest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgePswdTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ForgePswdApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgePswdApp() {
    var basePassword by remember { mutableStateOf("") }
    var masterKey by remember { mutableStateOf("") }
    var generatedPassword by remember { mutableStateOf("") }
    var basePasswordVisible by remember { mutableStateOf(false) }
    var masterKeyVisible by remember { mutableStateOf(false) }
    var generatedPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ForgePswd",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Secure Deterministic Password Generator",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Base Password Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = basePassword,
                    onValueChange = { basePassword = it },
                    label = { Text("Base Password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { basePasswordVisible = !basePasswordVisible }) {
                            Icon(
                                imageVector = if (basePasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (basePasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (basePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Master Key Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = masterKey,
                    onValueChange = { masterKey = it },
                    label = { Text("Master Key") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { masterKeyVisible = !masterKeyVisible }) {
                            Icon(
                                imageVector = if (masterKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (masterKeyVisible) "Hide key" else "Show key"
                            )
                        }
                    },
                    visualTransformation = if (masterKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
        }

        // Generate Password Button
        Button(
            onClick = {
                if (basePassword.isNotEmpty() && masterKey.isNotEmpty()) {
                    generatedPassword = generatePassword(basePassword, masterKey)
                } else {
                    Toast.makeText(context, "Please enter both base password and master key", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "Generate Password",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Generated Password Display
        if (generatedPassword.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Generated Password",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = generatedPassword,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { generatedPasswordVisible = !generatedPasswordVisible }) {
                                Icon(
                                    imageVector = if (generatedPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (generatedPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (generatedPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy Password Button
                    Button(
                        onClick = {
                            copyToClipboard(context, generatedPassword)
                            Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Copy Password",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun generatePassword(basePassword: String, masterKey: String): String {
    // Step 1: Concatenate and hash
    val combined = basePassword + masterKey
    val hash = sha256(combined)
    val prefix = hash.substring(0, 8)

    Log.d("ForgePswd", "Combined input: $combined")
    Log.d("ForgePswd", "SHA-256 hash: $hash")
    Log.d("ForgePswd", "Initial prefix: $prefix")

    // Step 2: Apply rules to ensure strong password
    val modifiedPrefix = applyPasswordRules(prefix)

    Log.d("ForgePswd", "Modified prefix: $modifiedPrefix")

    // Step 3: Final password format
    val finalPassword = "$modifiedPrefix,$masterKey"
    Log.d("ForgePswd", "Final password: $finalPassword")

    return finalPassword
}

fun applyPasswordRules(prefix: String): String {
    val result = prefix.toCharArray()
    val modifiedIndices = mutableSetOf<Int>()

    Log.d("ForgePswd", "Applying password rules to: $prefix")

    // Rule 1: Ensure at least one uppercase
    val hasUppercase = result.any { it.isLetter() && it.isUpperCase() }
    if (!hasUppercase) {
        val firstAlphabetIndex = result.indexOfFirst { it.isLetter() }
        if (firstAlphabetIndex >= 0) {
            result[firstAlphabetIndex] = result[firstAlphabetIndex].uppercaseChar()
            modifiedIndices.add(firstAlphabetIndex)
            Log.d("ForgePswd", "Rule 1: Uppercased character at index $firstAlphabetIndex")
        } else {
            result[0] = 'A'
            modifiedIndices.add(0)
            Log.d("ForgePswd", "Rule 1: No alphabet found, replaced index 0 with 'A'")
        }
    }

    // Rule 2: Ensure at least one lowercase
    val hasLowercase = result.any { it.isLetter() && it.isLowerCase() }
    if (!hasLowercase) {
        var secondAlphabetIndex = -1
        for (i in 1 until result.size) {
            if (result[i].isLetter() && !modifiedIndices.contains(i)) {
                secondAlphabetIndex = i
                break
            }
        }
        if (secondAlphabetIndex >= 0) {
            result[secondAlphabetIndex] = result[secondAlphabetIndex].lowercaseChar()
            modifiedIndices.add(secondAlphabetIndex)
            Log.d("ForgePswd", "Rule 2: Lowercased character at index $secondAlphabetIndex")
        } else {
            val fallbackIndex = if (modifiedIndices.contains(1)) 2 else 1
            if (fallbackIndex < result.size) {
                result[fallbackIndex] = 'b'
                modifiedIndices.add(fallbackIndex)
                Log.d("ForgePswd", "Rule 2: Inserted 'b' at index $fallbackIndex")
            }
        }
    }

    // Rule 3: Ensure at least one digit
    val hasDigit = result.any { it.isDigit() }
    if (!hasDigit) {
        var insertIndex = 7
        while (insertIndex >= 0 && modifiedIndices.contains(insertIndex)) {
            insertIndex--
        }
        if (insertIndex >= 0 && insertIndex < result.size) {
            result[insertIndex] = '4'
            modifiedIndices.add(insertIndex)
            Log.d("ForgePswd", "Rule 3: Inserted '4' at index $insertIndex")
        }
    }

    val finalResult = String(result)
    Log.d("ForgePswd", "Final modified prefix: $finalResult")
    return finalResult
}

fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Generated Password", text)
    clipboard.setPrimaryClip(clip)
}