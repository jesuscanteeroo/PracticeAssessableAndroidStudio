package com.example.sosphone

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sosphone.databinding.ActivityConfBinding
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

class ConfActivity : AppCompatActivity() {

    private lateinit var confBinding: ActivityConfBinding
    private lateinit var sharedFich: SharedPreferences
    private lateinit var nameSharedPhone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //layoutInflater, es el objeto que se encarga de inflar las vistas de la interfaz.
        confBinding = ActivityConfBinding.inflate(layoutInflater)

        //activityMaiinBinding, ya es el objeto con las views infladas.
        //insertamos la IU del activity, a partir de la raiz del árbol generado de views,
        setContentView(confBinding.root)
    //    confBinding.

        // Llamamos a la función que habilita el modo de borde a borde
        //enableEdgeToEdge()

        //Ajustamos los margenes con binding.
       /* ViewCompat.setOnApplyWindowInsetsListener(confBinding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
*/
        initPreferentShared()
        start()
    }

    private fun initPreferentShared() {
        val nameSharedFich = getString(R.string.name_preferen_shared_fich)
        this.nameSharedPhone = getString(R.string.name_shared_phone)

        //Abrimos el fichero de preferencias compartidas privada.
        this.sharedFich = getSharedPreferences(nameSharedFich, Context.MODE_PRIVATE)
    }

    override fun onResume() {
        super.onResume()
        val ret = intent.getBooleanExtra("back", false)
        if (ret){
            confBinding.editPhone.setText("")
            Toast.makeText(this, R.string.msg_new_phone, Toast.LENGTH_LONG).show()
            intent.removeExtra("back")  //por si se interrumpe.
        }
    }

    private fun start(){
        //buscamos la preferencia del phone guardada. En caso de que no esté, será null
        val sharedPhone : String?  = sharedFich.getString(nameSharedPhone, null)

        sharedPhone?.let {
            startMainActivity(it)
        }

        confBinding.btnConf.setOnClickListener {
            val numberPhone = confBinding.editPhone.text.toString()
            if (numberPhone.isEmpty())
                Toast.makeText(this, R.string.msg_empty_phone, Toast.LENGTH_LONG).show()
            else
                if (!isValidPhoneNumber2(numberPhone, "ES"))
                    Toast.makeText(this, R.string.msg_not_valid_phone, Toast.LENGTH_LONG).show()
                else{
                    //registramos el teléfono, ya que es válido.
                    val edit = sharedFich.edit()
                    edit.putString(nameSharedPhone, numberPhone)
                    edit.apply()
                    startMainActivity(numberPhone)
                }
        }
    }


    private fun startMainActivity(phone: String) {
        val intent = Intent(this@ConfActivity, MainActivity::class.java)
        intent.apply {
            putExtra(getString(R.string.string_phone), phone)
            //ese Flag, no volverá a crear una instancia del intent. Será la misma
            /*
            1.- No crean una nueva instancia de Activity. Por defecto y sin flags, se crearán en la pila
            tantas instancias como veces llames al Activity. Esto no es lo que queremos.
            2.- El flag CLEAR_TOP, lo que hace es eliminar todas las instancias de activitys que hayan
            por encima del que se quiere lanzar y por tanto, vuelve a la cabeza de la pila
            3.- el flag single_top, significa que sólo se creará una instancia del activity aunque se haya
            llamado 20 veces al mismo.
            4.- Estos flags, provocan que se ejecute el método onNewIntent en el Activity que se quiere volver
            a llamar. De esa forma, puede refrescar el intent con los nuevos datos.
             */
            //FLAG_ACTIVITY_REORDER_TO_FRONT
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent ) //lanzamos el Activity
    }


    //clase de Android que valida un teléfono.
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return PhoneNumberUtils.isGlobalPhoneNumber(phoneNumber)
    }

    fun isValidPhoneNumber2(phoneNumber: String, countryCode: String): Boolean {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            // Parseamos el número de teléfono en base al código de país proporcionado
            val number = phoneUtil.parse(phoneNumber, countryCode)
            // Verificamos si es un número válido para el país especificado
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            e.printStackTrace()
            false
        }
    }

 /*
 Necesitamos sobreescribie este método, porque es el intent actualizado que utilizó
 el activity MainActivity con el booleano ret a true. Esto es porque la instancia
 creada en MainActivity, es el mismo y por sí sólo, cuando volvemos a modificar el intent
 con nuevos datos, de manera implícita no lo actualiza el Activity ConfActivity, por ello
 necesitamos sobreescribir este método, para que actualize el intent recibido con los
 nuevos datos.
  */
    //Conf
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Actualiza el Intent con los nuevos extras
    }
}