package com.example.ovep.services;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static IbgeService getIbgeService() {
        if (retrofit == null) {

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)  // timeout de conexão
                    .readTimeout(30, TimeUnit.SECONDS)     // timeout para resposta
                    .writeTimeout(30, TimeUnit.SECONDS)    // timeout para envio dados (se precisar)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://servicodados.ibge.gov.br/api/v1/localidades/")
                    .client(okHttpClient)   // seta o client com timeout personalizado
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(IbgeService.class);
    }
}
