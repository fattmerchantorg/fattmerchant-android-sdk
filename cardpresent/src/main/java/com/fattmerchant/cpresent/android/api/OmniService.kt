package com.fattmerchant.cpresent.android.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class OmniService {

    val interceptor = Interceptor { chain ->
        val requestBuilder = chain?.request()?.newBuilder()
            ?.addHeader("Accept", "application/json")
            ?.addHeader("Content-Type", "application/json")

        if (token != null) {
            requestBuilder!!.addHeader("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder!!.build())
    }

    var httpClient = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    var moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    var token: String? =
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZXJjaGFudCI6ImE2MWQ3OGNjLWNkZTktNDRhYy04YTE4LTMwYzM5YmUwNTg3OSIsImdvZFVzZXIiOnRydWUsInN1YiI6IjMwYzZlZWI2LTY0YjYtNDdmNi1iY2Y2LTc4N2E5YzU4Nzk4YiIsImlzcyI6Imh0dHA6Ly9hcGlkZXYwMS5mYXR0bGFicy5jb20vYXV0aGVudGljYXRlIiwiaWF0IjoxNTY4NjI0OTEyLCJleHAiOjE1Njg3MTEzMTIsIm5iZiI6MTU2ODYyNDkxMiwianRpIjoidHh2c1VpUkt6TXVkUnh0WCJ9.sTFTfPw9_NPqzkpONQTX6_iAAHJTGN-3I8LWDR2sMTY"

    var omniApi: OmniApi = Retrofit.Builder()
        .baseUrl("https://apidev01.fattlabs.com")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient)
        .build()
        .create(OmniApi::class.java)
}