package com.example.todoapp.network

import com.example.todoapp.models.Tarea
import retrofit2.Call
import retrofit2.http.*

interface ToDoApiService {
    @GET("to-do")
    fun getAllTareas(): Call<List<Tarea>>

    @POST("to-do")
    fun crearTarea(@Body tarea: Tarea): Call<Tarea>

    @PUT("to-do/{id}")
    fun actualizarTarea(@Path("id") id: String, @Body tarea: Tarea): Call<Tarea>

    @DELETE("to-do/{id}")
    fun eliminarTarea(@Path("id") id: String): Call<Void>
}
