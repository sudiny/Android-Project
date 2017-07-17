package com.vongvia.kotlingank.net


/**
 * Created by vongvia on 2017/5/26.
 */

object NetWork {
    val Client = okhttp3.OkHttpClient.Builder()
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                        .newBuilder()
                        .addHeader("Charset", "UTF-8")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build()
                chain.proceed(request)
            }
            .build()

    //Json
    fun <S> create(service: Class<S>, baseUrl: String): S {
        val retrofit = retrofit2.Retrofit.Builder()
                .client(com.vongvia.kotlingank.net.NetWork.Client)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .addCallAdapterFactory(retrofit2.adapter.rxjava.RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build()
        return retrofit.create(service)
    }

    fun <S> getBaseUrl(service: Class<S>): String {
        try {
            val field = service.getDeclaredField("BASE_URL")
            return field.get(service) as String
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}