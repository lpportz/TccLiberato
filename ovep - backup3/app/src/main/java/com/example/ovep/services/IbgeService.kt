package com.example.ovep.services

import com.example.ovep.models.Cidade
import com.example.ovep.models.Estado
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface IbgeService {

    @GET("estados")
    fun listarEstados(): Call<List<Estado>>

    @GET("estados/{id}/municipios")
    fun listarCidades(@Path("id") estadoId: Int): Call<List<Cidade>>
}
