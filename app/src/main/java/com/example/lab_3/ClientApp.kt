package com.example.lab_3

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.ActivityCompat
import java.io.IOException


class ClientApp(private val activity: MainActivity, private val targetDevice: BluetoothDevice) : SocketApp() {
    private lateinit var clientConnection: BluetoothSocket

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            connectToServer()
            setupStreams()
            whileRunning()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        finally {
            return null
        }
    }

    @Throws(IOException::class)
    private fun connectToServer() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        clientConnection = targetDevice.createRfcommSocketToServiceRecord(APP_UUID)
        clientConnection.connect()
        connection = clientConnection
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun whileRunning() {
        val vibrator: Vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        while (true) {
            val msg = input!!.readObject() as String
            activity.runOnUiThread { activity.customGetMessagesAdapter()!!.add("SERVER: $msg") }
            if (activity.muteOff == false){
                val vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            }
        }
    }
}


