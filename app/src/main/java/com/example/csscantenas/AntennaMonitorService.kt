package com.example.csscantenas

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer

class AntennaMonitorService : Service() {

    private lateinit var database: DatabaseReference
    private val ANTENNA_PATH = "antenas/aggregated_data/antennas"
    private val ALERTS_PATH = "antenas/alerts"
    private val CHECK_INTERVAL = 30000L
    private val INACTIVITY_THRESHOLD = 2 * 60 * 1000L

    private val dateFormatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    private val TAG = "AntennaMonitor"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio creado")

        database = FirebaseDatabase.getInstance().reference
        startForeground(1, NotificationHelper.createServiceNotification(this))

        monitorAntennas()
    }

    private fun monitorAntennas() {
        Log.d(TAG, "Iniciando monitoreo de antenas...")

        timer(period = CHECK_INTERVAL) {
            Log.d(TAG, "Verificando antenas...")
            val ref = database.child(ANTENNA_PATH)

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val now = System.currentTimeMillis()

                    for (antennaSnapshot in snapshot.children) {
                        val data = antennaSnapshot.value as? Map<*, *> ?: continue
                        val nombre = data["nombre"] as? String ?: "Sin nombre"
                        val ip = data["ip"] as? String ?: "IP desconocida"

                        val estadoGeneral = data["Estado_General"] as? Map<*, *>
                        val ultimaActualizacionStr = estadoGeneral?.get("Ultima_Actualizacion") as? String

                        val lastUpdateDate = try {
                            if (!ultimaActualizacionStr.isNullOrEmpty()) {
                                dateFormatter.parse(ultimaActualizacionStr)
                            } else null
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parseando fecha de $nombre: $ultimaActualizacionStr", e)
                            null
                        }

                        val lastUpdateMillis = lastUpdateDate?.time ?: 0L

                        if (lastUpdateMillis > 0 && now - lastUpdateMillis > INACTIVITY_THRESHOLD) {
                            Log.d(TAG, "❌ Sin datos: $nombre ($ip) - última actualización: $ultimaActualizacionStr")
                            val timestamp = dateFormatter.format(Date())
                            val alertRef = database.child(ALERTS_PATH).push()
                            alertRef.setValue(mapOf(
                                "timestamp" to timestamp,
                                "antennaName" to nombre,
                                "message" to "No se reciben datos desde $ultimaActualizacionStr"
                            )).addOnSuccessListener {
                                Log.d(TAG, "Alerta guardada en Firebase para $nombre")
                            }.addOnFailureListener {
                                Log.e(TAG, "Error al guardar alerta para $nombre: ${it.message}")
                            }

                            val handler = Handler(Looper.getMainLooper())
                            handler.post {
                                NotificationHelper.showAntennaAlert(
                                    context = this@AntennaMonitorService,
                                    title = "⚠ Antena sin datos",
                                    message = "No se reciben datos de $nombre ($ip) desde $ultimaActualizacionStr"
                                )
                            }
                        } else {
                            Log.d(TAG, "✅ Antena OK: $nombre ($ip) - última actualización: $ultimaActualizacionStr")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error leyendo base de datos: ${error.message}")
                }
            })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand recibido")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Servicio detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}