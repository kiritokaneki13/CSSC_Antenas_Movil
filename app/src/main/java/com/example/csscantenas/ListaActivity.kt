package com.example.csscantenas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import androidx.core.view.setPadding
import com.google.firebase.database.GenericTypeIndicator

class ListaActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var antennaListLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)

        database = FirebaseDatabase.getInstance().reference

        antennaListLayout = findViewById(R.id.antennaListLayout)

        loadAntennaButtons()
    }

    private fun loadAntennaButtons() {
        val antennaDataRef = database.child("antenas").child("aggregated_data").child("antennas")

        antennaDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                antennaListLayout.removeAllViews()
                if (snapshot.exists()) {
                    val antennas = snapshot.children.mapNotNull {
                        it.getValue(object : GenericTypeIndicator<Map<String, Any>>() {}) as Map<String, Any>?
                    }
                    antennas.forEach { antenna ->
                        val nombre = antenna["nombre"] as? String ?: "Sin nombre"
                        val button = Button(this@ListaActivity).apply {
                            text = nombre
                            textSize = 18f
                            setTextColor(android.graphics.Color.WHITE)
                            setBackgroundColor(android.graphics.Color.parseColor("#2A2A2A"))
                            setPadding(24, 32, 24, 32)
                            elevation = 8f
                            background = resources.getDrawable(R.drawable.button_dark, null)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 12, 0, 12)
                            }
                            setOnClickListener {
                                val intent = Intent(this@ListaActivity, AntennaDetailActivity::class.java)
                                intent.putExtra("antennaName", nombre)
                                startActivity(intent)
                            }
                        }
                        antennaListLayout.addView(button)
                    }
                } else {
                    antennaListLayout.addView(Button(this@ListaActivity).apply {
                        text = "No hay antenas disponibles"
                        isEnabled = false
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                antennaListLayout.addView(Button(this@ListaActivity).apply {
                    text = "Error: ${error.message}"
                    isEnabled = false
                })
            }
        })
    }
}