package com.staxpayments.sdk.networking

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class JsonParser {
    companion object {
        val gson = GsonBuilder()
            .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()

        fun <T> toJson(obj: T): String {
            return gson.toJson(obj)
        }

        fun <T> fromJson(json: String): T {
            val type = object : TypeToken<T>() {}.type

            val gson = GsonBuilder()
                .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .serializeNulls()
                .setLenient()
                .create()

            return gson.fromJson<T>(json, type)
        }
    }
}
