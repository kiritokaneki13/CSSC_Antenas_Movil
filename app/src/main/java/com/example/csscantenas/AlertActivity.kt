package com.example.csscantenas

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AlertActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var alertText: TextView
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val TAG = "AlertActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        database = FirebaseDatabase.getInstance().reference
        alertText = findViewById(R.id.alertText)

        // Verifica si Huawei Health está instalada
        val isHuaweiHealthInstalled = isPackageInstalled("com.huawei.health")
        val currentTime = dateFormat.format(Date())
        alertText.text = "[$currentTime] Estado: " +
                if (isHuaweiHealthInstalled) "Conexión con Huawei Health iniciada" else "Huawei Health no instalada"
        Log.d(TAG, "Estado inicial: Huawei Health ${if (isHuaweiHealthInstalled) "instalada" else "no instalada"}")

        loadAlerts()
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun loadAlerts() {
        val alertsRef = database.child("antenas").child("alerts")

        alertsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = StringBuilder()
                val currentTime = dateFormat.format(Date())

                // Estado de Huawei Health
                val isHuaweiHealthInstalled = isPackageInstalled("com.huawei.health")
                data.append("[$currentTime] Estado: " +
                        if (isHuaweiHealthInstalled) "Conexión con Huawei Health activa" else "Huawei Health no instalada\n")
                Log.d(TAG, "Estado actualizado: Huawei Health ${if (isHuaweiHealthInstalled) "activa" else "no disponible"}")

                if (snapshot.exists()) {
                    snapshot.children.forEach { alertSnapshot ->
                        val timestamp = alertSnapshot.child("timestamp").getValue(String::class.java)
                            ?: dateFormat.format(Date()) // Usa hora actual si no hay timestamp
                        val antennaName = alertSnapshot.child("antennaName").getValue(String::class.java) ?: "Sin nombre"
                        val message = alertSnapshot.child("message").getValue(String::class.java) ?: "Sin mensaje"
                        data.append("[$timestamp] $antennaName: $message\n")
                        Log.d(TAG, "Procesada alerta: $antennaName - $message a las $timestamp")
                    }
                } else {
                    data.append("[${dateFormat.format(Date())}] No hay alertas nuevas\n")
                }

                alertText.text = data.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                val currentTime = dateFormat.format(Date())
                alertText.text = "[$currentTime] Error: ${error.message}\n" +
                        "[${dateFormat.format(Date())}] Huawei Health: Conexión interrumpida"
                Log.e(TAG, "Error al cargar alertas: ${error.message}")
            }
        })
    }
}