package com.example.lab_3

import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*


abstract class SocketApp : AsyncTask<Void?, Void?, Void?>() {
    protected var input: ObjectInputStream? = null
    protected var output: ObjectOutputStream? = null
    protected var connection: BluetoothSocket? = null
    @Throws(IOException::class)
    protected fun setupStreams() {
        output = ObjectOutputStream(connection!!.outputStream)
        output!!.flush()
        input = ObjectInputStream(connection!!.inputStream)
    }

    @Throws(IOException::class)
    fun sendMsg(msg: String?) {
        output!!.writeObject(msg)
        output!!.flush()
    }

    companion object {
        @JvmStatic
        val APP_NAME = "STM Lab 3"
        val APP_UUID = UUID.fromString("5b1e84a5-24c5-4a6d-b3ee-3c2ea01b02c4")
    }
}

