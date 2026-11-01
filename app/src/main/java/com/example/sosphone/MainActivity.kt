package com.example.sosphone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sosphone.databinding.ActivityPpalBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityPpalBinding
    private var phoneSOS: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var permisionPhone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityPpalBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(mainBinding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        init()
        initEventCall()
        initEventWeb()
        initEventEmail()
        initEventLocation()
        initEventAlarm()
    }

    override fun onResume() {
        super.onResume()

        permisionPhone = isPermissionCall()

        val stringPhone = getString(R.string.string_phone)
        phoneSOS = intent.getStringExtra(stringPhone)

        mainBinding.txtPhone.text =
            getString(R.string.numero_introducido, phoneSOS)
    }

    private fun init() {
        registerLauncher()

        mainBinding.ivChangePhone.setOnClickListener {
            val nameSharedFich =
                getString(R.string.name_preferen_shared_fich)
            val nameSharedPhone =
                getString(R.string.name_shared_phone)

            val sharedFich =
                getSharedPreferences(nameSharedFich, Context.MODE_PRIVATE)

            sharedFich.edit().remove(nameSharedPhone).apply()

            val intent = Intent(this, ConfActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra("back", true)
            }

            startActivity(intent)
        }
    }

    private fun initEventWeb() {
        mainBinding.btnWeb.setOnClickListener {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://toystory.disney.com/")
            )
            startActivity(webIntent)
        }
    }

    private fun initEventEmail() {
        mainBinding.btnEmail.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
            }
            startActivity(emailIntent)
        }
    }

    private fun initEventLocation() {
        mainBinding.btnLocation.setOnClickListener {
            val locationIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=Patrocinio+De+Biedma")
            )
            startActivity(locationIntent)
        }
    }
    private fun initEventAlarm() {
        mainBinding.btnAlarm.setOnClickListener {
            try {
                val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, 120)
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Alarma SOSPhone")
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                }

                startActivity(timerIntent)

            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "No se encuentra una aplicación de reloj compatible",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun registerLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                permisionPhone = true
            } else {
                Toast.makeText(
                    this,
                    "Necesitas habilitar los permisos",
                    Toast.LENGTH_LONG
                ).show()
                goToConfiguracionApp()
            }
        }
    }

    private fun initEventCall() {
        mainBinding.button.setOnClickListener {
            val intent = Intent(this, CallActivity::class.java).apply {
                putExtra(getString(R.string.string_phone), phoneSOS)
            }
            startActivity(intent)
        }
    }

    private fun isPermissionCall(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            true
        } else {
            isPermissionToUser()
        }
    }

    private fun isPermissionToUser(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun call() {
        val number = phoneSOS

        if (number.isNullOrBlank()) {
            Toast.makeText(
                this,
                "No hay un número configurado",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
        }
        startActivity(intent)
    }

    private fun goToConfiguracionApp() {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}