package com.example.forgepswd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasswordGeneratorApp()
        }
    }
}

@Composable
fun PasswordGeneratorApp() {
    var base by remember { mutableStateOf("") }
    var master by remember { mutableStateOf("") }
    var generatedPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateSimplifiedSecurePassword(base: String, master: String): String {
        val hash = sha256(base + master)
        val prefix = StringBuilder(hash.substring(0, 8))

        println("🔒 SHA256(base + master): $hash")
        println("🔑 First 8 chars (prefix): $prefix")

        val modifiedIndices = mutableSetOf<Int>()
        val foundAlphabets = mutableListOf<Int>()
        var hasDigit = false

        // Scan prefix
        for (i in prefix.indices) {
            val c = prefix[i]
            if (c.isLetter()) {
                foundAlphabets.add(i)
            }
            if (c.isDigit()) {
                hasDigit = true
            }
        }

        // Rule 1: Uppercase first alphabet
        val firstAlphaIdx = foundAlphabets.getOrNull(0)
        if (firstAlphaIdx != null) {
            prefix.setCharAt(firstAlphaIdx, prefix[firstAlphaIdx].uppercaseChar())
            modifiedIndices.add(firstAlphaIdx)
        } else {
            prefix.setCharAt(0, 'A')
            modifiedIndices.add(0)
        }

        // Rule 2: Lowercase second alphabet
        val secondAlphaIdx = foundAlphabets.getOrNull(1)
        if (secondAlphaIdx != null && secondAlphaIdx !in modifiedIndices) {
            prefix.setCharAt(secondAlphaIdx, prefix[secondAlphaIdx].lowercaseChar())
            modifiedIndices.add(secondAlphaIdx)
        } else {
            val fallbackIdx = if (1 !in modifiedIndices) 1 else 2
            prefix.setCharAt(fallbackIdx, 'b')
            modifiedIndices.add(fallbackIdx)
        }

        // Rule 3: Ensure at least one digit
        if (!hasDigit) {
            val digitIdx = if (7 !in modifiedIndices) 7 else (6 downTo 0).firstOrNull { it !in modifiedIndices } ?: 7
            prefix.setCharAt(digitIdx, '4')
            modifiedIndices.add(digitIdx)
        }

        return prefix.toString() + "," + master
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            OutlinedTextField(
                value = base,
                onValueChange = { base = it },
                label = { Text("Base Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = master,
                onValueChange = { master = it },
                label = { Text("Master Key") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                if (base.isNotBlank() && master.isNotBlank()) {
                    generatedPassword = generateSimplifiedSecurePassword(base, master)
                }
            }) {
                Text("Generate Password")
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (generatedPassword.isNotBlank()) {
                Text(
                    text = "Generated: $generatedPassword",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val clip = ClipData.newPlainText("Generated Password", generatedPassword)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Copy Password")
                }
            }

        }
    }
}