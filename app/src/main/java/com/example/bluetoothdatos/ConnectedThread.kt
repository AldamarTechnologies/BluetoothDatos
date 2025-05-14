package com.example.bluetoothdatos

import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.InputStream
import java.io.OutputStream

class ConnectedThread(
    private val socket: BluetoothSocket,
    private val handler: Handler
) : Thread() {

    private val inStream: InputStream = socket.inputStream
    private val outStream: OutputStream = socket.outputStream
    private val buffer: ByteArray = ByteArray(1024)

    override fun run() {
        while (true) {
            try {
                val bytes = inStream.read(buffer)
                handler.obtainMessage(0, bytes, -1, buffer.copyOf(bytes)).sendToTarget()
            } catch (e: Exception) {
                Log.e("BT", "Error leyendo del inputStream", e)
                break
            }
        }
    }

    fun write(bytes: ByteArray) {
        try {
            outStream.write(bytes)
        } catch (e: Exception) {
            Log.e("BT", "Error escribiendo al outputStream", e)
        }
    }

    fun cancel() {
        socket.close()
    }
}
