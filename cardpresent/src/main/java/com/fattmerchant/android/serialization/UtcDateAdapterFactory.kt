package com.fattmerchant.android.serialization

import com.squareup.moshi.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class UtcDateAdapterFactory : JsonAdapter.Factory {

    override fun create(
        type: Type,
        annotations: Set<Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        // check for UTC annotation - if found proceed with format, otherwise do not
        val delegateAnnotations = Types.nextAnnotations(annotations, Utc::class.java) ?: return null
        if (type != Date::class.java) return null

        return object : JsonAdapter<Date>() {

            private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            override fun fromJson(reader: JsonReader): Date? {
                val str = reader.nextString()
                return try {
                    formatter.parse(str)
                } catch (e: ParseException) {
                    throw JsonDataException("Cannot decode date: $str")
                }
            }

            override fun toJson(writer: JsonWriter, value: Date?) {
                writer.value(value?.let { formatter.format(it) })
            }
        }.nullSafe()
    }
}
