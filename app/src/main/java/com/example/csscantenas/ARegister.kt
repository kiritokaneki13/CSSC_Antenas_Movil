package com.example.csscantenas

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

class ARegister : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var ipInput: EditText
    private lateinit var nombreInput: EditText
    private lateinit var usuarioInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var addButton: Button
    private lateinit var antennaSpinner: Spinner
    private lateinit var statusText: TextView
    private var antennaList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_aregister)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseDatabase.getInstance().reference
        ipInput = findViewById(R.id.ipInput)
        nombreInput = findViewById(R.id.nombreInput)
        usuarioInput = findViewById(R.id.usuarioInput)
        passwordInput = findViewById(R.id.passwordInput)
        addButton = findViewById(R.id.addButton)
        antennaSpinner = findViewById(R.id.antennaSpinner)
        statusText = findViewById(R.id.statusText)

        setupSpinner()
        setupAddButton()
    }

    private fun setupSpinner() {
        database.child("antenas_registradas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                antennaList.clear()
                snapshot.children.forEach { antenna ->
                    val nombre = antenna.child("nombre").getValue(String::class.java) ?: "Sin nombre"
                    val ip = antenna.child("ip").getValue(String::class.java) ?: ""
                    antennaList.add("$nombre ($ip)")
                }
                val adapter = ArrayAdapter(this@ARegister, android.R.layout.simple_spinner_item, antennaList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                antennaSpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ARegister, "Error al cargar antenas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAddButton() {
        addButton.setOnClickListener {
            val ip = ipInput.text.toString()
            val nombre = nombreInput.text.toString()
            val usuario = usuarioInput.text.toString()
            val password = passwordInput.text.toString()

            if (ip.isNotEmpty() && nombre.isNotEmpty() && usuario.isNotEmpty() && password.isNotEmpty()) {
                val newAntenna = mapOf(
                    "ip" to ip,
                    "nombre" to nombre,
                    "activa" to true,
                    "usuario_ssh" to usuario,
                    "password_ssh" to password
                )
                val newKey = database.child("antenas_registradas").push().key ?: return@setOnClickListener
                database.child("antenas_registradas").child(newKey).setValue(newAntenna)
                    .addOnSuccessListener {
                        Toast.makeText(this@ARegister, "Antena agregada: $nombre", Toast.LENGTH_SHORT).show()
                        ipInput.text.clear()
                        nombreInput.text.clear()
                        usuarioInput.text.clear()
                        passwordInput.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@ARegister, "Error al agregar antena", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this@ARegister, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}