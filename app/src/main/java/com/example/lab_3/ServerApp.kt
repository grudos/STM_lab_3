package com.example.lab_3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.ActivityCompat
import java.io.IOException


open class ServerApp(private val activity: MainActivity, private val mBluetoothAdapter: BluetoothAdapter) : SocketApp() {
    private var socket: BluetoothServerSocket? = null

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
            socket = mBluetoothAdapter
                .listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
            while (true) {
                waitForConnection()
                setupStreams()
                whileRunning()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } finally {
            return null
        }
    }

    @Throws(IOException::class)
    private fun waitForConnection() {
        try {
            connection = socket!!.accept()
        } catch (e: IOException) {
            println("Ex in listening RFCOMM: " + e.message)
            return
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun whileRunning() {
        val vibrator: Vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        while (true) {
            val msg = input!!.readObject() as String
            activity.runOnUiThread { activity.customGetMessagesAdapter()!!.add("CLIENT: $msg") }
            if (activity.muteOff == false){
                val vibrationEffect = VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            }
        }
    }
}


