package com.example.bluetoothdatos
/*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bluetoothdatos.ui.theme.BluetoothDatosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothDatosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothDatosTheme {
        Greeting("Android")
    }
}*/


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectedThread: ConnectedThread? = null

    private lateinit var handler: Handler

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )

        setContent {
            var heartRate by remember { mutableStateOf("N/A") }
            var steps by remember { mutableStateOf("N/A") }
            var sleep by remember { mutableStateOf("N/A") }
            var calories by remember { mutableStateOf("N/A") }

            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("❤️ Frecuencia cardíaca: $heartRate")
                    Text("👣 Pasos: $steps")
                    Text("🛏️ Sueño: $sleep h")
                    Text("🔥 Calorías: $calories kcal")

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = {
                        connectedThread?.write("GET_DATA".toByteArray())
                    }) {
                        Text("Extraer datos")
                    }
                }
            }

            // Configura el handler para recibir datos
            LaunchedEffect(Unit) {
                handler = object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        val received = msg.obj as? ByteArray
                        val message = received?.toString(Charsets.UTF_8) ?: ""
                        val parts = message.trim().split(";")
                        if (parts.size >= 4) {
                            heartRate = parts[0]
                            steps = parts[1]
                            sleep = parts[2]
                            calories = parts[3]
                        }
                    }
                }
                connectToDevice()
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToDevice() {
        val device = bluetoothAdapter?.bondedDevices?.firstOrNull { it.name.contains("Smartwatch", true) }
        if (device != null) {
            try {
                val uuid = device.uuids?.firstOrNull()?.uuid
                    ?: UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()

                connectedThread = ConnectedThread(bluetoothSocket!!, handler)
                connectedThread?.start()

                Toast.makeText(this, "Conectado a ${device.name}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al conectar", Toast.LENGTH_SHORT).show()
                Log.e("BT", "Error de conexión", e)
            }
        } else {
            Toast.makeText(this, "Smartwatch no emparejado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectedThread?.cancel()
    }



}
