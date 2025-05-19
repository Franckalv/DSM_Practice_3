package com.example.todoapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.models.Tarea
import com.example.todoapp.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var logoutButton: Button
    private lateinit var welcomeText: TextView
    private lateinit var btnAgregar: Button
    private var tareasFiltradas = listOf<Tarea>()
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        userEmail = auth.currentUser?.email

        welcomeText = findViewById(R.id.welcomeText)
        logoutButton = findViewById(R.id.logoutButton)
        btnAgregar = findViewById(R.id.btnAgregarTarea)
        recyclerView = findViewById(R.id.recyclerTareas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        welcomeText.text = "Bienvenido, $userEmail"

        logoutButton.setOnClickListener {
            auth.signOut()
            finish()
        }

        btnAgregar.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }

        cargarTareas()
    }

    private fun cargarTareas() {
        RetrofitClient.apiService.getAllTareas().enqueue(object : Callback<List<Tarea>> {
            override fun onResponse(call: Call<List<Tarea>>, response: Response<List<Tarea>>) {
                if (response.isSuccessful) {
                    val todas = response.body() ?: emptyList()
                    tareasFiltradas = todas.filter { it.createdBy == userEmail }
                    recyclerView.adapter = TareaAdapter(
                        tareasFiltradas,
                        onEditar = { tarea -> mostrarDialogoEditarTarea(tarea) },
                        onEliminar = { tarea -> eliminarTarea(tarea) }
                    )
                } else {
                    Toast.makeText(this@MainActivity, "Error al cargar tareas", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Tarea>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Fallo de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun eliminarTarea(tarea: Tarea) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar tarea")
            .setMessage("¿Estás segura de que quieres eliminar esta tarea?\n\n${tarea.title}")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                RetrofitClient.apiService.eliminarTarea(tarea.id).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Toast.makeText(this@MainActivity, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                        cargarTareas()
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error al eliminar tarea", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarTarea(tarea: Tarea) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_tarea, null)
        val inputTitulo = view.findViewById<EditText>(R.id.inputTitulo)
        val inputDescripcion = view.findViewById<EditText>(R.id.inputDescripcion)

        inputTitulo.setText(tarea.title)
        inputDescripcion.setText(tarea.description)

        AlertDialog.Builder(this)
            .setTitle("Editar tarea")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = inputTitulo.text.toString()
                val descripcion = inputDescripcion.text.toString()

                val tareaEditada = tarea.copy(title = titulo, description = descripcion)

                RetrofitClient.apiService.actualizarTarea(tarea.id, tareaEditada).enqueue(object : Callback<Tarea> {
                    override fun onResponse(call: Call<Tarea>, response: Response<Tarea>) {
                        Toast.makeText(this@MainActivity, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                        cargarTareas()
                    }

                    override fun onFailure(call: Call<Tarea>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoAgregarTarea() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_tarea, null)
        val inputTitulo = view.findViewById<EditText>(R.id.inputTitulo)
        val inputDescripcion = view.findViewById<EditText>(R.id.inputDescripcion)

        AlertDialog.Builder(this)
            .setTitle("Nueva tarea")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = inputTitulo.text.toString()
                val descripcion = inputDescripcion.text.toString()

                if (titulo.isNotBlank() && userEmail != null) {
                    val nueva = Tarea(
                        title = titulo,
                        description = descripcion,
                        createdBy = userEmail!!
                    )

                    RetrofitClient.apiService.crearTarea(nueva).enqueue(object : Callback<Tarea> {
                        override fun onResponse(call: Call<Tarea>, response: Response<Tarea>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@MainActivity, "Tarea agregada", Toast.LENGTH_SHORT).show()
                                cargarTareas()
                            }
                        }

                        override fun onFailure(call: Call<Tarea>, t: Throwable) {
                            Toast.makeText(this@MainActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Título vacío o usuario no válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
