package com.example.lab_3

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private var targetDevice: BluetoothDevice? = null
    private var activity: MainActivity? = null
    var statusBar: TextView? = null
        private set
    var listMessages: ListView? = null
        private set
    var etSend: EditText? = null
        private set
    var btnSend: Button? = null
        private set
    var btnMute: Button? = null
        private set
    var muteOff: Boolean? = false
    val messagesArray = ArrayList<String?>()
    var messagesAdapter: ArrayAdapter<String?>? = null
        private set
    private var app: SocketApp? = null

    private val BLUETOOTH_PERMISSION_REQUEST = 1001

    fun customGetMessagesAdapter(): ArrayAdapter<String?>? {
        return messagesAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this
        statusBar = findViewById(R.id.statusBar)
        listMessages = findViewById(R.id.listMessages)
        etSend = findViewById(R.id.etSend)
        btnSend = findViewById(R.id.btnSend)
        btnMute = findViewById(R.id.btnMute)

        checkBluetoothPermissions()

        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            println("BT not supported")
            return
        }
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        initialize(mBluetoothAdapter)
        messagesAdapter = ArrayAdapter<String?>(
            activity!!,
            android.R.layout.simple_list_item_1, messagesArray
        )
        listMessages!!.adapter = messagesAdapter
        messagesArray.add("TEST")

        val btnBondServer = findViewById(R.id.btnBondServer) as Button
        val btnBondClient = findViewById(R.id.btnBondClient) as Button
        btnBondServer.setOnClickListener(View.OnClickListener {
            if (targetDevice == null) {
                return@OnClickListener
            }
            app = ServerApp(activity!!, mBluetoothAdapter)
            (app as ServerApp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        })
        btnBondClient.setOnClickListener(View.OnClickListener {
            if (targetDevice == null) {
                return@OnClickListener
            }
            app = ClientApp(activity!!, targetDevice!!)
            (app as ClientApp).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        })
        btnSend!!.setOnClickListener(View.OnClickListener {
            val text = etSend!!.text.toString()
            try {
                etSend!!.setText("")
                messagesAdapter!!.add("ME: $text")
                app!!.sendMsg(text)
            } catch (e: IOException) {
                return@OnClickListener
            }
        })
        btnMute!!.setOnClickListener(View.OnClickListener {
            if (muteOff == false){
                muteOff = true
            }
            else {
                muteOff = false
            }
        })
    }

    fun initialize(mBluetoothAdapter: BluetoothAdapter) {
        val btSpinner = activity!!.findViewById(R.id.btSpinner) as Spinner
        val pairedDevices: Set<BluetoothDevice> = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            emptySet()
        } else {
            mBluetoothAdapter.bondedDevices
        }

        val pairedDevicesList = java.util.ArrayList<String?>()
        for (device in pairedDevices) {
            pairedDevicesList.add(
                """
            ${device.name}
            [${device.address}]
            """.trimIndent()
            )
        }

        val adapter: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
            activity!!,
            android.R.layout.simple_list_item_1, pairedDevicesList as List<Any?>
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        btSpinner.adapter = adapter

        btSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val targetDeviceDesc = parent.getItemAtPosition(position).toString()
                val mac = targetDeviceDesc.substring(
                    targetDeviceDesc.indexOf('[') + 1, targetDeviceDesc.indexOf(']')
                )
                for (device in pairedDevices) {
                    if (device.address.toString() == mac) {
                        targetDevice = device
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bluetoothPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH
            )
            val bluetoothAdminPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADMIN
            )
            val bluetoothConnectPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            )

            val permissions = arrayOf(
                bluetoothPermission, bluetoothAdminPermission, bluetoothConnectPermission
            )

            if (permissions.any { it != PackageManager.PERMISSION_GRANTED }) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_PRIVILEGED
                    ),
                    BLUETOOTH_PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                }
                else {
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}
