package com.example.csscantenas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AntennaDetailActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var antennaTitle: TextView
    private lateinit var rendimientoSection: TextView
    private lateinit var cpuSection: TextView
    private lateinit var cpuDetalladoSection: TextView
    private lateinit var traficoSection: TextView
    private lateinit var estadoGeneralSection: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_antenna_detail)

        database = FirebaseDatabase.getInstance().reference

        antennaTitle = findViewById(R.id.antennaTitle)
        rendimientoSection = findViewById(R.id.rendimientoSection)
        cpuSection = findViewById(R.id.cpuSection)
        cpuDetalladoSection = findViewById(R.id.cpuDetalladoSection)
        traficoSection = findViewById(R.id.traficoSection)
        estadoGeneralSection = findViewById(R.id.estadoGeneralSection)

        val antennaName = intent.getStringExtra("antennaName") ?: "Sin nombre"

        loadAntennaDetails(antennaName)
    }

    private fun loadAntennaDetails(antennaName: String) {
        val antennaDataRef = database.child("antenas").child("aggregated_data").child("antennas")

        antennaDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val antennas = snapshot.children.mapNotNull { it.getValue(object : GenericTypeIndicator<Map<String, Any>>() {}) as Map<String, Any>? }
                    val antenna = antennas.find { it["nombre"] == antennaName }
                    if (antenna != null) {

                        val ip = antenna["ip"] as? String ?: "IP no encontrada"
                        antennaTitle.text = "Antena: $antennaName (IP: $ip)"

                        val rendimiento = antenna["Rendimiento"] as? Map<String, String>
                        rendimientoSection.text = if (rendimiento != null) {
                            "Rendimiento:\n" +
                                    "  Memoria Usada: ${rendimiento["Memoria_Usada"] ?: "N/A"}\n" +
                                    "  Memoria Libre: ${rendimiento["Memoria_Libre"] ?: "N/A"}"
                        } else {
                            "Rendimiento: N/A"
                        }

                        val cpu = antenna["Uso_de_CPU"] as? Map<String, String>
                        cpuSection.text = if (cpu != null) {
                            "Uso de CPU:\n" +
                                    "  Uso Total: ${cpu["Uso_Total"] ?: "N/A"}"
                        } else {
                            "Uso de CPU: N/A"
                        }

                        val cpuDetallado = antenna["Uso_de_CPU_Detallado"] as? Map<String, String>
                        cpuDetalladoSection.text = if (cpuDetallado != null) {
                            "Uso de CPU Detallado:\n" +
                                    "  Uso por Usuario: ${cpuDetallado["Uso_por_Usuario"] ?: "N/A"}\n" +
                                    "  Uso por Sistema: ${cpuDetallado["Uso_por_Sistema"] ?: "N/A"}\n" +
                                    "  Tiempo Ocioso: ${cpuDetallado["Tiempo_Ocioso"] ?: "N/A"}"
                        } else {
                            "Uso de CPU Detallado: N/A"
                        }

                        val traficoDatos = antenna["Trafico_de_Datos"] as? Map<String, String>
                        traficoSection.text = if (traficoDatos != null) {
                            "Tráfico de Datos:\n" +
                                    "  Bajada: ${traficoDatos["Bajada"] ?: "N/A"}\n" +
                                    "  Subida: ${traficoDatos["Subida"] ?: "N/A"}"
                        } else {
                            "Tráfico de Datos: N/A"
                        }

                        val estadoGeneral = antenna["Estado_General"] as? Map<String, String>
                        estadoGeneralSection.text = if (estadoGeneral != null) {
                            "Estado General:\n" +
                                    "  AP Asociado: ${estadoGeneral["AP_Asociado"] ?: "N/A"}\n" +
                                    "  Nombre Dispositivo: ${estadoGeneral["Nombre_Dispositivo"] ?: "N/A"}\n" +
                                    "  Modo Máscara Red: ${estadoGeneral["Modo_Mascara_Red"] ?: "N/A"}\n" +
                                    "  Ancho de Canal: ${estadoGeneral["Ancho_de_Canal"] ?: "N/A"}\n" +
                                    "  Potencia TX: ${estadoGeneral["Potencia_TX"] ?: "N/A"}\n" +
                                    "  Antena: ${estadoGeneral["Antena"] ?: "N/A"}\n" +
                                    "  Última Actualización: ${estadoGeneral["Ultima_Actualizacion"] ?: "N/A"}"
                        } else {
                            "Estado General: N/A"
                        }
                    } else {
                        antennaTitle.text = "Datos de la antena no encontrados"
                        rendimientoSection.text = ""
                        cpuSection.text = ""
                        cpuDetalladoSection.text = ""
                        traficoSection.text = ""
                        estadoGeneralSection.text = ""
                    }
                } else {
                    antennaTitle.text = "Datos no disponibles"
                    rendimientoSection.text = ""
                    cpuSection.text = ""
                    cpuDetalladoSection.text = ""
                    traficoSection.text = ""
                    estadoGeneralSection.text = ""
                }
            }

            override fun onCancelled(error: DatabaseError) {
                antennaTitle.text = "Error: ${error.message}"
                rendimientoSection.text = ""
                cpuSection.text = ""
                cpuDetalladoSection.text = ""
                traficoSection.text = ""
                estadoGeneralSection.text = ""
            }
        })
    }
}