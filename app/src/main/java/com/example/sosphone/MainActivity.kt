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

    /*
    Cada vez que vuelve el Activity a la cabeza de la pila,
    debemos de volver a seleccionar el phone pasado mediante el intent
    desde el Activity 1.
     */
    override fun onResume() {
        super.onResume()
        permisionPhone = isPermissionCall()
        val stringPhone  = getString(R.string.string_phone)
        phoneSOS = intent.getStringExtra(stringPhone)
        mainBinding.txtPhone.setText(phoneSOS)


    }



    private fun init(){
        registerLauncher()
        if (!isPermissionCall()) //verificamos permisos en de llamada.
            requestPermissionLauncher.launch(Manifest.permission. CALL_PHONE)

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


    /*
        Para registrar una petición de permisos que aún no se ha lanzado....

        - Con ActivityResultContracts.RequestPermission(), indicamos a Android
        que solicitamos un único permiso peligroso.
        - la lambda,es la lógica que queremos hacer, cuando el usuario haya pulsado
        aceptar/denegar el permiso solicitado.
    */
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


    //inicia el proceso de llamada, junto con la petición de permisos.
    private fun initEventCall() {
        mainBinding.button.setOnClickListener {
            permisionPhone=isPermissionCall()
            if (permisionPhone)
                call()
            else
                requestPermissionLauncher.launch(Manifest.permission. CALL_PHONE)
        }
    }
    private fun isPermissionCall():Boolean{
        //Para versión del sdk inferior a la API 23, no hace falta pedir permisos en t. ejecución.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true  //no hace falta pedir permisos en t. real al usuario
        else
            return isPermissionToUser() //Hay que ver si se concedieron en ejecución o no.
    }

    /*
    - ContextCompat es un objeto que accede a funciones del sistema.
    - PackageManager es el encargado de saber qué paquetes tiene instalado y qué permisos fueron concedidos.
    1.- Comprueba que el permiso fue concedido. Para ello, debe tener un valor de 0.
    2.- Si devuelve -1, es que el permiso no fue concedido.
     */
    private fun isPermissionToUser() = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    /*
    Realizamos la petición de llamada.
    1.- Es una intención y por tanto un intent implícito.
    2.- Una URI, es la forma común que identificar un recurso. Los intent, necesitan encapsular
    un objeto de tipo URI, representando algo común para todas las intenciones.
         - Una llamada telefónica, una web, geo, un mail, un fichero son recursos que hay que pasar al Intent
         - Necesitamos convertir un string a Uri, para ello utilizamos la función parse.

    - Intent  ¿Qué quieres hacer?
    - Uri   ¿Con qué recurso?
     */
    private fun call() {
        val intent = Intent(Intent.ACTION_CALL).apply {  //creamos la intención
            //Indicamos la Uri que es la forma de indicarle a Android que es un teléfono.
            data = Uri.parse("tel:"+phoneSOS!!)
        }
        startActivity(intent)
    }

/*
Queremos abrir la configuración de configuración de una aplicación en concreto.
1.- Settings es una clase del sistema operativo, que contiene utilidades para abrir configuraciones.
2.- Con ACTION_APPLICATION_DETAILS_SETTINGS, indicamos detalles de una aplicación en cuestión.
3.- Necesitamos una URI, que represente el recurso a la intención, para ello hay que pasarle qué
aplicación queremos que muestre sus detalles de configuración. Con Uri.fromParts, convertimos a una
Uri, del que desglosamos por partes los parámetros. En una llamada telef´nica, no necesitamos separar
en partes tel:112, pero en otro tipo de recursos, si. Imagina una web dinámica al que tenemos que pasarle
un parámetro con un valor u otro.
    - "package"  identifica que queremos trabajar con un paquete y por tanto aplicación única.
    - packageName identifica nuestra aplicación como paquete  com.example.sosphone
    - null (no utilizamos fragmentos)
 */
    private fun goToConfiguracionApp(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }

        startActivity(intent)
    }

    /*
    Con el flag Intent.FLAG_ACTIVITY_REORDER_TO_FRONT indicamos que el Activity no se vuelva a crear,
    sino que se traiga al frente de la pila de actividades (back stack) si ya existe.

    Sin embargo, cada vez que se llama a startActivity(), Android crea un nuevo objeto Intent,
    aunque la actividad destino ya exista en memoria.

    El problema es que, al reactivar una actividad existente mediante este flag, dicha actividad
    conserva el Intent anterior (el que tenía al crearse originalmente) y no recibe automáticamente
    los nuevos datos que se hayan añadido con putExtra().

    Para solucionar este comportamiento, debemos sobrescribir el método onNewIntent(),
    el cual se invoca cuando una actividad existente recibe un nuevo Intent o tiene el flag .
    Dentro de este método, basta con llamar a setIntent(intent) para reemplazar
    el Intent antiguo por el nuevo y así disponer de los extras actualizados.
 */
    //Main
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Actualiza el Intent con los nuevos extras
    }

}

/*
1. Registro del permiso:
Cuando llamamos a registerForActivityResult(), registramos el contrato (en este caso, ActivityResultContracts.RequestPermission()) y, al mismo tiempo, proporcionamos una función lambda. Esta lambda define lo que queremos que suceda cuando el usuario acepte o deniegue el permiso.

En este momento, todavía no estamos solicitando el permiso, simplemente estamos "preparando" la acción que ocurrirá después de que el usuario interactúe con la solicitud de permiso.

2. Lanzamiento del permiso:
Cuando llamas a launch(), es cuando realmente se solicita el permiso al usuario. Esto hará que aparezca el diálogo de solicitud de permiso en la pantalla del dispositivo (el diálogo en el que el usuario puede aceptar o denegar el permiso).

3. Respuesta del usuario:
Si el usuario acepta o deniega el permiso, esa respuesta activa la lambda que registraste previamente en registerForActivityResult().
La lambda es una función de orden superior que recibe como argumento el resultado de la acción del usuario (es decir, si el permiso fue concedido o no).
 */