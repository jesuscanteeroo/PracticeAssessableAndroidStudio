package com.example.sosphone

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sosphone.databinding.ActivityPpalBinding
import android.Manifest
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding : ActivityPpalBinding
    private  var phoneSOS : String? = null
    //Acepta como lanzador un string que representa el permiso a solicitar.
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var permisionPhone = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityPpalBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(mainBinding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        init()
        initEventCall()

    }

    override fun onResume() {
        super.onResume()
        permisionPhone = isPermissionCall()
        val stringPhone  = getString(R.string.string_phone)
        phoneSOS = intent.getStringExtra(stringPhone)
        mainBinding.txtPhone.text = getString(R.string.numero_introducido, phoneSOS)


    }

    private fun init(){
        registerLauncher()

        mainBinding.ivChangePhone.setOnClickListener {
            val nameSharedFich = getString(R.string.name_preferen_shared_fich)
            val nameSharedPhone = getString(R.string.name_shared_phone)
            val sharedFich = getSharedPreferences(nameSharedFich, Context.MODE_PRIVATE)
            val edit = sharedFich.edit()
            edit.remove(nameSharedPhone)
            edit.apply()
            val intent = Intent(this, ConfActivity::class.java )
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("back", true)//volvemos desde El ACtivity2
                }
            startActivity(intent)
        }

    }

    private fun registerLauncher(){
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { //lambda
            /*
            Se ejecuta esta lambda, cuando el usuario pulsa aceptar/denegar
             */
                isGranted->
            if (isGranted) {
                permisionPhone = true
            }
            else {
                Toast.makeText( this, "Necesitas habilitar los permisos",
                    Toast.LENGTH_LONG).show()
                goToConfiguracionApp()  //abrimos la configuración de la aplicación.
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
    private fun isPermissionCall():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true  //no hace falta pedir permisos en t. real al usuario
        else
            return isPermissionToUser() //Hay que ver si se concedieron en ejecución o no.
    }

    private fun isPermissionToUser() = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    private fun call() {
        val intent = Intent(Intent.ACTION_CALL).apply {  //creamos la intención
            //Indicamos la Uri que es la forma de indicarle a Android que es un teléfono.
            data = Uri.parse("tel:"+phoneSOS!!)
        }
        startActivity(intent)
    }

    private fun goToConfiguracionApp(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }

        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Actualiza el Intent con los nuevos extras
    }

}