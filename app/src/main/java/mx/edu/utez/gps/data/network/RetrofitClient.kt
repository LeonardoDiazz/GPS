package mx.edu.utez.gps.data.network


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // ¡¡¡IMPORTANTE: PEGA AQUÍ TU URL DE MOCKAPI!!!
    // Debe terminar en diagonal '/'
    private const val BASE_URL = "https://693bc40db762a4f15c3e32be.mockapi.io/"

    val apiService: GpsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GpsApiService::class.java)
    }
}