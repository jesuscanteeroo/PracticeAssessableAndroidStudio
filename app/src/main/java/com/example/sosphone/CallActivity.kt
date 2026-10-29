package com.example.sosphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CallActivity : AppCompatActivity() {



    private var phone: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        phone = intent.getStringExtra(getString(R.string.string_phone))

        findViewById<TextView>(R.id.txtCallPhone).text = phone

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                call()
            } else {
                Toast.makeText(
                    this,
                    "Necesitas conceder permiso para llamar",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        findViewById<ImageButton>(R.id.btnMakeCall).setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                call()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
    }

    private fun call() {
        val number = phone

        if (number.isNullOrBlank()) {
            Toast.makeText(this, "No hay un número configurado", Toast.LENGTH_LONG).show()
            return
        }

        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
        }

        startActivity(callIntent)
    }
}