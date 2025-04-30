package com.fattmerchant.android.serialization

import com.squareup.moshi.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class UtcDateAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: Set<Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        // Only apply to Date + @Utc
        val delegateAnnotations = Types.nextAnnotations(annotations, Utc::class.java) ?: return null
        if (type != Date::class.java) return null

        return object : JsonAdapter<Date>() {
            // Updated format to handle "Z" at the end of the ISO 8601 date string
            private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            override fun fromJson(reader: JsonReader): Date? {
                val str = reader.nextString()
                return formatter.parse(str)
            }

            override fun toJson(writer: JsonWriter, value: Date?) {
                writer.value(value?.let { formatter.format(it) })
            }
        }.nullSafe()
    }
}
